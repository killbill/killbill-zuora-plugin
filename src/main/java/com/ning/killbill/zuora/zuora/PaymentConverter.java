package com.ning.killbill.zuora.zuora;


import java.math.BigDecimal;

import org.joda.time.DateTime;

import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin.PaymentPluginStatus;
import com.zuora.api.object.Payment;

public class PaymentConverter implements Converter<Payment, PaymentInfoPlugin> {
    @Override

    public PaymentInfoPlugin convert(final Payment payment) {

        final DateTime zuoraCreatedDate = ZuoraDateUtils.toDateTime(payment.getCreatedDate());
        //final DateTime zuoraUpdatedDate = ZuoraDateUtils.toDateTime(payment.getUpdatedDate());

        return new PaymentInfoPlugin() {
            @Override
            public PaymentPluginStatus getStatus() {
                return toPluginStatus(payment.getStatus());
            }
            @Override
            public DateTime getEffectiveDate() {
                return ZuoraDateUtils.toDateTime(payment.getEffectiveDate());
            }
            @Override
            public DateTime getCreatedDate() {
                return zuoraCreatedDate;
            }
            @Override
            public BigDecimal getAmount() {
                return payment.getAmount();
            }
            @Override
            public String getGatewayError() {
                return payment.getGatewayResponse();
            }
            @Override
            public String getGatewayErrorCode() {
                return payment.getGatewayResponseCode();
            }
            @Override
            public String getExtFirstReferenceId() {
                return payment.getReferenceId();
            }
            @Override
            public String getExtSecondReferenceId() {
                return payment.getSecondPaymentReferenceId();
            }
        };
    }

    //
    // From zuora doc:
    // Draft, Processing, Processed, Error, Voided
    //
    PaymentPluginStatus toPluginStatus(String status) {
        if (status.equalsIgnoreCase("error")) {
            return PaymentPluginStatus.ERROR;
        } else if (status.equalsIgnoreCase("processed")) {
            return PaymentPluginStatus.PROCESSED;
        } else {
            return PaymentPluginStatus.UNDEFINED;
        }
    }

}
