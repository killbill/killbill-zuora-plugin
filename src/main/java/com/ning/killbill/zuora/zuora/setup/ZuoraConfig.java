package com.ning.killbill.zuora.zuora.setup;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public interface ZuoraConfig {

    @Config("killbill.payment.provider.${pluginInstanceName}.userName")
    @Default("foo")
    public String getZuoraUserName();

    @Config("killbill.payment.provider.${pluginInstanceName}.password")
    @Default("bar")
    public String getZuoraPassword();

    @Config("killbill.payment.provider.${pluginInstanceName}.url")
    @Default("https://apisandbox.zuora.com/apps/services/a/27.0")
    public String getZuoraApiUrl();

    @Config("killbill.payment.provider.${pluginInstanceName}.propertyFileLocation")
    @DefaultNull
    public String getZuoraPropertyFileLocation();

    @Config("killbill.payment.provider.${pluginInstanceName}.ratePlanChargeName")
    @Default("ning-killbill2-onetime")
    public String getRatePlanChargeName();

    @Config("killbill.payment.provider.${pluginInstanceName}.pool.maxIdle")
    @Default("8")
    public int getPoolMaxIdle();

    @Config("killbill.payment.provider.${pluginInstanceName}.pool.minIdle")
    @Default("0")
    public int getPoolMinIdle();

    @Config("killbill.payment.provider.${pluginInstanceName}.pool.maxActive")
    @Default("8")
    public int getPoolMaxActive();

    @Config("killbill.payment.provider.${pluginInstanceName}.maxLoginRetries")
    @Default("3")
    public int getMaxLoginRetries();

    @Config("killbill.payment.provider.${pluginInstanceName}.isOverrideCreditCardGateway")
    @Default("false")
    public boolean isOverrideCreditcardGateway();

    @Config("killbill.payment.provider.${pluginInstanceName}.overrideCreditCardGateway")
    @Default("")
    public String getOverrideCreditcardGateway();

    @Config("killbill.payment.provider.${pluginInstanceName}.shouldCheckForStatePayment")
    @Default("true")
    public boolean shouldCheckForStatePayment();

}
