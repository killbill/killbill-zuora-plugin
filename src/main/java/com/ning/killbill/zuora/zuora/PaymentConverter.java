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
            public String getFirstPaymentReferenceId() {
                return payment.getReferenceId();
            }

            @Override
            public String getSecondPaymentReferenceId() {
                return payment.getSecondPaymentReferenceId();
            }
        };
    }

    //
    // From zuora doc:
    // Draft, Processing, Processed, Error, Voided
    //
    public static PaymentPluginStatus toPluginStatus(String status) {
        if (status.equalsIgnoreCase("error")) {
            return PaymentPluginStatus.ERROR;
        } else if (status.equalsIgnoreCase("processed")) {
            return PaymentPluginStatus.PROCESSED;
        } else {
            return PaymentPluginStatus.UNDEFINED;
        }
    }

}
