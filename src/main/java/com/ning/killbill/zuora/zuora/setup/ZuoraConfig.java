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

package com.ning.killbill.zuora.zuora.setup;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public interface ZuoraConfig {
    
    static final String ZuoraConfigPropertyPrefix = "killbill.zuora.config.";

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.useJPADAO")
    @Default("false")
    public boolean useJPADAOImplementation();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.userName")
    @Default("foo")
    public String getZuoraUserName();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.password")
    @Default("bar")
    public String getZuoraPassword();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.url")
    @Default("https://apisandbox.zuora.com/apps/services/a/27.0")
    public String getZuoraApiUrl();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.propertyFileLocation")
    @DefaultNull
    public String getZuoraPropertyFileLocation();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.ratePlanChargeName")
    @Default("ning-killbill2-onetime")
    public String getRatePlanChargeName();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.pool.maxIdle")
    @Default("8")
    public int getPoolMaxIdle();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.pool.minIdle")
    @Default("0")
    public int getPoolMinIdle();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.pool.maxActive")
    @Default("8")
    public int getPoolMaxActive();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.maxLoginRetries")
    @Default("3")
    public int getMaxLoginRetries();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.isOverrideCreditCardGateway")
    @Default("false")
    public boolean isOverrideCreditcardGateway();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.overrideCreditCardGateway")
    @Default("")
    public String getOverrideCreditcardGateway();

    @Config(ZuoraConfigPropertyPrefix + "${pluginInstanceName}.shouldCheckForStatePayment")
    @Default("true")
    public boolean shouldCheckForStatePayment();


}
