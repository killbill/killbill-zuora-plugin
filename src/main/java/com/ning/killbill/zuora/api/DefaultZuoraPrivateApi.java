package com.ning.killbill.zuora.api;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.osgi.service.log.LogService;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.TenantContext;
import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;
import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.util.Either;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.PaymentMethodConverter;
import com.ning.killbill.zuora.zuora.ZuoraApi;
import com.ning.killbill.zuora.zuora.ZuoraConnection;
import com.ning.killbill.zuora.zuora.ZuoraError;

import com.zuora.api.object.Invoice;
import com.zuora.api.object.Payment;
import com.zuora.api.object.PaymentMethod;

public class DefaultZuoraPrivateApi extends ZuoraApiBase implements ZuoraPrivateApi {


    public DefaultZuoraPrivateApi(final ConnectionPool pool, final ZuoraApi api, final LogService logService,
                                  final DefaultKillbillApi defaultKillbillApi, final ZuoraPluginDao zuoraPluginDao, final String instanceName) {
        super(pool, api, logService, defaultKillbillApi, zuoraPluginDao, instanceName);

    }

    @Override
    public String createPaymentProviderAccount(final UUID accountId, final TenantContext context) throws PaymentPluginApiException {

        final com.ning.billing.account.api.Account account = defaultKillbillApi.getAccountFromId(accountId, context);
        final Either<ZuoraError, String> result = withConnection(new ConnectionCallback<Either<ZuoraError, String>>() {
            @Override
            public Either<ZuoraError, String> withConnection(final ZuoraConnection connection) {
                return convert(zuoraApi.createPaymentProviderAccount(connection, account), errorConverter, stringConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }

    }

    @Override
    public List<PaymentMethodPlugin> getPaymentMethodDetails(final UUID accountId, final TenantContext context) throws PaymentPluginApiException {

        final String externalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(accountId, context);
        final Either<ZuoraError, List<PaymentMethodPlugin>> result =  withConnection(new ConnectionCallback<Either<ZuoraError, List<PaymentMethodPlugin>>>() {
            @Override
            public Either<ZuoraError, List<PaymentMethodPlugin>> withConnection(final ZuoraConnection connection) {
                final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, externalKey);

                if (accountOrError.isLeft()) {
                    return convert(accountOrError, errorConverter, null);
                }
                else {
                    final com.zuora.api.object.Account account = accountOrError.getRight();
                    final PaymentMethodConverter converter = new PaymentMethodConverter(account);
                    final Either<ZuoraError, List<PaymentMethod>> paymentMethods = zuoraApi.getPaymentMethodsForAccount(connection, account);
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
    public void updateDefaultPaymentMethod(final UUID accountId, final PaymentMethodPlugin paymentMethodProps, final TenantContext context) throws PaymentPluginApiException {

        final String externalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(accountId, context);
        final String paymentMethodType = paymentMethodProps.getValueString(PaymentMethodProperties.TYPE);
        if (CreditCardProperties.TYPE_VALUE.equals(paymentMethodType)) {

            final Either<ZuoraError, PaymentMethodPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentMethodPlugin>>() {
                @Override
                public Either<ZuoraError, PaymentMethodPlugin> withConnection(final ZuoraConnection connection) {
                    final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, externalKey);

                    if (accountOrError.isLeft()) {
                        return convert(accountOrError, errorConverter, null);
                    }
                    else {
                        final com.zuora.api.object.Account account = accountOrError.getRight();
                        final Either<ZuoraError, PaymentMethod> paymentMethodOrError = zuoraApi.updateCreditCardPaymentMethod(connection, account, paymentMethodProps);

                        if (paymentMethodOrError.isLeft()) {
                            return convert(paymentMethodOrError, errorConverter, null);
                        }
                        else {
                            final PaymentMethodConverter converter = new PaymentMethodConverter(account);
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
    public String getExternalPaymentMethodId(final UUID paymentMethodId) throws PaymentPluginApiException {
        final PaymentMethodEntity entity = zuoraPluginDao.getPaymentMethodById(paymentMethodId.toString());
        if (entity != null) {
            return entity.getZuoraPaymentMethodId();
        }
        return null;
    }

    @Override
    public List<Invoice> getInvoices(final UUID accountId, @Nullable final DateTime from, @Nullable final DateTime to, final TenantContext context) throws PaymentPluginApiException {
        final String externalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(accountId, context);
        final Either<ZuoraError, List<Invoice>> result = withConnection(new ConnectionCallback<Either<ZuoraError, List<Invoice>>>() {
            @Override
            public Either<ZuoraError, List<Invoice>> withConnection(final ZuoraConnection connection) {
                final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, externalKey);

                if (accountOrError.isLeft()) {
                    return convert(accountOrError, errorConverter, null);
                } else {
                    final com.zuora.api.object.Account account = accountOrError.getRight();
                    return zuoraApi.getInvoicesForAccount(connection, account, from, to);
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
    public String getInvoiceContent(final UUID accountId, final String invoiceNumber, final TenantContext context) throws PaymentPluginApiException {
        final String externalKey = defaultKillbillApi.getAccountExternalKeyFromAccountId(accountId, context);
        final Either<ZuoraError, String> result = withConnection(new ConnectionCallback<Either<ZuoraError, String>>() {
            @Override
            public Either<ZuoraError, String> withConnection(final ZuoraConnection connection) {
                final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = zuoraApi.getByAccountName(connection, externalKey);

                if (accountOrError.isLeft()) {
                    return convert(accountOrError, errorConverter, null);
                } else {
                    final com.zuora.api.object.Account account = accountOrError.getRight();
                    return zuoraApi.getInvoiceContent(connection, account, invoiceNumber);
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
    public Payment getLastPaymentForInvoice(final String invoiceId, final TenantContext context) throws PaymentPluginApiException {
        final Either<ZuoraError, Payment> result = withConnection(new ConnectionCallback<Either<ZuoraError, Payment>>() {
            @Override
            public Either<ZuoraError, Payment> withConnection(final ZuoraConnection connection) {
                return zuoraApi.getLastPaymentForInvoice(connection, invoiceId);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

}
