package com.ning.killbill.zuora.zuora;

import java.math.BigDecimal;

import org.joda.time.DateTime;

import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.payment.plugin.api.RefundInfoPlugin;
import com.ning.billing.payment.plugin.api.RefundInfoPlugin.RefundPluginStatus;

import com.zuora.api.object.Payment;
import com.zuora.api.object.Refund;

public class RefundConverter implements Converter<Refund, RefundInfoPlugin> {

    public RefundInfoPlugin convert(final Refund refund) {
        final DateTime zuoraCreatedDate = ZuoraDateUtils.toDateTime(refund.getCreatedDate());

        return new RefundInfoPlugin() {
            @Override
            public BigDecimal getAmount() {
                return refund.getAmount();
            }

            @Override
            public DateTime getCreatedDate() {
                return zuoraCreatedDate;
            }

            @Override
            public DateTime getEffectiveDate() {
                return refund.getRefundDate();
            }

            @Override
            public RefundPluginStatus getStatus() {
                return toPluginStatus(refund.getStatus());
            }

            @Override
            public String getGatewayError() {
                return refund.getGatewayResponse();
            }

            @Override
            public String getGatewayErrorCode() {
                return refund.getGatewayResponseCode();
            }
        };
    }

    //
    private RefundPluginStatus toPluginStatus(String status) {
        if (status.equalsIgnoreCase("error")) {
            return RefundPluginStatus.ERROR;
        } else if (status.equalsIgnoreCase("processed")) {
            return RefundPluginStatus.PROCESSED;
        } else {
            return RefundPluginStatus.UNDEFINED;
        }
    }
}
