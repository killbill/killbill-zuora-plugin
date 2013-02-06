package com.ning.killbill.zuora.zuora;

public class ZuoraError {
    public static final String ERROR_NOTFOUND = "not found";
    public static final String ERROR_UNKNOWN = "unknown";
    public static final String ERROR_ACCOUNT_NOT_FOUND = "no such account";
    public static final String ERROR_UNSUPPORTED = "unsupported";

    private final String type;
    private final String msg;

    public ZuoraError(String type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + msg;
    }
}
