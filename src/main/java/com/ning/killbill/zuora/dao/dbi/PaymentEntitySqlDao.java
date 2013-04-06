package com.ning.killbill.zuora.dao.dbi;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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

import com.ning.killbill.zuora.dao.entities.PaymentEntity;

@UseStringTemplate3StatementLocator
public interface PaymentEntitySqlDao extends Transactional<PaymentMethodEntitySqlDao>, CloseMe {

    @SqlQuery
    @Mapper(PaymentEntitySqlDaoMapper.class)
    public PaymentEntity getById(@Bind("kbPId") final String paymentId);


    @SqlUpdate
    public void insert(@Bind(binder = PaymentEntitySqlDaoBinder.class) PaymentEntity entity);


    public static class PaymentEntitySqlDaoBinder implements Binder<Bind, PaymentEntity> {

        @Override
        public void bind(final SQLStatement<?> stmt, final Bind bind, final PaymentEntity arg) {
            stmt.bind("kbPId", arg.getKbPaymentId());
            stmt.bind("kbAccountId", arg.getKbAccountId());
            stmt.bind("zPId", arg.getZuoraPaymentId());
            stmt.bind("zCreatedDate", arg.getCreatedDate());
            stmt.bind("zEffectiveDate", arg.getEffectiveDate());
            stmt.bind("zAmount", arg.getAmount());
            stmt.bind("zStatus", arg.getStatus());
            stmt.bind("zGatewayError", arg.getGatewayError());
            stmt.bind("zGatewayErrorCode", arg.getGatewayErrorCode());
            stmt.bind("zRefId", arg.getReferenceId());
            stmt.bind("zSecondRefId", arg.getSecondReferenceId());
        }
    }

    public static class PaymentEntitySqlDaoMapper implements ResultSetMapper<PaymentEntity> {

        @Override
        public PaymentEntity map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException {
            final String kbPamentId = r.getString("kb_p_id");
            final String kbAccountId = r.getString("kb_account_id");
            final String zuoraPamentId = r.getString("z_p_id");
            final Date createdDate = r.getDate("z_created_date");
            final Date effectiveDate = r.getDate("z_effective_date");
            final BigDecimal amount = r.getBigDecimal("z_amount");
            final String status = r.getString("z_status");
            final String gatewayError = r.getString("z_gateway_error");
            final String gatewayErrorCode = r.getString("z_gateway_error_code");
            final String firstRefId = r.getString("z_reference_id");
            final String secondRefId = r.getString("z_snd_reference_id");
            return new PaymentEntity(kbPamentId, kbAccountId, zuoraPamentId, createdDate, effectiveDate, amount, status, gatewayError, gatewayErrorCode, firstRefId, secondRefId);
        }
    }
}
