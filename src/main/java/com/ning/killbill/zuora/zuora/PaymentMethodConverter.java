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

import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.method.PaypalProperties;
import com.ning.killbill.zuora.api.ZuoraPaymentMethodPlugin;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.zuora.api.object.PaymentMethod;

public class PaymentMethodConverter implements Converter<PaymentMethod, PaymentMethodPlugin> {
    // Need account to find out if the payment method is the default one
    private final com.zuora.api.object.Account account;

    public PaymentMethodConverter(com.zuora.api.object.Account account) {
        this.account = account;
    }

    private boolean isCreditCard(PaymentMethod paymentMethod) {
        return paymentMethod.getCreditCardMaskNumber() != null;
    }

    private boolean isPaypal(PaymentMethod paymentMethod) {
        return paymentMethod.getPaypalBaid() != null;
    }

    @Override
    public PaymentMethodPlugin convert(PaymentMethod paymentMethod) {
        
        final boolean isDefault = paymentMethod.getId().equals(account.getDefaultPaymentMethodId());
        ZuoraPaymentMethodPlugin result = new ZuoraPaymentMethodPlugin(paymentMethod.getId(), isDefault);
        result.addProperty(PaymentMethodProperties.ACCOUNT_ID, paymentMethod.getAccountId(), false);
        if (isPaypal(paymentMethod)) {
            result.addProperty(PaypalProperties.TYPE, PaypalProperties.TYPE_VALUE, false);
            result.addProperty(PaypalProperties.EMAIL, paymentMethod.getPaypalEmail(), false);
            result.addProperty(PaypalProperties.BAID, paymentMethod.getPaypalBaid(), false);  

        } else if (isCreditCard(paymentMethod)) {
            
            result.addProperty(CreditCardProperties.TYPE, CreditCardProperties.TYPE_VALUE, false);            
            result.addProperty(CreditCardProperties.CARD_HOLDER_NAME, paymentMethod.getCreditCardHolderName(), true);
            result.addProperty(CreditCardProperties.CARD_TYPE, paymentMethod.getCreditCardType(), false);  
            result.addProperty(CreditCardProperties.EXPIRATION_DATE,
                    paymentMethod.getCreditCardExpirationYear()
                    + "-" 
                    + (paymentMethod.getCreditCardExpirationMonth() < 10 ? "0" : "") 
                    + paymentMethod.getCreditCardExpirationMonth(),
                    true);  
            result.addProperty(CreditCardProperties.MASK_NUMBER, paymentMethod.getCreditCardMaskNumber(), false);
            result.addProperty(CreditCardProperties.ADDRESS1, paymentMethod.getCreditCardAddress1(), false);            
            result.addProperty(CreditCardProperties.ADDRESS2, paymentMethod.getCreditCardAddress2(), false);
            result.addProperty(CreditCardProperties.CITY, paymentMethod.getCreditCardCity(), false);            
            result.addProperty(CreditCardProperties.POSTAL_CODE, paymentMethod.getCreditCardPostalCode(), false);                        
            result.addProperty(CreditCardProperties.STATE, paymentMethod.getCreditCardState(), false);                                    
            result.addProperty(CreditCardProperties.COUNTRY, paymentMethod.getCreditCardCountry(), false);
        }
        else {
            result.addProperty("type", paymentMethod.getType(), false);            
        }
        return result;
    }
}
