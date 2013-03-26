package com.ning.killbill.zuora.api;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.mockito.Mockito;
import org.osgi.service.log.LogService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.killbill.zuora.dao.MockZuoraPluginDao;
import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;
import com.ning.killbill.zuora.killbill.MockDefaultKillbillApi;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.MockZuoraApi;
import com.ning.killbill.zuora.zuora.ZuoraApi;

public class TestZuoraPaymentPluginApi {

    private final CallContext callContext = Mockito.mock(CallContext.class);

    private ZuoraPluginDao zuoraPluginDao;
    private ZuoraPaymentPluginApi zuoraPaymentPluginApi;
    private UUID kbAccountId;
    private String kbExternalKey;
    private UUID kbPaymentMethodId;

    @BeforeMethod(groups = "fast")
    public void setup() throws Exception {
        final ZuoraApi zuoraApi = new MockZuoraApi();
        zuoraPluginDao = new MockZuoraPluginDao();
        final MockDefaultKillbillApi defaultKillbillApi = new MockDefaultKillbillApi();
        zuoraPaymentPluginApi = new ZuoraPaymentPluginApi(Mockito.mock(ConnectionPool.class),
                                                          zuoraApi,
                                                          Mockito.mock(LogService.class),
                                                          defaultKillbillApi,
                                                          zuoraPluginDao,
                                                          "testing");

        // Create the account in Killbill...
        kbAccountId = UUID.randomUUID();
        kbExternalKey = UUID.randomUUID().toString();
        defaultKillbillApi.createKbAccount(kbAccountId, kbExternalKey);
        // ...and in Zuora
        final Account account = Mockito.mock(Account.class);
        Mockito.when(account.getId()).thenReturn(kbAccountId);
        Mockito.when(account.getExternalKey()).thenReturn(kbExternalKey);
        zuoraApi.createPaymentProviderAccount(null, account);

        // Create the payment method in Killbill
        kbPaymentMethodId = UUID.randomUUID();
        defaultKillbillApi.createKbPaymentMethodId(kbPaymentMethodId, kbExternalKey);
    }

    @Test(groups = "fast")
    public void testPaymentMethods() throws Exception {
        verifyPaymentMethods(0);

        // Not really used (only for kv properties in the real system)
        final PaymentMethodPlugin paymentMethodPlugin = new ZuoraPaymentMethodPlugin(null, false);

        // Create the payment method
        zuoraPaymentPluginApi.addPaymentMethod(kbAccountId, kbPaymentMethodId, paymentMethodPlugin, false, callContext);

        // Retrieve it
        final PaymentMethodPlugin retrievedPaymentMethodPlugin = zuoraPaymentPluginApi.getPaymentMethodDetail(kbAccountId, kbPaymentMethodId, callContext);
        Assert.assertNotNull(retrievedPaymentMethodPlugin);
        final String zuoraPaymentMethodId = retrievedPaymentMethodPlugin.getExternalPaymentMethodId();

        // Retrieve by account
        verifyPaymentMethods(kbPaymentMethodId, zuoraPaymentMethodId, false, 1);

        zuoraPaymentPluginApi.setDefaultPaymentMethod(kbAccountId, kbPaymentMethodId, callContext);
        verifyPaymentMethods(kbPaymentMethodId, zuoraPaymentMethodId, true, 1);

        zuoraPaymentPluginApi.deletePaymentMethod(kbAccountId, kbPaymentMethodId, callContext);
        verifyPaymentMethods(0);
    }

    private void verifyPaymentMethods(final int howMany) throws PaymentPluginApiException {
        verifyPaymentMethods(null, null, false, howMany);
    }

    private void verifyPaymentMethods(@Nullable final UUID kbPaymentMethodId, @Nullable final String zuoraPaymentMethodId,
                                      final boolean isDefault, final int howMany) throws PaymentPluginApiException {
        // Verify the Zuora state
        final List<PaymentMethodInfoPlugin> paymentMethodInfoPlugins = zuoraPaymentPluginApi.getPaymentMethods(kbAccountId, false, callContext);
        Assert.assertEquals(paymentMethodInfoPlugins.size(), howMany);

        if (howMany == 1) {
            final PaymentMethodInfoPlugin zePm = paymentMethodInfoPlugins.get(0);
            Assert.assertEquals(zePm.getAccountId(), kbAccountId);
            Assert.assertEquals(zePm.getPaymentMethodId(), kbPaymentMethodId);
            Assert.assertEquals(zePm.getExternalPaymentMethodId(), zuoraPaymentMethodId);
            Assert.assertEquals(zePm.isDefault(), isDefault);
        }

        // Verify the Killbill state
        final List<PaymentMethodEntity> paymentMethodEntities = zuoraPluginDao.getPaymentMethods(kbAccountId.toString());
        Assert.assertEquals(paymentMethodEntities.size(), howMany);

        if (howMany == 1) {
            final PaymentMethodEntity zePm = paymentMethodEntities.get(0);
            Assert.assertEquals(zePm.getKbAccountId(), kbAccountId.toString());
            Assert.assertEquals(zePm.getKbPaymentMethodId(), kbPaymentMethodId.toString());
            Assert.assertEquals(zePm.getZuoraPaymentMethodId(), zuoraPaymentMethodId);
            Assert.assertEquals(zePm.isDefault(), isDefault);
        }
    }
}
