package com.ning.killbill.zuora.zuora;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;

public class LogServiceTest implements LogService {

    private final Logger logger;

    public LogServiceTest(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(final int i, final String s) {
        switch (i) {
            case LOG_INFO:
                logger.info(s);
                break;
            case LOG_WARNING:
                logger.warn(s);
                break;
            case LOG_ERROR:
                logger.error(s);
                break;
            case LOG_DEBUG:
                logger.debug(s);
                break;
            default:
                throw new UnsupportedOperationException("Unknow log level = " + i);
        }
    }

    @Override
    public void log(final int i, final String s, final Throwable throwable) {
        switch (i) {
            case LOG_INFO:
                logger.info(s, throwable);
                break;
            case LOG_WARNING:
                logger.warn(s, throwable);
                break;
            case LOG_ERROR:
                logger.error(s, throwable);
                break;
            case LOG_DEBUG:
                logger.debug(s, throwable);
                break;
            default:
                throw new UnsupportedOperationException("Unknow log level = " + i);
        }
    }

    @Override
    public void log(final ServiceReference serviceReference, final int i, final String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(final ServiceReference serviceReference, final int i, final String s, final Throwable throwable) {
        throw new UnsupportedOperationException();
    }
}
