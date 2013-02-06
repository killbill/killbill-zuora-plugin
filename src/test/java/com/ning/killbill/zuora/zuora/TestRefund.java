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

import java.math.BigDecimal;
import java.util.List;

import com.ning.killbill.zuora.util.Either;

import org.osgi.service.log.LogService;
import org.testng.annotations.Test;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.zuora.api.object.Account;
import com.zuora.api.object.Invoice;
import com.zuora.api.object.Payment;
import com.zuora.api.object.RefundInvoicePayment;
import com.zuora.api.object.Subscription;

public class TestRefund extends TestZuoraApiBase {



    @Test(groups = { "zuora"})
    public void testRefund() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                logService.log(LogService.LOG_INFO,"Starting test testCreatePayment for account " + account.getId());

                PaymentMethodPlugin detail = createCreditCardPaymentMethod(null, true, "2015-07");
                Either<ZuoraError, String> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
                assertTrue(resultAddPm.isRight());

                final BigDecimal paymentAmount = new BigDecimal(12.56);
                final String kbPaymentId =  KILLBILL_PAYMENT_ID;
                Either<ZuoraError, Payment> paymentOrError = zuoraApi.processPayment(connection, account.getAccountNumber(), paymentAmount, kbPaymentId);
                assertTrue(paymentOrError.isRight());

                Either<ZuoraError, List<Subscription>> subscriptionsOrError = zuoraApi.getSubscriptionsForAccount(connection, account.getId());
                assertTrue(paymentOrError.isRight());
                assertEquals(subscriptionsOrError.getRight().size(), 1);

                Either<ZuoraError, List<Invoice>> invoicesOrError = zuoraApi.getPostedInvoicesForAccount(connection, account.getId());
                assertTrue(invoicesOrError.isRight());
                assertEquals(invoicesOrError.getRight().size(), 1);

                final String paymentId = paymentOrError.getRight().getId();

                Either<ZuoraError, List<RefundInvoicePayment>>  invPayOrError = zuoraApi.getRefundsForPayment(connection, paymentId);
                assertTrue(invPayOrError.isRight());
                assertEquals(invPayOrError.getRight().size(), 0);
                for (RefundInvoicePayment cur : invPayOrError.getRight()) {
                    logService.log(LogService.LOG_INFO,"refund amount = " + cur.getRefundAmount());
                }

                //Either<ZuoraError, Payment> fetchedPaymentOrError = getPayment(connection, account.getId(), paymentId);
                //assertTrue(fetchedPaymentOrError.isRight());

                //final Payment fetchedPayment = fetchedPaymentOrError.getRight();

                BigDecimal refundAmount = new BigDecimal(5.00);
                Either<ZuoraError, Void> createRefundOrError = zuoraApi.createRefund(connection, paymentId, kbPaymentId, refundAmount);
                assertTrue(createRefundOrError.isRight());

                invPayOrError = zuoraApi.getRefundsForPayment(connection, paymentId);
                assertTrue(invPayOrError.isRight());
                assertEquals(invPayOrError.getRight().size(), 1);
                for (RefundInvoicePayment cur : invPayOrError.getRight()) {
                    logService.log(LogService.LOG_INFO,"refund amount = " + cur.getRefundAmount());
                }

                createRefundOrError = zuoraApi.createRefund(connection, paymentId, kbPaymentId, refundAmount);
                assertTrue(createRefundOrError.isRight());

                invPayOrError = zuoraApi.getRefundsForPayment(connection, paymentId);
                assertTrue(invPayOrError.isRight());
                assertEquals(invPayOrError.getRight().size(), 2);
                for (RefundInvoicePayment cur : invPayOrError.getRight()) {
                    logService.log(LogService.LOG_INFO,"refund amount = " + cur.getRefundAmount());
                }

                refundAmount = new BigDecimal(2.56);
                createRefundOrError = zuoraApi.createRefund(connection, paymentId, kbPaymentId, refundAmount);
                assertTrue(createRefundOrError.isRight());

                invPayOrError = zuoraApi.getRefundsForPayment(connection, paymentId);
                assertEquals(invPayOrError.getRight().size(), 3);
                for (RefundInvoicePayment cur : invPayOrError.getRight()) {
                    logService.log(LogService.LOG_INFO,"refund amount = " + cur.getRefundAmount());
                }

                refundAmount = new BigDecimal(10.00);
                createRefundOrError = zuoraApi.createRefund(connection, paymentId, kbPaymentId, refundAmount);
                assertTrue(createRefundOrError.isLeft());

                Either<ZuoraError, Account> accountOrError = zuoraApi.getAccountById(connection, account.getId());
                assertTrue(accountOrError.isRight());

                assertTrue(accountOrError.getRight().getBalance().compareTo(BigDecimal.ZERO) == 0);
                return null;
            }
        });
    }
}
