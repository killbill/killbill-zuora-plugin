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
