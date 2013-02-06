package com.ning.killbill.zuora.zuora;

public interface Converter<S, T> {
    T convert(S original);
}
