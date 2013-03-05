package com.ning.killbill.zuora.zuora;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mockito.Mockito;
import org.osgi.service.log.LogService;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.killbill.zuora.util.Either;
import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.zuora.api.object.Account;
import com.zuora.api.object.PaymentMethod;

public class MockZuoraApi extends ZuoraApi {

    private final Map<UUID, Account> accounts = new HashMap<UUID, Account>();
    private final Map<String, UUID> accountExternalKeys = new HashMap<String, UUID>();
    private final Map<UUID, PaymentMethod> paymentMethods = new HashMap<UUID, PaymentMethod>();
    private final Map<String, UUID> defaultPaymentMethodPerAccount = new HashMap<String, UUID>();

    public MockZuoraApi() {
        super(Mockito.mock(ZuoraConfig.class), Mockito.mock(LogService.class));
    }

    @Override
    public Either<ZuoraError, PaymentMethod> addPaymentMethod(final ZuoraConnection connection, final String accountExternalKey, final PaymentMethodPlugin paymentMethodProps, final boolean setDefault) {
        synchronized (paymentMethods) {
            final UUID paymentMethodId = UUID.randomUUID();

            final PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setAccountId(accountExternalKeys.get(accountExternalKey).toString());
            paymentMethod.setAchAccountName(accountExternalKey);
            paymentMethod.setId(paymentMethodId.toString());

            paymentMethods.put(paymentMethodId, paymentMethod);

            if (setDefault) {
                setDefaultPaymentMethod(null, accountExternalKey, paymentMethod);
            }

            return Either.right(paymentMethod);
        }
    }

    @Override
    public Either<ZuoraError, List<PaymentMethod>> getPaymentMethodsForAccount(final ZuoraConnection connection, final Account account) {
        final List<PaymentMethod> results = ImmutableList.<PaymentMethod>copyOf(Collections2.filter(paymentMethods.values(), new Predicate<PaymentMethod>() {
            @Override
            public boolean apply(final PaymentMethod input) {
                return input.getAchAccountName().equals(account.getAccountNumber());
            }
        }));

        return Either.right(results);
    }

    @Override
    public Either<ZuoraError, PaymentMethod> getPaymentMethodById(final ZuoraConnection connection, final String paymentMethodId) {
        return Either.right(paymentMethods.get(UUID.fromString(paymentMethodId)));
    }

    @Override
    public Either<ZuoraError, Void> setDefaultPaymentMethod(final ZuoraConnection connection, final String accountKey, final PaymentMethod paymentMethod) {
        synchronized (defaultPaymentMethodPerAccount) {
            defaultPaymentMethodPerAccount.put(accountKey, UUID.fromString(paymentMethod.getId()));
        }

        final Either<ZuoraError, Account> eitherErrorOrAccount = getByAccountName(connection, accountKey);
        if (eitherErrorOrAccount.isRight()) {
            eitherErrorOrAccount.getRight().setDefaultPaymentMethodId(paymentMethod.getId());
        }

        return Either.right(null);
    }

    @Override
    public Either<ZuoraError, Void> deletePaymentMethod(final ZuoraConnection connection, final String accountKey, final String kbPaymentMethodId) {
        synchronized (paymentMethods) {
            for (final UUID paymentMethodId : paymentMethods.keySet()) {
                if (paymentMethods.get(paymentMethodId).getAchAccountName().equals(accountKey)) {
                    paymentMethods.remove(paymentMethodId);
                }
            }
        }

        return Either.right(null);
    }

    @Override
    public Either<ZuoraError, String> createPaymentProviderAccount(final ZuoraConnection connection, final com.ning.billing.account.api.Account inputAccount) {
        synchronized (accounts) {
            final UUID accountId = UUID.randomUUID();

            final Account account = new Account();
            account.setId(accountId.toString());
            account.setAccountNumber(inputAccount.getExternalKey());
            accounts.put(accountId, account);
            accountExternalKeys.put(inputAccount.getExternalKey(), accountId);

            return Either.right(accountId.toString());
        }
    }

    @Override
    public Either<ZuoraError, Account> getAccountById(final ZuoraConnection connection, final String id) {
        final Account account = accounts.get(UUID.fromString(id));
        return Either.right(account);
    }

    @Override
    public Either<ZuoraError, Account> getByAccountName(final ZuoraConnection connection, final String accountName) {
        final UUID zuoraAccountId = accountExternalKeys.get(accountName);
        if (zuoraAccountId == null) {
            return Either.left(new ZuoraError(ZuoraError.ERROR_NOTFOUND, "Can't find Account"));
        }

        return Either.right(accounts.get(zuoraAccountId));
    }
}
