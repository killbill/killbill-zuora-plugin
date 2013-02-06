package com.ning.killbill.zuora.zuora;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.ning.killbill.zuora.util.Either;


import org.osgi.service.log.LogService;
import org.testng.annotations.Test;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.zuora.api.object.Invoice;
import com.zuora.api.object.Payment;
import com.zuora.api.object.Subscription;

public class TestPaymentCreationZuoraApi extends TestZuoraApiBase {


    @Test(groups = { "zuora"}, enabled=true)
    public void testFailedCreatePaypalPayment() {

        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {

                logService.log(LogService.LOG_INFO,"Starting test testCreatePayment for account " + account.getId());

                PaymentMethodPlugin detail = createPaypalPaymentMethod(null, true);
                Either<ZuoraError, String> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
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
                Either<ZuoraError, String> resultAddPm = zuoraApi.addPaymentMethod(connection, EXTERNAL_NAME, detail, true);
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
