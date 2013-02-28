package com.ning.killbill.zuora.dao;

import java.util.List;

import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

public interface ZuoraPluginDao {

    public void insertPaymentMethod(final PaymentMethodEntity pm);

    public PaymentMethodEntity getPaymentMethodById(final String kbPaymentMethodId);

    public List<PaymentMethodEntity> getPaymentMethods(final String kbAccountId);

    public void deletePaymentMethodById(final String kbPaymentMethodId);

    public void updatePaymentMethod(final PaymentMethodEntity newPm);

    public void resetPaymentMethods(final List<PaymentMethodEntity> newPms);

}
