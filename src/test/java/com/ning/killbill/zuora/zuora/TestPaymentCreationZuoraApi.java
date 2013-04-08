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
import java.math.RoundingMode;
import java.util.List;

import org.osgi.service.log.LogService;
import org.testng.annotations.Test;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.killbill.zuora.util.Either;

import com.zuora.api.object.Invoice;
import com.zuora.api.object.Payment;
import com.zuora.api.object.PaymentMethod;
import com.zuora.api.object.Subscription;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestPaymentCreationZuoraApi extends TestZuoraApiBase {


    @Test(groups = { "zuora"}, enabled=false)
    public void testFailedCreatePaypalPayment() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                logService.log(LogService.LOG_INFO,"Starting test testCreatePayment for account " + account.getId());

                PaymentMethodPlugin detail = createPaypalPaymentMethod(null, true);
                Either<ZuoraError, PaymentMethod> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                final BigDecimal paymentAmount = new BigDecimal(12.56).setScale(2, RoundingMode.HALF_EVEN);
                final String kbPaymentId =  KILLBILL_PAYMENT_ID;
                Either<ZuoraError, Payment> paymentOrError = zuoraApi.processPayment(connection, account.getAccountNumber(), paymentAmount, kbPaymentId);
                assertTrue(paymentOrError.isRight());

                Either<ZuoraError, List<Subscription>> subscriptionsOrError = zuoraApi.getSubscriptionsForAccount(connection, account.getId());
                assertTrue(paymentOrError.isRight());
                assertEquals(subscriptionsOrError.getRight().size(), 1);

                Either<ZuoraError, List<Invoice>> invoicesOrError = zuoraApi.getPostedInvoicesForAccount(connection, account.getId());
                assertTrue(invoicesOrError.isRight());
                assertEquals(invoicesOrError.getRight().size(), 1);

                // And then do it again-- we should see the same number of invoices and subscriptions on the account
                paymentOrError = zuoraApi.processPayment(connection, account.getAccountNumber(), paymentAmount, kbPaymentId);
                assertTrue(paymentOrError.isLeft());

                subscriptionsOrError = zuoraApi.getSubscriptionsForAccount(connection, account.getId());
                assertTrue(subscriptionsOrError.isRight());
                assertEquals(subscriptionsOrError.getRight().size(), 1);

                invoicesOrError = zuoraApi.getPostedInvoicesForAccount(connection, account.getId());
                assertTrue(invoicesOrError.isRight());
                assertEquals(invoicesOrError.getRight().size(), 1);
                return null;
            }
        });
    }

    @Test(groups = { "zuora"})
    public void testCreateSuccessCCPayment() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                logService.log(LogService.LOG_INFO,"Starting test testCreatePayment for account " + account.getId());

                PaymentMethodPlugin detail = createCreditCardPaymentMethod(null, true, "2015-07");
                Either<ZuoraError, PaymentMethod> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                final BigDecimal paymentAmount = new BigDecimal(12.56).setScale(2, RoundingMode.HALF_EVEN);
                final String kbPaymentId =  KILLBILL_PAYMENT_ID;
                Either<ZuoraError, Payment> paymentOrError = zuoraApi.processPayment(connection, account.getAccountNumber(), paymentAmount, kbPaymentId);
                assertTrue(paymentOrError.isRight());

                Either<ZuoraError, List<Subscription>> subscriptionsOrError = zuoraApi.getSubscriptionsForAccount(connection, account.getId());
                assertTrue(paymentOrError.isRight());
                assertEquals(subscriptionsOrError.getRight().size(), 1);

                Either<ZuoraError, List<Invoice>> invoicesOrError = zuoraApi.getPostedInvoicesForAccount(connection, account.getId());
                assertTrue(invoicesOrError.isRight());
                assertEquals(invoicesOrError.getRight().size(), 1);

                return null;
            }
        });
    }



}
