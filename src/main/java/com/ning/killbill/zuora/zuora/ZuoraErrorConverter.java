package com.ning.killbill.zuora.zuora;

public class ZuoraErrorConverter implements Converter<ZuoraError, ZuoraError> {
    @Override
    public ZuoraError convert(ZuoraError original) {
        return original;
    }
}
