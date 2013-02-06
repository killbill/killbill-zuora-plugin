package com.ning.killbill.zuora.zuora;

public class IdentityConverter<T> implements Converter<T, T> {
    @Override
    public T convert(T original) {
        return original;
    }
}
