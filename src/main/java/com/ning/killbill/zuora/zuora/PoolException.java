package com.ning.killbill.zuora.zuora;

public class PoolException extends RuntimeException {
    public PoolException() {
        super();
    }

    public PoolException(String msg, Throwable baseEx) {
        super(msg, baseEx);
    }

    public PoolException(String msg) {
        super(msg);
    }

    public PoolException(Throwable baseEx) {
        super(baseEx);
    }
}
