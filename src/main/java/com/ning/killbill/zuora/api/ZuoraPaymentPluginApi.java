package com.ning.killbill.zuora.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.log.LogService;

import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.payment.plugin.api.PaymentProviderAccount;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.TenantContext;

import com.zuora.api.object.PaymentMethod;
import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.zuora.AccountConverter;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.Converter;
import com.ning.killbill.zuora.zuora.IdentityConverter;
import com.ning.killbill.zuora.zuora.PaymentConverter;
import com.ning.killbill.zuora.zuora.PaymentMethodConverter;
import com.ning.killbill.zuora.zuora.PoolException;
import com.ning.killbill.zuora.zuora.ZuoraApi;
import com.ning.killbill.zuora.zuora.ZuoraConnection;
import com.ning.killbill.zuora.zuora.ZuoraError;
import com.ning.killbill.zuora.zuora.ZuoraErrorConverter;
import com.ning.killbill.zuora.util.Either;

public class ZuoraPaymentPluginApi implements PaymentPluginApi {

    private static <S1, S2, T1, T2> Either<T1, T2> convert(Either<S1, S2> source, Converter<S1, T1> converter1, Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        }
        else {
            return Either.right(converter2 == null ? null : converter2.convert(source.getRight()));
        }
    }

    private static <S1, S2, T1, T2> Either<T1, List<T2>> convertList(Either<S1, List<S2>> source, Converter<S1, T1> converter1, Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        }
        else {
            List<T2> objs = new ArrayList<T2>();
            for (S2 sourceObj : source.getRight()) {
                objs.add(converter2.convert(sourceObj));
            }
            return Either.right(objs);
        }
    }

    private static interface ConnectionCallback<T> {
        T withConnection(ZuoraConnection connection);
    }

    private final ZuoraErrorConverter errorConverter = new ZuoraErrorConverter();
    private final PaymentConverter paymentConverter = new PaymentConverter();
    private final AccountConverter accountConverter = new AccountConverter();
    private final IdentityConverter<String> stringConverter = new IdentityConverter<String>();
    private final ConnectionPool pool;
    private final ZuoraApi api;
    private final String instanceName;
    private final LogService logService;

    public ZuoraPaymentPluginApi(final ConnectionPool pool, final ZuoraApi api, final LogService logService, final String instanceName) {
        this.pool = pool;
        this.api = api;
        this.instanceName = instanceName;
        this.logService = logService;
    }

    private <T> T withConnection(ConnectionCallback<T> callback) {
        ZuoraConnection connection = pool.borrowFromPool();

        try {
            return callback.withConnection(connection);
        }
        finally {
            if (connection != null) {
                try {
                    pool.returnToPool(connection);
                }
                catch (PoolException ex) {
                    logService.log(LogService.LOG_INFO, "Error while returning a zuora connection to the pool", ex);
                }
            }
        }
    }

    @Override
    public String getName() {
        return instanceName;
    }


    @Override
    public PaymentInfoPlugin processPayment(final String externalKey, final UUID paymentId, final BigDecimal amount, final CallContext context) throws PaymentPluginApiException {
        Either<ZuoraError, PaymentInfoPlugin> result =  withConnection(new ConnectionCallback<Either<ZuoraError, PaymentInfoPlugin>>() {
            @Override
            public Either<ZuoraError, PaymentInfoPlugin> withConnection(ZuoraConnection connection) {
                return convert(api.processPayment(connection, externalKey, amount, paymentId.toString()), errorConverter, paymentConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID paymentId, final TenantContext context) throws PaymentPluginApiException {
        Either<ZuoraError, PaymentInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentInfoPlugin>>() {
            @Override
            public Either<ZuoraError, PaymentInfoPlugin> withConnection(ZuoraConnection connection) {
                // STEPH WRONG From paymentId -> get zuor paymentID
                return convert(api.getPaymentById(connection, paymentId.toString()), errorConverter, paymentConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public String createPaymentProviderAccount(final Account account, final CallContext context) throws PaymentPluginApiException {
        Either<ZuoraError, String> result =  withConnection(new ConnectionCallback<Either<ZuoraError, String>>() {
            @Override
            public Either<ZuoraError, String> withConnection(ZuoraConnection connection) {
                return convert(api.createPaymentProviderAccount(connection, account), errorConverter, stringConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final String accountKey, final String externalPaymentId, final TenantContext context)
        throws PaymentPluginApiException {
        Either<ZuoraError, PaymentMethodPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentMethodPlugin>>() {
            @Override
            public Either<ZuoraError, PaymentMethodPlugin> withConnection(ZuoraConnection connection) {
                Either<ZuoraError, PaymentMethod> paymentMethodOrError = api.getPaymentMethodById(connection, externalPaymentId);

                if (paymentMethodOrError.isLeft()) {
                    return convert(paymentMethodOrError, errorConverter, null);
                }
                else {
                    Either<ZuoraError, com.zuora.api.object.Account> accountOrError = api.getAccountById(connection, paymentMethodOrError.getRight().getAccountId());

                    if (accountOrError.isLeft()) {
                        return convert(accountOrError, errorConverter, null);
                    }
                    else {
                        com.zuora.api.object.Account account = accountOrError.getRight();
                        PaymentMethodConverter converter = new PaymentMethodConverter(account);
                        return convert(paymentMethodOrError, errorConverter, converter);
                    }
                }
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public List<PaymentMethodPlugin> getPaymentMethodDetails(final String accountName, final TenantContext context) throws PaymentPluginApiException {
        Either<ZuoraError, List<PaymentMethodPlugin>> result =  withConnection(new ConnectionCallback<Either<ZuoraError, List<PaymentMethodPlugin>>>() {
            @Override
            public Either<ZuoraError, List<PaymentMethodPlugin>> withConnection(ZuoraConnection connection) {
                Either<ZuoraError, com.zuora.api.object.Account> accountOrError = api.getByAccountName(connection, accountName);

                if (accountOrError.isLeft()) {
                    return convert(accountOrError, errorConverter, null);
                }
                else {
                    com.zuora.api.object.Account account = accountOrError.getRight();
                    PaymentMethodConverter converter = new PaymentMethodConverter(account);
                    Either<ZuoraError, List<PaymentMethod>> paymentMethods = api.getPaymentMethodsForAccount(connection, account);
                    return convertList(paymentMethods, errorConverter, converter);
                }
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public String addPaymentMethod(final String accountKey, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final CallContext context) throws PaymentPluginApiException {
        Either<ZuoraError, String> result = withConnection(new ConnectionCallback<Either<ZuoraError, String>>() {
            @Override
            public Either<ZuoraError, String> withConnection(ZuoraConnection connection) {
                return convert(api.addPaymentMethod(connection, accountKey, paymentMethodProps, setDefault), errorConverter, stringConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public void deletePaymentMethod(final String accountKey, final String paymentMethodId, final CallContext context) throws PaymentPluginApiException {
        Either<ZuoraError, Void> result =  withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(ZuoraConnection connection) {
                return convert(api.deletePaymentMethod(connection, accountKey, paymentMethodId), errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }

    @Override
    public void updatePaymentMethod(final String accountKey, final PaymentMethodPlugin paymentMethodProps, final CallContext context) throws PaymentPluginApiException {

        final String paymentMethodType = paymentMethodProps.getValueString(PaymentMethodProperties.TYPE);
        if (CreditCardProperties.TYPE_VALUE.equals(paymentMethodType)) {

            Either<ZuoraError, PaymentMethodPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentMethodPlugin>>() {
                @Override
                public Either<ZuoraError, PaymentMethodPlugin> withConnection(ZuoraConnection connection) {
                    Either<ZuoraError, com.zuora.api.object.Account> accountOrError = api.getByAccountName(connection, accountKey);

                    if (accountOrError.isLeft()) {
                        return convert(accountOrError, errorConverter, null);
                    }
                    else {
                        com.zuora.api.object.Account account = accountOrError.getRight();
                        Either<ZuoraError, PaymentMethod> paymentMethodOrError = api.updateCreditCardPaymentMethod(connection, account, paymentMethodProps);

                        if (paymentMethodOrError.isLeft()) {
                            return convert(paymentMethodOrError, errorConverter, null);
                        }
                        else {
                            PaymentMethodConverter converter = new PaymentMethodConverter(account);
                            return convert(paymentMethodOrError, errorConverter, converter);
                        }
                    }
                }
            });
            if (result.isLeft()) {
                throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
            }
            return;
        }
        throw new PaymentPluginApiException(ZuoraError.ERROR_UNSUPPORTED, "Payment method " + paymentMethodType + " cannot be updated by the Zuora plugin");
    }

    @Override
    public void setDefaultPaymentMethod(final String accountKey,
            final String externalPaymentId, final CallContext context) throws PaymentPluginApiException {
        Either<ZuoraError, Void> result =  withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(ZuoraConnection connection) {


                Either<ZuoraError, PaymentMethod> paymentMethodOrError = api.getPaymentMethodById(connection, externalPaymentId);
                if (paymentMethodOrError.isLeft()) {
                    return convert(paymentMethodOrError, errorConverter, null);
                }
                PaymentMethod paymentMethod = paymentMethodOrError.getRight();

                Either<ZuoraError, Void> updatePaymentOrError = api.setDefaultPaymentMethod(connection, accountKey, paymentMethod);
                return convert(updatePaymentOrError, errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }

    @Override
    public void processRefund(final Account account, final UUID paymentId, final BigDecimal refundAmount, final CallContext context) throws PaymentPluginApiException  {
        Either<ZuoraError, Void> result =  withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(ZuoraConnection connection) {
                Either<ZuoraError, com.zuora.api.object.Account>  accountOrError = api.getByAccountName(connection, account.getExternalKey());
                if (accountOrError.isLeft()) {
                    return Either.left(accountOrError.getLeft());
                }
                final String accountId = accountOrError.getRight().getId();

                Either<ZuoraError, List<com.zuora.api.object.Payment>> paymentsOrError = api.getProcessedPaymentsForAccount(connection, accountId);
                if (paymentsOrError.isLeft()) {
                    return Either.left(paymentsOrError.getLeft());
                }
                com.zuora.api.object.Payment paymentToBeRefunded = null;
                for (com.zuora.api.object.Payment cur : paymentsOrError.getRight()) {

                    if (cur.getComment() != null && cur.getComment().equals(paymentId.toString())) {
                        paymentToBeRefunded = cur;
                        break;
                    }
                }
                if (paymentToBeRefunded == null) {
                    return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "Can't find Payment object for refund"));
                }
                return api.createRefund(connection, paymentToBeRefunded.getId(), paymentId.toString(), refundAmount);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }

    @Override
    public int getNbRefundForPaymentAmount(final Account account, final UUID paymentId, final BigDecimal refundAmount, final TenantContext context)
    throws PaymentPluginApiException {
        Either<ZuoraError, Integer> result =  withConnection(new ConnectionCallback<Either<ZuoraError, Integer>>() {
            @Override
            public Either<ZuoraError, Integer> withConnection(ZuoraConnection connection) {
                Either<ZuoraError, com.zuora.api.object.Account>  accountOrError = api.getByAccountName(connection, account.getExternalKey());
                if (accountOrError.isLeft()) {
                    return Either.left(accountOrError.getLeft());
                }
                final String accountId = accountOrError.getRight().getId();

                Either<ZuoraError, List<com.zuora.api.object.Payment>> paymentsOrError = api.getProcessedPaymentsForAccount(connection, accountId);
                if (paymentsOrError.isLeft()) {
                    return Either.left(paymentsOrError.getLeft());
                }
                com.zuora.api.object.Payment paymentToBeRefunded = null;
                for (com.zuora.api.object.Payment cur : paymentsOrError.getRight()) {
                    if (cur.getComment() != null && cur.getComment().equals(paymentId.toString())) {
                        paymentToBeRefunded = cur;
                        break;
                    }
                }
                if (paymentToBeRefunded == null) {
                    return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "Can't find Payment object for refund"));
                }
                Either<ZuoraError, List<com.zuora.api.object.RefundInvoicePayment>> refundsOrError = api.getRefundsForPayment(connection, paymentToBeRefunded.getId());
                if (refundsOrError.isLeft()) {
                    return Either.left(refundsOrError.getLeft());
                }
                int result = 0;
                for (com.zuora.api.object.RefundInvoicePayment cur : refundsOrError.getRight()) {
                    if (cur.getRefundAmount().compareTo(refundAmount) == 0) {
                        result++;
                    }
                }
                return Either.right(result);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    // STEPH removed from the Plugin interface as they don't seem needed, but we'll see...
    public void updatePaymentProviderAccountExistingContact(final Account account) throws PaymentPluginApiException {
        Either<ZuoraError, Void> result =  withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(ZuoraConnection connection) {
                return convert(api.updatePaymentProviderAccountExistingContact(connection, account), errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }


    public PaymentProviderAccount getPaymentProviderAccount(final String accountKey) throws PaymentPluginApiException {
        Either<ZuoraError, PaymentProviderAccount> result =  withConnection(new ConnectionCallback<Either<ZuoraError, PaymentProviderAccount>>() {
            @Override
            public Either<ZuoraError, PaymentProviderAccount> withConnection(ZuoraConnection connection) {
                Either<ZuoraError, com.zuora.api.object.Account> accountOrError = api.getByAccountName(connection, accountKey);
                return convert(accountOrError, errorConverter, accountConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    public void updatePaymentProviderAccountWithNewContact(final Account account) throws PaymentPluginApiException {
        Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(ZuoraConnection connection) {
                return convert(api.updatePaymentProviderAccountExistingContact(connection, account), errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }
}
