package com.ning.killbill.zuora.dao.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.sql.DataSource;

import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

public class JPAZuoraPluginDao implements ZuoraPluginDao {

    private EntityManagerFactory entityManagerFactory;


    public JPAZuoraPluginDao(final DataSource dataSource) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("openjpa.ConnectionFactory", dataSource);
        entityManagerFactory = Persistence.createEntityManagerFactory("zuora", props);
    }

    public void close() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }


    public void insertPaymentMethod(final PaymentMethodEntity pm) {

        new WithEntityManager(true).<Void>doOperation(new EntityManagerCallback<Void>() {
            @Override
            public Void doRealOperation(final EntityManager entityManager) {
                entityManager.persist(pm);
                return null;
            }
        });
    }

    public PaymentMethodEntity getPaymentMethodById(final String kbPaymentMethodId) {

        return new WithEntityManager(false).<PaymentMethodEntity>doOperation(new EntityManagerCallback<PaymentMethodEntity>() {
            @Override
            public PaymentMethodEntity doRealOperation(final EntityManager entityManager) {
                final PaymentMethodEntity pm = entityManager.find(PaymentMethodEntity.class, kbPaymentMethodId);
                return pm;
            }
        });
    }

    public List<PaymentMethodEntity> getPaymentMethods(final String kbAccountId) {

        return new WithEntityManager(false).<List<PaymentMethodEntity>>doOperation(new EntityManagerCallback<List<PaymentMethodEntity>>() {
            @Override
            public List<PaymentMethodEntity> doRealOperation(final EntityManager entityManager) {
                final Query query = entityManager.createQuery("SELECT pm FROM _zuora_payment_methods pm WHERE pm.kbAccountId=:kbAccountId");
                query.setParameter("kbAccountId", kbAccountId);
                final List<PaymentMethodEntity> pms = query.getResultList();
                return pms;
            }
        });
    }

    public void deletePaymentMethodById(final String kbPaymentMethodId) {

        new WithEntityManager(true).<Void>doOperation(new EntityManagerCallback<Void>() {
            @Override
            public Void doRealOperation(final EntityManager entityManager) {
                final PaymentMethodEntity pm = entityManager.find(PaymentMethodEntity.class, kbPaymentMethodId);
                if (pm != null) {
                    entityManager.remove(pm);
                }
                return null;
            }
        });
    }

    public void updatePaymentMethod(final PaymentMethodEntity newPm) {

        new WithEntityManager(true).<Void>doOperation(new EntityManagerCallback<Void>() {
            @Override
            public Void doRealOperation(final EntityManager entityManager) {
                final PaymentMethodEntity pm = entityManager.find(PaymentMethodEntity.class, newPm.getKbPaymentMethodId());
                if (pm != null) {
                    pm.setZuoraPaymentMethodId(newPm.getZuoraPaymentMethodId());
                    pm.setDefault(newPm.isDefault());
                    entityManager.merge(pm);
                }
                return null;
            }
        });
    }

    @Override
    public void resetPaymentMethods(final List<PaymentMethodEntity> newPms) {

        if (newPms == null || newPms.size() == 0) {
            return;
        }

        final String kbAccountId = newPms.get(0).getKbAccountId();

        new WithEntityManager(true).<Void>doOperation(new EntityManagerCallback<Void>() {
            @Override
            public Void doRealOperation(final EntityManager entityManager) {


                final Query query = entityManager.createQuery("SELECT pm FROM _zuora_payment_methods pm WHERE pm.kbAccountId=:kbAccountId");
                query.setParameter("kbAccountId", kbAccountId);
                final List<PaymentMethodEntity> pms = query.getResultList();
                for (PaymentMethodEntity cur : pms) {
                    entityManager.remove(cur);
                }

                for (PaymentMethodEntity cur : newPms) {
                    entityManager.persist(cur);
                }
                return null;
            }
        });
    }


    private interface EntityManagerCallback<T> {
        T doRealOperation(EntityManager entityManager);
    }

    private class WithEntityManager {

        private final boolean withTransaction;

        public WithEntityManager(boolean withTransaction) {
            this.withTransaction = withTransaction;
        }

        public <T> T doOperation(EntityManagerCallback<T> callback)  {

            EntityManager entityManager = null;
            EntityTransaction transaction = null;
            boolean committed = false;
            T res = null;

            try {
                entityManager = entityManagerFactory.createEntityManager();

                if (withTransaction) {
                    transaction = entityManager.getTransaction();
                    transaction.begin();
                }
                res = callback.doRealOperation(entityManager);

                if (withTransaction) {
                    transaction.commit();
                    committed = true;
                }
            } finally {
                if (transaction != null && !committed) {
                    transaction.rollback();
                }
                if (entityManager != null) {
                    entityManager.close();
                }
            }
            return res;
        }
    }
}
