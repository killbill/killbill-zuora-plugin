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

package com.ning.killbill.zuora.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.log.LogService;
import org.skife.config.ConfigurationObjectFactory;

import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.billing.payment.plugin.api.PaymentPluginApi;
import com.ning.killbill.zuora.api.ZuoraPaymentPluginApi;
import com.ning.killbill.zuora.zuora.ConnectionFactory;
import com.ning.killbill.zuora.zuora.ConnectionPool;
import com.ning.killbill.zuora.zuora.ZuoraApi;
import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;

import com.google.common.collect.ImmutableMap;

public class ZuoraActivator implements BundleActivator {

    private final static String DEFAULT_INSTANCE_NAME = "default";
    private final static String PLUGIN_NAME = "zuora";

    private volatile ServiceRegistration paymentInfoPluginRegistration;

    private OSGIKillbill osgiKillbill;
    private volatile ServiceReference<OSGIKillbill> osgiKillbillReference;


    @Override
    public void start(final BundleContext context) throws Exception {
        final ZuoraPaymentPluginApi zuoraPaymentPluginApi = initializePlugin(DEFAULT_INSTANCE_NAME);
        fetchOSGIKIllbill(context);
        registerPaymentPluginApi(context, zuoraPaymentPluginApi);
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        releaseOSGIKIllbill(context);
        unregisterPlaymentPluginApi(context);
    }

    private final ZuoraConfig readConfigFromSystemProperties(final String instanceName) {
        final Properties props = System.getProperties();
        final ConfigurationObjectFactory factory = new ConfigurationObjectFactory(props);
        return factory.buildWithReplacements(ZuoraConfig.class,
                                                    ImmutableMap.of("pluginInstanceName", instanceName));
    }


    private final LogService getLogService() {
        return null;
    }

    private ZuoraPaymentPluginApi initializePlugin(final String pluginInstanceName) {

        final ZuoraConfig config = readConfigFromSystemProperties(pluginInstanceName);
        final LogService logService = getLogService();

        final ZuoraApi api = new ZuoraApi(config, logService);
        final ConnectionFactory factory = new ConnectionFactory(config, api, logService);
        final ConnectionPool pool = new ConnectionPool(factory, config);
        return  new ZuoraPaymentPluginApi(pool, api, logService, osgiKillbill, pluginInstanceName);
    }

    private void registerPaymentPluginApi(final BundleContext context, final PaymentPluginApi api) {
        final Dictionary props = new Hashtable();
        // TODO STEPH the 'name' should come from PaymentPluginApi so payment plugins know how to register their APIs
        props.put("name", PLUGIN_NAME);
        props.put("instance", DEFAULT_INSTANCE_NAME);

        this.paymentInfoPluginRegistration = context.registerService(PaymentPluginApi.class.getName(), api, props);
    }

    private void unregisterPlaymentPluginApi(final BundleContext context) {
        if (paymentInfoPluginRegistration != null) {
            paymentInfoPluginRegistration.unregister();
            paymentInfoPluginRegistration = null;
        }
    }


    private void fetchOSGIKIllbill(final BundleContext context) {
        this.osgiKillbillReference = (ServiceReference<OSGIKillbill>) context.getServiceReference(OSGIKillbill.class.getName());
        try {
            this.osgiKillbill = context.getService(osgiKillbillReference);
        } catch (Exception e) {
            System.err.println("Error in HelloActivator: " + e.getLocalizedMessage());
        }
    }

    private void releaseOSGIKIllbill(final BundleContext context) {
        if (osgiKillbillReference != null) {
            context.ungetService(osgiKillbillReference);
        }
    }
}
