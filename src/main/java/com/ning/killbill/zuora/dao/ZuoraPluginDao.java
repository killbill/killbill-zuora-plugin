package com.ning.killbill.zuora.dao;

import java.util.List;

import com.ning.killbill.zuora.dao.entities.PaymentEntity;
import com.ning.killbill.zuora.dao.entities.PaymentMethodDetailEntity;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

public interface ZuoraPluginDao {

    public void insertPaymentMethod(final PaymentMethodEntity pm);

    public PaymentMethodEntity getPaymentMethodById(final String kbPaymentMethodId);

    public List<PaymentMethodEntity> getPaymentMethods(final String kbAccountId);

    public void deletePaymentMethodById(final String kbPaymentMethodId);

    public void updatePaymentMethod(final PaymentMethodEntity newPm);

    public void resetPaymentMethods(final List<PaymentMethodEntity> newPms);

    public PaymentMethodDetailEntity getPaymentMethodDetailById(final String zPaymentMethodId);

    public void insertPaymentMethodDetail(final PaymentMethodDetailEntity pmd);

    public void deletePaymentMethodDetailById(final String zPaymentMethodId);

    public void insertPayment(PaymentEntity p);

    public PaymentEntity getPayment(final String kbPaymentId);

}
