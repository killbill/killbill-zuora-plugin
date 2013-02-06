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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.method.PaypalProperties;
import com.ning.killbill.zuora.util.Either;

import org.testng.annotations.Test;

import com.ning.billing.payment.api.PaymentMethodPlugin;

import com.zuora.api.object.Account;
import com.zuora.api.object.PaymentMethod;


public class TestPaymentMethodsZuoraApi extends TestZuoraApiBase {


    @Test(groups = { "zuora"})
    public void testDeleteDefaultPaymentMethod() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                PaymentMethodPlugin detail = createCreditCardPaymentMethod(null, true, "2015-07");
                Either<ZuoraError, String> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                // Fetch account again
                Either<ZuoraError, Account> resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                assertTrue(resultGetSnAccount.isRight());

                String paymentMethodId = resultAddPm.getRight();

                zuoraApi.deletePaymentMethod(connection, EXTERNAL_NAME, paymentMethodId);

                Either<ZuoraError, List<PaymentMethod>> resultGetPms = zuoraApi.getPaymentMethodsForAccount(connection, resultGetSnAccount.getRight());
                assertTrue(resultGetPms.isRight());
                assertEquals(resultGetPms.getRight().size(), 0);
                return null;
            }
        });
    }


    @Test(groups = { "zuora"})
    public void testPaymentMethods() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                PaymentMethodPlugin detail = createPaypalPaymentMethod(null, true);
                Either<ZuoraError, String> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                // Fetch account again
                Either<ZuoraError, Account> resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                assertTrue(resultGetSnAccount.isRight());

                Account updatedAccount = resultGetSnAccount.getRight();
                PaymentMethodConverter converter = new PaymentMethodConverter(updatedAccount);


                String paymentMethodId = resultAddPm.getRight();
                final String paypalMethodId = paymentMethodId;

                Either<ZuoraError, PaymentMethod> resultGetPm = zuoraApi.getPaymentMethodById(connection, paymentMethodId);
                assertTrue(resultGetPm.isRight());
                PaymentMethodPlugin paymentMethod = converter.convert(resultGetPm.getRight());
                assertEquals(paymentMethod.getValueString(PaymentMethodProperties.TYPE), PaypalProperties.TYPE_VALUE);
                assertEquals(paymentMethod.getValueString(PaypalProperties.EMAIL), PAYPAL_EMAIL);
                assertEquals(paymentMethod.getValueString(PaypalProperties.BAID), PAYPAL_BAID);
                assertTrue(paymentMethod.isDefaultPaymentMethod());
                assertEquals(updatedAccount.getDefaultPaymentMethodId(), resultGetPm.getRight().getId());
                assertEquals(updatedAccount.getPaymentGateway(), "PAYPAL");

                // ADD CREDIT CARD AND CHANGE DEFAULT
                detail = createCreditCardPaymentMethod(null, true, "2015-07");
                resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                // Fetch account again
                resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                assertTrue(resultGetSnAccount.isRight());

                updatedAccount = resultGetSnAccount.getRight();
                converter = new PaymentMethodConverter(updatedAccount);

                paymentMethodId = resultAddPm.getRight();
                final String creditCardMethodId = paymentMethodId;


                resultGetPm = zuoraApi.getPaymentMethodById(connection, paymentMethodId);
                assertTrue(resultGetPm.isRight());
                paymentMethod = converter.convert(resultGetPm.getRight());
                assertEquals(paymentMethod.getValueString(PaymentMethodProperties.TYPE), CreditCardProperties.TYPE_VALUE);
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CARD_HOLDER_NAME), "booboo");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.ADDRESS1), "12 peralta ave");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.ADDRESS2), "suite 300");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CITY), "San Francisco");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.COUNTRY), "United States");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.POSTAL_CODE), "94110");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.EXPIRATION_DATE), "2015-07");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.STATE), "California");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CARD_TYPE), "Visa");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.MASK_NUMBER), "************1111");
                assertTrue(paymentMethod.isDefaultPaymentMethod());
                assertEquals(updatedAccount.getDefaultPaymentMethodId(), resultGetPm.getRight().getId());
                assertEquals(updatedAccount.getPaymentGateway(), "USD");

                // CHANGE DEFAULT BACK TO PAYPAL
                Either<ZuoraError, PaymentMethod> tmpPaymentMethodOrError = zuoraApi.getPaymentMethodById(connection, paypalMethodId);
                assertTrue(tmpPaymentMethodOrError.isRight());

                zuoraApi.setDefaultPaymentMethod(connection, EXTERNAL_NAME, tmpPaymentMethodOrError.getRight());

                resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                assertTrue(resultGetSnAccount.isRight());
                assertEquals(resultGetSnAccount.getRight().getDefaultPaymentMethodId(), paypalMethodId);
                assertEquals(resultGetSnAccount.getRight().getPaymentGateway(), "PAYPAL");

                // CHANGE DEFAULT BACK TO CREDIT CARD AGAIN
                tmpPaymentMethodOrError = zuoraApi.getPaymentMethodById(connection, creditCardMethodId);
                assertTrue(tmpPaymentMethodOrError.isRight());

                zuoraApi.setDefaultPaymentMethod(connection, EXTERNAL_NAME, tmpPaymentMethodOrError.getRight());

                resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                assertTrue(resultGetSnAccount.isRight());
                assertEquals(resultGetSnAccount.getRight().getDefaultPaymentMethodId(), creditCardMethodId);
                assertEquals(resultGetSnAccount.getRight().getPaymentGateway(), "USD");

                // UPDATE CREDIT CARD
                detail = createCreditCardPaymentMethod(creditCardMethodId, false, "2015-09");
                zuoraApi.updateCreditCardPaymentMethod(connection, updatedAccount, detail);
                resultGetPm = zuoraApi.getPaymentMethodById(connection, paymentMethodId);
                assertTrue(resultGetPm.isRight());
                paymentMethod = converter.convert(resultGetPm.getRight());
                assertEquals(paymentMethod.getValueString(PaymentMethodProperties.TYPE), CreditCardProperties.TYPE_VALUE);
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CARD_HOLDER_NAME), "booboo");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.ADDRESS1), "12 peralta ave");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.ADDRESS2), "suite 300");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CITY), "San Francisco");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.COUNTRY), "United States");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.POSTAL_CODE), "94110");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.EXPIRATION_DATE), "2015-09");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.STATE), "California");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.CARD_TYPE), "Visa");
                assertEquals(paymentMethod.getValueString(CreditCardProperties.MASK_NUMBER), "************1111");
                assertTrue(paymentMethod.isDefaultPaymentMethod());
                assertEquals(updatedAccount.getDefaultPaymentMethodId(), resultGetPm.getRight().getId());
                assertEquals(updatedAccount.getPaymentGateway(), "USD");

                Either<ZuoraError,List<PaymentMethod>> paymentMethodsOrError =  zuoraApi.getPaymentMethodsForAccount(connection, resultGetSnAccount.getRight());
                assertTrue(paymentMethodsOrError.isRight());
                assertEquals(paymentMethodsOrError.getRight().size(), 2);
                return null;
            }
        });
    }
 }
