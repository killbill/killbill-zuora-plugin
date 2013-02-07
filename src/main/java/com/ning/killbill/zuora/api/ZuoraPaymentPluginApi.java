/*
 * Copyright 2010-2013 Ning, Inc.
 *
 *  Ning licenses this file to you under the Apache License, version 2.0
 *  (the "License"); you may not use this file except in compliance with the
 *  License.  You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package com.ning.killbill.zuora.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.log.LogService;

import com.ning.billing.account.api.Account;
import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.payment.plugin.api.RefundInfoPlugin;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.TenantContext;
import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.util.Either;
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
import com.ning.killbill.zuora.zuora.RefundConverter;

import com.zuora.api.object.PaymentMethod;

public class ZuoraPaymentPluginApi implements PaymentPluginApi {

    private static <S1, S2, T1, T2> Either<T1, T2>  convert(final Either<S1, S2> source, final Converter<S1, T1> converter1, final Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        } else {
            return Either.right(converter2 == null ? null : converter2.convert(source.getRight()));
        }
    }

    private static <S1, S2, T1, T2> Either<T1, List<T2>> convertList(final Either<S1, List<S2>> source, final Converter<S1, T1> converter1, final Converter<S2, T2> converter2) {
        if (source.isLeft()) {
            return Either.left(converter1.convert(source.getLeft()));
        } else {
            final List<T2> objs = new ArrayList<T2>();
            for (final S2 sourceObj : source.getRight()) {
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
    private final RefundConverter refundConverter = new RefundConverter();
    private final IdentityConverter<String> stringConverter = new IdentityConverter<String>();
    private final ConnectionPool pool;
    private final ZuoraApi api;
    private final String instanceName;
    private final LogService logService;
    private final DefaultKillbillApi killbillApi;

    public ZuoraPaymentPluginApi(final ConnectionPool pool, final ZuoraApi api, final LogService logService, final OSGIKillbill osgiKillbill, final String instanceName) {
        this.pool = pool;
        this.api = api;
        this.instanceName = instanceName;
        this.logService = logService;
        this.killbillApi = new DefaultKillbillApi(osgiKillbill, logService);
    }

    private <T> T withConnection(final ConnectionCallback<T> callback) {
        final ZuoraConnection connection = pool.borrowFromPool();

        try {
            return callback.withConnection(connection);
        } finally {
            if (connection != null) {
                try {
                    pool.returnToPool(connection);
                } catch (PoolException ex) {
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
    public PaymentInfoPlugin processPayment(final UUID kbPaymentId, final UUID kbPaymentMethodId, final BigDecimal amount, final CallContext context) throws PaymentPluginApiException {

        final String accountExternalKey = killbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
        final Either<ZuoraError, PaymentInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentInfoPlugin>>() {
            @Override
            public Either<ZuoraError, PaymentInfoPlugin> withConnection(final ZuoraConnection connection) {
                return convert(api.processPayment(connection, accountExternalKey, amount, kbPaymentId.toString()), errorConverter, paymentConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public PaymentInfoPlugin getPaymentInfo(final UUID kbPaymentId, final TenantContext context) throws PaymentPluginApiException {

        final Either<ZuoraError, PaymentInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, PaymentInfoPlugin>>() {
            @Override
            public Either<ZuoraError, PaymentInfoPlugin> withConnection(final ZuoraConnection connection) {
                return convert(api.getPaymentById(connection, kbPaymentId.toString()), errorConverter, paymentConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }

    @Override
    public RefundInfoPlugin processRefund(final UUID kbPaymentId, final BigDecimal refundAmount, final CallContext context) throws PaymentPluginApiException {

        final String accountExternalKey = killbillApi.getAccountExternalKeyFromPaymentId(kbPaymentId, context);
        final Either<ZuoraError, RefundInfoPlugin> result = withConnection(new ConnectionCallback<Either<ZuoraError, RefundInfoPlugin>>() {
            @Override
            public Either<ZuoraError, RefundInfoPlugin> withConnection(final ZuoraConnection connection) {
                final Either<ZuoraError, com.zuora.api.object.Account> accountOrError = api.getByAccountName(connection, accountExternalKey);
                if (accountOrError.isLeft()) {
                    return Either.left(accountOrError.getLeft());
                }
                final String accountId = accountOrError.getRight().getId();

                final Either<ZuoraError, List<com.zuora.api.object.Payment>> paymentsOrError = api.getProcessedPaymentsForAccount(connection, accountId);
                if (paymentsOrError.isLeft()) {
                    return Either.left(paymentsOrError.getLeft());
                }
                com.zuora.api.object.Payment paymentToBeRefunded = null;
                for (final com.zuora.api.object.Payment cur : paymentsOrError.getRight()) {

                    if (cur.getComment() != null && cur.getComment().equals(kbPaymentId.toString())) {
                        paymentToBeRefunded = cur;
                        break;
                    }
                }
                if (paymentToBeRefunded == null) {
                    return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "Can't find Payment object for refund"));
                }
                return convert(api.createRefund(connection, paymentToBeRefunded.getId(), kbPaymentId.toString(), refundAmount), errorConverter, refundConverter);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        } else {
            return result.getRight();
        }
    }


    @Override
    public void addPaymentMethod(final UUID kbPaymentMethodId, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault, final CallContext context) throws PaymentPluginApiException {

        final String accountExternalKey = killbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
        final Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(final ZuoraConnection connection) {
                final Either<ZuoraError, String> result = api.addPaymentMethod(connection, accountExternalKey, paymentMethodProps, setDefault);
                return null;
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }

    @Override
    public void deletePaymentMethod(final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {

        final String accountExternalKey = killbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
        final Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(final ZuoraConnection connection) {
                return convert(api.deletePaymentMethod(connection, accountExternalKey, kbPaymentMethodId.toString()), errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }

    @Override
    public void setDefaultPaymentMethod(final UUID kbPaymentMethodId, final CallContext context) throws PaymentPluginApiException {

        final String accountExternalKey = killbillApi.getAccountExternalKeyFromPaymentMethodId(kbPaymentMethodId, context);
        final Either<ZuoraError, Void> result = withConnection(new ConnectionCallback<Either<ZuoraError, Void>>() {
            @Override
            public Either<ZuoraError, Void> withConnection(final ZuoraConnection connection) {


                final Either<ZuoraError, PaymentMethod> paymentMethodOrError = api.getPaymentMethodById(connection, kbPaymentMethodId.toString());
                if (paymentMethodOrError.isLeft()) {
                    return convert(paymentMethodOrError, errorConverter, null);
                }
                final PaymentMethod paymentMethod = paymentMethodOrError.getRight();

                final Either<ZuoraError, Void> updatePaymentOrError = api.setDefaultPaymentMethod(connection, accountExternalKey, paymentMethod);
                return convert(updatePaymentOrError, errorConverter, null);
            }
        });
        if (result.isLeft()) {
            throw new PaymentPluginApiException(result.getLeft().getType(), result.getLeft().getMessage());
        }
    }


}
