package com.ning.killbill.zuora.dao.dbi;

import java.util.List;

import javax.sql.DataSource;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.IDBI;
import org.skife.jdbi.v2.Transaction;
import org.skife.jdbi.v2.TransactionStatus;

import com.ning.killbill.zuora.dao.ZuoraPluginDao;
import com.ning.killbill.zuora.dao.entities.PaymentEntity;
import com.ning.killbill.zuora.dao.entities.PaymentMethodDetailEntity;
import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

public class JDBIZuoraPluginDao implements ZuoraPluginDao {

    private final IDBI dbi;
    private final PaymentMethodEntitySqlDao paymentMethodEntitySqlDao;
    private final PaymentMethodDetailEntitySqlDao paymentMethodDetailEntitySqlDao;
    private final PaymentEntitySqlDao paymentEntitySqlDao;

    public JDBIZuoraPluginDao(final DataSource dataSource) {
        dbi = new DBI(dataSource);
        this.paymentMethodEntitySqlDao = dbi.onDemand(PaymentMethodEntitySqlDao.class);
        this.paymentMethodDetailEntitySqlDao = dbi.onDemand(PaymentMethodDetailEntitySqlDao.class);
        this.paymentEntitySqlDao = dbi.onDemand(PaymentEntitySqlDao.class);
    }

    @Override
    public void insertPaymentMethod(final PaymentMethodEntity newPm) {
        paymentMethodEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.insert(newPm);
                return null;
            }
        });
    }

    @Override
    public PaymentMethodEntity getPaymentMethodById(final String kbPaymentMethodId) {
        return paymentMethodEntitySqlDao.inTransaction(new Transaction<PaymentMethodEntity, PaymentMethodEntitySqlDao>() {
            @Override
            public PaymentMethodEntity inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                return transactional.getById(kbPaymentMethodId);
            }
        });
    }

    @Override
    public List<PaymentMethodEntity> getPaymentMethods(final String kbAccountId) {
        return paymentMethodEntitySqlDao.inTransaction(new Transaction<List<PaymentMethodEntity>, PaymentMethodEntitySqlDao>() {
            @Override
            public List<PaymentMethodEntity> inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                return transactional.getByAccountId(kbAccountId);
            }
        });
    }

    @Override
    public void deletePaymentMethodById(final String kbPaymentMethodId) {
        paymentMethodEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.deleteById(kbPaymentMethodId);
                return null;
            }
        });
    }

    @Override
    public void updatePaymentMethod(final PaymentMethodEntity newPm) {
        paymentMethodEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.update(newPm.getKbPaymentMethodId(), newPm.getZuoraPaymentMethodId(), newPm.isDefault());
                return null;
            }
        });
    }

    @Override
    public void resetPaymentMethods(final List<PaymentMethodEntity> newPms) {
        paymentMethodEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodEntitySqlDao transactional, final TransactionStatus status) throws Exception {

                final String accountId = (newPms != null && newPms.size() > 0) ? newPms.get(0).getKbAccountId() : null;
                if (accountId != null) {
                    final List<PaymentMethodEntity> old = transactional.getByAccountId(accountId);
                    for (PaymentMethodEntity cur : old) {
                        transactional.deleteById(cur.getKbPaymentMethodId());
                    }
                    for (PaymentMethodEntity cur : newPms) {
                        transactional.insert(cur);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public PaymentMethodDetailEntity getPaymentMethodDetailById(final String zPaymentMethodId) {
        return paymentMethodDetailEntitySqlDao.inTransaction(new Transaction<PaymentMethodDetailEntity, PaymentMethodDetailEntitySqlDao>() {
            @Override
            public PaymentMethodDetailEntity inTransaction(final PaymentMethodDetailEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                return transactional.getById(zPaymentMethodId);
            }
        });
    }


    @Override
    public void insertPaymentMethodDetail(final PaymentMethodDetailEntity pmd) {
        paymentMethodDetailEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodDetailEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodDetailEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.insert(pmd);
                return null;
            }
        });
    }

    @Override
    public void deletePaymentMethodDetailById(final String zPaymentMethodId) {
        paymentMethodDetailEntitySqlDao.inTransaction(new Transaction<Void, PaymentMethodDetailEntitySqlDao>() {
            @Override
            public Void inTransaction(final PaymentMethodDetailEntitySqlDao transactional, final TransactionStatus status) throws Exception {
                transactional.deleteById(zPaymentMethodId);
                return null;
            }
        });
    }

    @Override
    public void insertPayment(final PaymentEntity p) {
        paymentEntitySqlDao.insert(p);
    }

    @Override
    public PaymentEntity getPayment(final String kbPaymentId) {
        return paymentEntitySqlDao.getById(kbPaymentId);

    }
}
