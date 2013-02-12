package com.ning.killbill.zuora.http;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.api.PaymentMethodPlugin.PaymentMethodKVInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class PaymentMethodJson {


    private final String paymentMethodId;
    private final String accountId;
    private final Boolean isDefault;
    private final String pluginName;
    private final PaymentMethodPluginDetailJson pluginInfo;

    @JsonCreator
    public PaymentMethodJson(@JsonProperty("paymentMethodId") final String paymentMethodId,
                             @JsonProperty("accountId") final String accountId,
                             @JsonProperty("isDefault") final Boolean isDefault,
                             @JsonProperty("pluginName") final String pluginName,
                             @JsonProperty("pluginInfo") final PaymentMethodPluginDetailJson pluginInfo) {
        super();
        this.paymentMethodId = paymentMethodId;
        this.accountId = accountId;
        this.isDefault = isDefault;
        this.pluginName = pluginName;
        this.pluginInfo = pluginInfo;
    }

    public static PaymentMethodJson toPaymentMethodJson(final Account account, final PaymentMethod in) {

        final boolean isDefault = account.getPaymentMethodId() != null && account.getPaymentMethodId().equals(in.getId());
        PaymentMethodPluginDetailJson detail = null;
        if (in.getPluginDetail() != null) {
            List<PaymentMethodProperties> properties = null;
            if (in.getPluginDetail().getProperties() != null) {
                properties = new ArrayList<PaymentMethodProperties>(Collections2.transform(in.getPluginDetail().getProperties(), new Function<PaymentMethodKVInfo, PaymentMethodProperties>() {
                    @Override
                    public PaymentMethodProperties apply(final PaymentMethodKVInfo input) {
                        return new PaymentMethodProperties(input.getKey(), input.getValue() == null ? null : input.getValue().toString(), input.getIsUpdatable());
                    }
                }));
            }
            detail = new PaymentMethodPluginDetailJson(in.getPluginDetail().getExternalPaymentMethodId(), properties);
        }
        return new PaymentMethodJson(in.getId().toString(), account.getId().toString(), isDefault, in.getPluginName(), detail);
    }


    public PaymentMethodJson() {
        this(null, null, null, null, null);
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public String getAccountId() {
        return accountId;
    }

    @JsonProperty("isDefault")
    public Boolean isDefault() {
        return isDefault;
    }

    public String getPluginName() {
        return pluginName;
    }

    public PaymentMethodPluginDetailJson getPluginInfo() {
        return pluginInfo;
    }

    public static class PaymentMethodPluginDetailJson {

        private final String externalPaymentId;
        private final List<PaymentMethodProperties> properties;


        @JsonCreator
        public PaymentMethodPluginDetailJson(@JsonProperty("externalPaymentId") final String externalPaymentId,
                                             @JsonProperty("properties") final List<PaymentMethodProperties> properties) {
            this.externalPaymentId = externalPaymentId;
            this.properties = properties;
        }

        public static PaymentMethodPluginDetailJson toPaymentMethodPluginDetailJson(final String externalPaymentId,
                                             final List<PaymentMethodKVInfo> properties) {

            final List<PaymentMethodProperties> props = new ArrayList<PaymentMethodProperties>();
            props.addAll(Collections2.transform(properties, new Function<PaymentMethodKVInfo, PaymentMethodProperties>() {
                @Override
                public PaymentMethodProperties apply(final PaymentMethodKVInfo input) {
                    return new PaymentMethodProperties(input);
                }
            }));
            return new PaymentMethodPluginDetailJson(externalPaymentId, props);
        }

        public String getExternalPaymentId() {
            return externalPaymentId;
        }

        public List<PaymentMethodProperties> getProperties() {
            return properties;
        }
    }

    public static final class PaymentMethodProperties {

        private final String key;
        private final String value;
        private final Boolean isUpdatable;

        @JsonCreator
        public PaymentMethodProperties(@JsonProperty("key") final String key,
                                       @JsonProperty("value") final String value,
                                       @JsonProperty("isUpdatable") final Boolean isUpdatable) {
            super();
            this.key = key;
            this.value = value;
            this.isUpdatable = isUpdatable;
        }

        public PaymentMethodProperties(PaymentMethodKVInfo info) {
            this(info.getKey(), (String)info.getValue(), info.getIsUpdatable());
        }


        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public Boolean getIsUpdatable() {
            return isUpdatable;
        }
    }
}
