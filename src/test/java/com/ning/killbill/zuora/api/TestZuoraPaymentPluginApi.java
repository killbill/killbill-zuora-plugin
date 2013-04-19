package com.ning.killbill.zuora.api;

import java.math.BigDecimal;
import java.util.UUID;

import javax.sql.DataSource;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.billing.account.api.Account;
import com.ning.billing.account.api.AccountApiException;
import com.ning.billing.account.api.AccountUserApi;
import com.ning.billing.catalog.api.Currency;
import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.billing.payment.api.PaymentApi;
import com.ning.billing.payment.api.PaymentApiException;
import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentInfoPlugin;
import com.ning.billing.util.callcontext.TenantContext;
import com.ning.killbill.zuora.dao.TestZuoraPluginDao;
import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.dao.dbi.JDBIZuoraPluginDao;
import com.ning.killbill.zuora.killbill.DefaultKillbillApi;
import com.ning.killbill.zuora.osgi.ZuoraActivator;
import com.ning.killbill.zuora.zuora.TestZuoraApiBase;

public class TestZuoraPaymentPluginApi extends TestZuoraApiBase {


    private final static UUID PAYMENT_METHOD_ID = UUID.fromString("7a01e4ea-dd5b-424c-ae65-f977735144a6");
    private final static UUID PAYMENT_ID = UUID.fromString("ed3357f9-dc9e-4d12-b46a-dc2395f6e9db");

    private ZuoraPaymentPluginApi zuoraPaymentPluginApi;
    private DataSource dataSource;
    private ZuoraPluginDao zuoraPluginDao;


    @BeforeClass(groups = {"zuora"}, enabled = true)
    public void setup() {
        try {
            super.setup();
            dataSource = TestZuoraPluginDao.getC3P0DataSource();
            zuoraPluginDao = new JDBIZuoraPluginDao(dataSource);
            zuoraPaymentPluginApi = new ZuoraPaymentPluginApi(pool, zuoraApi, logService, getKillbillApi(), zuoraPluginDao, ZuoraActivator.PLUGIN_NAME);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @BeforeMethod(groups = {"zuora"}, enabled = true)
    public void setupMethod() throws Exception {
        TestZuoraPluginDao.cleanupTables(dataSource);
        super.setupMethod();
        PaymentMethodPlugin detail = createPaypalPaymentMethod(null, true);
        zuoraPaymentPluginApi.addPaymentMethod(ACCOUNT_ID, PAYMENT_METHOD_ID, detail, true, null);

    }

    @Test(groups = {"zuora"}, enabled = true)
    public void testPaymentApi() throws Exception {

        final PaymentInfoPlugin paymentInfo = zuoraPaymentPluginApi.processPayment(ACCOUNT_ID, PAYMENT_ID, PAYMENT_METHOD_ID, new BigDecimal("12.76"), Currency.GBP, null);
        Assert.assertEquals(paymentInfo.getAmount().compareTo(new BigDecimal("12.76")), 0);
        Assert.assertEquals(paymentInfo.getGatewayError(), "Approved");
        Assert.assertEquals(paymentInfo.getGatewayErrorCode(), "0");


        // Now retrieve should come from the database
        Assert.assertNotNull(zuoraPluginDao.getPayment(PAYMENT_ID.toString()));

        final PaymentInfoPlugin paymentInfo2 = zuoraPaymentPluginApi.getPaymentInfo(ACCOUNT_ID, PAYMENT_ID, null);
        Assert.assertEquals(paymentInfo2.getAmount().compareTo(paymentInfo.getAmount()), 0);
        Assert.assertEquals(paymentInfo2.getGatewayError(), paymentInfo.getGatewayError());
        Assert.assertEquals(paymentInfo2.getGatewayErrorCode(), paymentInfo.getGatewayErrorCode());
        Assert.assertEquals(paymentInfo2.getFirstPaymentReferenceId(), paymentInfo.getFirstPaymentReferenceId());
        Assert.assertEquals(paymentInfo2.getSecondPaymentReferenceId(), paymentInfo.getSecondPaymentReferenceId());
        //Assert.assertEquals(paymentInfo2.getCreatedDate().compareTo(paymentInfo.getCreatedDate()), 0);
        //Assert.assertEquals(paymentInfo2.getEffectiveDate().compareTo(paymentInfo.getEffectiveDate()), 0);

        //
        // Now delete entry from DB and fetch payment again-- should retrieve it from zuora
        //
        TestZuoraPluginDao.cleanupTables(dataSource);

        Assert.assertNull(zuoraPluginDao.getPayment(PAYMENT_ID.toString()));

        final PaymentInfoPlugin paymentInfo3 = zuoraPaymentPluginApi.getPaymentInfo(ACCOUNT_ID, PAYMENT_ID, null);
        Assert.assertEquals(paymentInfo3.getAmount().compareTo(paymentInfo.getAmount()), 0);
        Assert.assertEquals(paymentInfo3.getGatewayError(), paymentInfo.getGatewayError());
        Assert.assertEquals(paymentInfo3.getGatewayErrorCode(), paymentInfo.getGatewayErrorCode());
        Assert.assertEquals(paymentInfo3.getFirstPaymentReferenceId(), paymentInfo.getFirstPaymentReferenceId());
        Assert.assertEquals(paymentInfo3.getSecondPaymentReferenceId(), paymentInfo.getSecondPaymentReferenceId());
        //Assert.assertEquals(paymentInfo3.getCreatedDate().compareTo(paymentInfo.getCreatedDate()), 0);
        //Assert.assertEquals(paymentInfo3.getEffectiveDate().compareTo(paymentInfo.getEffectiveDate()), 0);

        Assert.assertNotNull(zuoraPluginDao.getPayment(PAYMENT_ID.toString()));

    }


    private DefaultKillbillApi getKillbillApi() throws AccountApiException, PaymentApiException {

        final Account account = Mockito.mock(Account.class);
        Mockito.when(account.getExternalKey()).thenReturn(EXTERNAL_NAME);

        final PaymentMethod paymentMethod = Mockito.mock(PaymentMethod.class);
        Mockito.when(paymentMethod.getAccountId()).thenReturn(ACCOUNT_ID);

        final AccountUserApi accountUserApi = Mockito.mock(AccountUserApi.class);
        Mockito.when(accountUserApi.getAccountById(Mockito.eq(ACCOUNT_ID), Mockito.<TenantContext>any())).thenReturn(account);

        final PaymentApi paymentApi = Mockito.mock(PaymentApi.class);
        Mockito.when(paymentApi.getPaymentMethodById(Mockito.eq(PAYMENT_METHOD_ID), Mockito.eq(false), Mockito.eq(false), Mockito.<TenantContext>any())).thenReturn(paymentMethod);


        final OSGIKillbill osgiKillbill = Mockito.mock(OSGIKillbill.class);
        Mockito.when(osgiKillbill.getPaymentApi()).thenReturn(paymentApi);
        Mockito.when(osgiKillbill.getAccountUserApi()).thenReturn(accountUserApi);

        return new DefaultKillbillApi(osgiKillbill, logService);
    }

}
