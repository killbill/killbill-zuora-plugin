package com.ning.killbill.zuora.api;

import java.util.LinkedList;
import java.util.List;

import com.ning.billing.payment.api.PaymentMethodPlugin;

public class ZuoraPaymentMethodPlugin implements PaymentMethodPlugin {

    private final String paymentMethodId;
    private boolean isDefault;
    private List<PaymentMethodKVInfo> properties;
    
    public ZuoraPaymentMethodPlugin(final String paymentMethodId, boolean isDefault, final List<PaymentMethodKVInfo> properties) {
        this.paymentMethodId = paymentMethodId;
        this.isDefault = isDefault;
        this.properties = properties;
    }
    
    public ZuoraPaymentMethodPlugin(final String paymentMethodId, boolean isDefault) {
        this.paymentMethodId = paymentMethodId;
        this.isDefault = isDefault;
        this.properties = new LinkedList<PaymentMethodPlugin.PaymentMethodKVInfo>();
    }

    @Override
    public String getExternalPaymentMethodId() {
        return paymentMethodId;
    }

    @Override
    public boolean isDefaultPaymentMethod() {
        return isDefault;
    }

    @Override
    public List<PaymentMethodKVInfo> getProperties() {
        return properties;
    }
    
    public void addProperty(String key, Object value, boolean isEditable) {
        properties.add(new PaymentMethodKVInfo(key, value, isEditable));
    }


    @Override
    public String getValueString(String key) {
        if (properties == null) {
            return null;
        }
        for (PaymentMethodKVInfo cur : properties) {
            if (cur.getKey().equals(key)) {
                return cur.getValue() != null ? cur.getValue().toString() : null;
            }
        }
        return null;
    }
    
}
