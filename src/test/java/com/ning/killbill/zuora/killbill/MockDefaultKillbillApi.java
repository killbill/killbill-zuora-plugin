package com.ning.killbill.zuora.killbill;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mockito.Mockito;
import org.osgi.service.log.LogService;

import com.ning.billing.account.api.Account;
import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.TenantContext;

public class MockDefaultKillbillApi extends DefaultKillbillApi {

    private final Map<UUID, String> paymentMethodMapping = new HashMap<UUID, String>();
    private final Map<UUID, String> paymentMapping = new HashMap<UUID, String>();
    private final Map<UUID, String> accountMapping = new HashMap<UUID, String>();

    public MockDefaultKillbillApi() {
        super(Mockito.mock(OSGIKillbill.class), Mockito.mock(LogService.class));
    }

    @Override
    public String getAccountExternalKeyFromPaymentMethodId(final UUID kbPaymentMethodId, final TenantContext tenantContext) throws PaymentPluginApiException {
        return paymentMethodMapping.get(kbPaymentMethodId);
    }

    @Override
    public String getAccountExternalKeyFromPaymentId(final UUID kbPaymentId, final TenantContext tenantContext) throws PaymentPluginApiException {
        return paymentMapping.get(kbPaymentId);
    }

    @Override
    public String getAccountExternalKeyFromAccountId(final UUID kbAccountId, final TenantContext tenantContext) throws PaymentPluginApiException {
        return accountMapping.get(kbAccountId);
    }

    @Override
    public Account getAccountFromId(final UUID kbAccountId, final TenantContext tenantContext) throws PaymentPluginApiException {
        throw new UnsupportedOperationException();
    }

    // Testing only

    public void createKbAccount(final UUID kbAccountId, final String kbExternalKey) {
        accountMapping.put(kbAccountId, kbExternalKey);
    }

    public void createKbPaymentMethodId(final UUID kbPaymentMethodId, final String kbExternalKey) {
        paymentMethodMapping.put(kbPaymentMethodId, kbExternalKey);
    }
}
