package com.ning.killbill.zuora.zuora;

import com.ning.billing.payment.plugin.api.PaymentProviderAccount;
import com.zuora.api.object.Account;

public class AccountConverter implements Converter<Account, PaymentProviderAccount> {
    @Override
    public PaymentProviderAccount convert(Account account) {
        return new PaymentProviderAccount.Builder()
                              .setId(account.getId())
                              .setAccountKey(account.getAccountNumber())
                              .setDefaultPaymentMethod(account.getDefaultPaymentMethodId())
                              .build();
    }
}
