package com.ning.killbill.zuora.http;

import org.joda.time.DateTime;

public class ZuoraPaymentJson {

    private DateTime effectiveDate;
    private String status;
    private String gatewayResponse;
    private String paymentMethodId;

    public ZuoraPaymentJson(final DateTime effectiveDate, final String status, final String gatewayResponse, final String paymentMethodId) {
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.gatewayResponse = gatewayResponse;
        this.paymentMethodId = paymentMethodId;
    }

    public DateTime getEffectiveDate() {
        return effectiveDate;
    }

    public String getStatus() {
        return status;
    }

    public String getGatewayResponse() {
        return gatewayResponse;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }
}
