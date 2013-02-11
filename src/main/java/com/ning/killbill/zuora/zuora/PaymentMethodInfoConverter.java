package com.ning.killbill.zuora.zuora;

import java.util.List;
import java.util.UUID;

import com.ning.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

import com.zuora.api.object.PaymentMethod;

public class PaymentMethodInfoConverter implements Converter<PaymentMethod, PaymentMethodInfoPlugin> {


    private final UUID kbAccountId;
    private final List<PaymentMethodEntity> pms;
    private final String defaultZuoraPaymentMethodId;

    public PaymentMethodInfoConverter(final UUID kbAccountId, final String defaultZuoraPaymentMethodId, final List<PaymentMethodEntity> pms) {
        this.kbAccountId = kbAccountId;
        this.defaultZuoraPaymentMethodId = defaultZuoraPaymentMethodId;
        this.pms = pms;
    }

    @Override
    public PaymentMethodInfoPlugin convert(final PaymentMethod original) {

        UUID kbPaymentMethodId = null;
        for (PaymentMethodEntity cur : pms) {
            if (cur.getZuoraPaymentMethodId().equals(original.getId())) {
                kbPaymentMethodId = UUID.fromString(cur.getKbPaymentMethodId());
                break;
            }
        }

        final boolean isZuoraDefault = original.getId().equals(defaultZuoraPaymentMethodId);
        final UUID kbPaymentMethodIdForClosure = kbPaymentMethodId;
        return new PaymentMethodInfoPlugin() {
            @Override
            public UUID getAccountId() {
                return kbAccountId;
            }

            @Override
            public UUID getPaymentMethodId() {
                return kbPaymentMethodIdForClosure;
            }

            @Override
            public boolean isDefault() {
                return isZuoraDefault;
            }

            @Override
            public String getExternalPaymentMethodId() {
                return original.getId();
            }
        };
    }
}
