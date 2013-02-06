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

import org.apache.commons.pool.PoolableObjectFactory;
import org.osgi.service.log.LogService;

import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;

public class ConnectionFactory implements PoolableObjectFactory {

    private final ZuoraConfig zuoraConfig;
    private final ZuoraApi api;
    private final LogService logService;

    public ConnectionFactory(ZuoraConfig zuoraConfig, ZuoraApi api, final LogService logService) {
        this.zuoraConfig = zuoraConfig;
        this.api = api;
        this.logService = logService;
    }

    @Override
    public Object makeObject() throws Exception {
        return new ZuoraConnection(zuoraConfig);
    }

    @Override
    public void destroyObject(Object obj) throws Exception {
        // nothing to be done
    }

    @Override
    public boolean validateObject(Object obj) {
        ZuoraConnection connection = (ZuoraConnection) obj;

        try {
            return api.loadRatePlanCharge(connection).isRight();
        } catch (Exception ex) {
            logService.log(LogService.LOG_WARNING, "Validation of a zuora connection failed", ex);
            return false;
        }
    }

    @Override
    public void activateObject(Object obj) throws Exception {
        // nothing to be done
    }

    @Override
    public void passivateObject(Object obj) throws Exception {
        // nothing to be done
    }
}
