package com.ning.killbill.zuora.dao.dbi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.mixins.CloseMe;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

@UseStringTemplate3StatementLocator
public interface PaymentMethodEntitySqlDao extends Transactional<PaymentMethodEntitySqlDao>, CloseMe {

    @SqlQuery
    @Mapper(PaymentMethodEntitySqlDaoMapper.class)
    public PaymentMethodEntity getById(@Bind("kbPmId") final String paymentMethodId);


    @SqlQuery
    @Mapper(PaymentMethodEntitySqlDaoMapper.class)
    public List<PaymentMethodEntity> getByAccountId(@Bind("kbAccountId") final String kbAccountId);


    @SqlUpdate
    public void insert(@Bind(binder = PaymentMethodEntitySqlDaoBinder.class) PaymentMethodEntity entity);

    @SqlUpdate
    public void update(@Bind("kbPmId") final String paymentMethodId, @Bind("zPmId") final String zuoraPaymentMethodId, @Bind("zDefault") final Boolean isDefault);


    @SqlUpdate
    public void deleteById(@Bind("kbPmId") final String paymentMethodId);

    public static class PaymentMethodEntitySqlDaoBinder implements Binder<Bind, PaymentMethodEntity> {

        @Override
        public void bind(final SQLStatement<?> stmt, final Bind bind, final PaymentMethodEntity arg) {
            stmt.bind("kbPmId", arg.getKbPaymentMethodId());
            stmt.bind("kbAccountId", arg.getKbAccountId());
            stmt.bind("zPmId", arg.getZuoraPaymentMethodId());
            stmt.bind("zDefault", arg.isDefault());
        }
    }

    public static class PaymentMethodEntitySqlDaoMapper implements ResultSetMapper<PaymentMethodEntity> {

        @Override
        public PaymentMethodEntity map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {

            final String kbPamentMethodId = r.getString("kb_pm_id");
            final String kbAccountId = r.getString("kb_account_id");
            final String zuoraPaymentMethodId = r.getString("z_pm_id");
            final boolean isDefault = r.getBoolean("z_default");
            //final Date updatedTime = r.getDate("last_updated");

            return new PaymentMethodEntity(kbPamentMethodId, kbAccountId, zuoraPaymentMethodId, isDefault);
        }
    }
}
