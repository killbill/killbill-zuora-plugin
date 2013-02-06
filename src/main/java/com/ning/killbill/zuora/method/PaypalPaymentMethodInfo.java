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

package com.ning.killbill.zuora.method;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

public final class PaypalPaymentMethodInfo extends PaymentMethodInfo {

    public static final String TYPE = "PayPal";

    public static final class Builder extends BuilderBase<PaypalPaymentMethodInfo, Builder> {
        private String baid;
        private String email;

        public Builder() {
            super(Builder.class);
        }

        public Builder(PaypalPaymentMethodInfo src) {
            super(Builder.class, src);
            this.baid = src.baid;
            this.email = src.email;
        }

        public Builder setBaid(String baid) {
            this.baid = baid;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public PaypalPaymentMethodInfo build() {
            return new PaypalPaymentMethodInfo(id, accountId, defaultMethod, baid, email);
        }
    }

    private final String baid;
    private final String email;

    @JsonCreator
    public PaypalPaymentMethodInfo(@JsonProperty("id") String id,
                                   @JsonProperty("accountId") String accountId,
                                   @JsonProperty("defaultMethod") Boolean defaultMethod,
                                   @JsonProperty("baid") String baid,
                                   @JsonProperty("email") String email) {
        super(id, accountId, defaultMethod, TYPE);

        if (Strings.isNullOrEmpty(baid) || Strings.isNullOrEmpty(email)) {
            throw new IllegalArgumentException("baid and email should be present");
        }

        this.baid = baid;
        this.email = email;
    }

    public String getBaid() {
        return baid;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "PaypalPaymentMethodInfo [baid=" + baid + ", email=" + email + "]";
    }

}
