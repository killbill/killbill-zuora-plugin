package com.ning.killbill.zuora.zuora.setup;

import org.osgi.service.log.LogService;

import com.ning.killbill.zuora.api.ZuoraPaymentPluginApi;
import com.ning.killbill.zuora.zuora.ConnectionFactory;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.ZuoraApi;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ning.billing.payment.provider.PaymentProviderPluginRegistry;

public class ZuoraPaymentProviderPluginProvider implements Provider<ZuoraPaymentPluginApi> {
    private PaymentProviderPluginRegistry registry;
    private final String instanceName;
    private final ZuoraConfig config;
    private final LogService logService;

    public ZuoraPaymentProviderPluginProvider(String instanceName, ZuoraConfig config, LogService logService) {
        this.instanceName = instanceName;
        this.config = config;
        this.logService = logService;
    }

    @Inject
    public void setPaymentProviderPluginRegistry(PaymentProviderPluginRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ZuoraPaymentPluginApi get() {
        ZuoraApi api = new ZuoraApi(config, logService);
        ConnectionFactory factory = new ConnectionFactory(config, api, logService);
        ConnectionPool pool = new ConnectionPool(factory, config);
        ZuoraPaymentPluginApi plugin = new ZuoraPaymentPluginApi(pool, api, logService, instanceName);

        registry.register(plugin, instanceName);
        return plugin;
    }
}
