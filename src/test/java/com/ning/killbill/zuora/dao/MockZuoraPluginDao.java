package com.ning.killbill.zuora.dao;

import java.util.ArrayList;
import java.util.List;

import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

public class MockZuoraPluginDao implements ZuoraPluginDao {

    final List<PaymentMethodEntity> paymentMethodEntities = new ArrayList<PaymentMethodEntity>();

    @Override
    public void insertPaymentMethod(final PaymentMethodEntity pm) {
        synchronized (paymentMethodEntities) {
            paymentMethodEntities.add(pm);
        }
    }

    @Override
    public PaymentMethodEntity getPaymentMethodById(final String kbPaymentMethodId) {
        for (final PaymentMethodEntity paymentMethodEntity : paymentMethodEntities) {
            if (paymentMethodEntity.getKbPaymentMethodId().equals(kbPaymentMethodId)) {
                return paymentMethodEntity;
            }
        }
        return null;
    }

    @Override
    public List<PaymentMethodEntity> getPaymentMethods(final String kbAccountId) {
        return ImmutableList.<PaymentMethodEntity>copyOf(Collections2.filter(paymentMethodEntities, new Predicate<PaymentMethodEntity>() {
            @Override
            public boolean apply(final PaymentMethodEntity input) {
                return input.getKbAccountId().equals(kbAccountId);
            }
        }));
    }

    @Override
    public void deletePaymentMethodById(final String kbPaymentMethodId) {
        synchronized (paymentMethodEntities) {
            for (final PaymentMethodEntity paymentMethodEntity : paymentMethodEntities) {
                if (paymentMethodEntity.getKbPaymentMethodId().equals(kbPaymentMethodId)) {
                    paymentMethodEntities.remove(paymentMethodEntity);
                }
            }
        }
    }

    @Override
    public void updatePaymentMethod(final PaymentMethodEntity newPm) {
        synchronized (paymentMethodEntities) {
            for (final PaymentMethodEntity paymentMethodEntity : paymentMethodEntities) {
                if (paymentMethodEntity.getKbPaymentMethodId().equals(newPm.getKbPaymentMethodId())) {
                    paymentMethodEntities.remove(paymentMethodEntity);
                    paymentMethodEntities.add(newPm);
                }
            }
        }
    }

    @Override
    public void resetPaymentMethods(final List<PaymentMethodEntity> newPms) {
        if (newPms.size() == 0) {
            return;
        }

        synchronized (paymentMethodEntities) {
            final String accountId = newPms.get(0).getKbAccountId();

            for (final PaymentMethodEntity paymentMethodEntity : paymentMethodEntities) {
                if (paymentMethodEntity.getKbAccountId().equals(accountId)) {
                    paymentMethodEntities.remove(paymentMethodEntity);
                }
            }

            for (final PaymentMethodEntity paymentMethodEntity : newPms) {
                paymentMethodEntities.add(paymentMethodEntity);
            }
        }
    }
}
