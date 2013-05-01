package com.ning.killbill.zuora.dao.dbi;

import java.sql.ResultSet;
import java.sql.SQLException;

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

import com.ning.killbill.zuora.dao.entities.PaymentMethodDetailEntity;

@UseStringTemplate3StatementLocator
public interface PaymentMethodDetailEntitySqlDao extends Transactional<PaymentMethodDetailEntitySqlDao>, CloseMe {

    @SqlQuery
    @Mapper(PaymentMethodDetailEntitySqlDaoMapper.class)
    public PaymentMethodDetailEntity getById(@Bind("zPmId") final String paymentMethodId);


    @SqlUpdate
    public void insert(@Bind(binder = PaymentMethodDetailEntitySqlDaoBinder.class) PaymentMethodDetailEntity entity);

    @SqlUpdate
    public void deleteById(@Bind("zPmId") final String paymentMethodId);

    public static class PaymentMethodDetailEntitySqlDaoBinder implements Binder<Bind, PaymentMethodDetailEntity> {

        @Override
        public void bind(final SQLStatement<?> stmt, final Bind bind, final PaymentMethodDetailEntity arg) {
            stmt.bind("zPmId", arg.getzPmId());
            stmt.bind("type", arg.getType());
            stmt.bind("ccName", arg.getCcName());
            stmt.bind("ccType", arg.getCcType());
            stmt.bind("ccExprirationMonth", arg.getCcExprirationMonth());
            stmt.bind("ccExprirationYear", arg.getCcExprirationYear());
            stmt.bind("ccLast4", arg.getCcLast4());
            stmt.bind("address1", arg.getAddress1());
            stmt.bind("address2", arg.getAddress2());
            stmt.bind("city", arg.getCity());
            stmt.bind("state", arg.getState());
            stmt.bind("zip", arg.getZip());
            stmt.bind("country", arg.getCountry());
        }
    }


    public static class PaymentMethodDetailEntitySqlDaoMapper implements ResultSetMapper<PaymentMethodDetailEntity> {

        @Override
        public PaymentMethodDetailEntity map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
            final String zPmId = r.getString("z_pm_id");
            final String type = r.getString("type");
            final String ccName = r.getString("cc_name");
            final String ccType = r.getString("cc_type");
            final String ccExprirationMonth = r.getString("cc_expriration_month");
            final String ccExprirationYear = r.getString("cc_expriration_year");
            final String ccLast4 = r.getString("cc_last4");
            final String address1 = r.getString("address1");
            final String address2 = r.getString("address2");
            final String city = r.getString("city");
            final String state = r.getString("state");
            final String zip = r.getString("zip");
            final String country = r.getString("country");
            return new PaymentMethodDetailEntity(zPmId, type, ccName, ccType, ccExprirationMonth, ccExprirationYear, ccLast4, address1, address2, city, state, zip, country);
        }
    }

}

