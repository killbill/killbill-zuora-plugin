package com.ning.killbill.zuora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.killbill.zuora.dao.entities.PaymentMethodEntity;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

/*
  We are using compile time enhancement (byte code waving); in order to have it runtime you can use
  -javaagent:/PATH/openjpa-2.2.1.jar
*/
public class TestZuoraPluginDao {


    // http://comments.gmane.org/gmane.comp.apache.openjpa.user/8354

    private final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/zuora";
    private final String JDBC_USER = "root";
    private final String JDBC_PWD = "root";

    private DefaultZuoraPluginDao defaultZuoraPluginDao;
    private DataSource dataSource;


    @BeforeClass(groups = "slow")
    public void setup() throws Exception {
        dataSource = getC3P0DataSource();
        defaultZuoraPluginDao = new DefaultZuoraPluginDao(dataSource);
        cleanupTables();
    }

    @BeforeMethod(groups = "slow")
    public void beforeMethod() throws Exception {
        cleanupTables();
        /*
        if (defaultZuoraPluginDao == null) {
            defaultZuoraPluginDao = new DefaultZuoraPluginDao(dataSource);
        }
        */
    }

    @AfterMethod(groups = "slow")
    public void afterTest() {
        /*
        if (defaultZuoraPluginDao != null) {
            defaultZuoraPluginDao.close();
            defaultZuoraPluginDao = null;
        }
        */
    }


    @Test(groups = "slow", enabled = true)
    public void testBasic() throws Exception {

        final String accountId = UUID.randomUUID().toString();

        final String pmId1 = UUID.randomUUID().toString();
        final String zId1 = "zid1";
        final PaymentMethodEntity pm1 = new PaymentMethodEntity(pmId1, accountId, zId1, true);

        defaultZuoraPluginDao.insertPaymentMethod(pm1);
        PaymentMethodEntity resPm1 = defaultZuoraPluginDao.getPaymentMethodById(pmId1);
        Assert.assertEquals(resPm1, pm1);
        Assert.assertEquals(resPm1.isDefault(), true);


        List<PaymentMethodEntity> resPms = defaultZuoraPluginDao.getPaymentMethods(accountId);
        Assert.assertEquals(resPms.size(), 1);
        Assert.assertEquals(resPms.get(0), pm1);

        final String pmId2 = UUID.randomUUID().toString();
        final String zId2 = "zid2";
        final PaymentMethodEntity pm2 = new PaymentMethodEntity(pmId2, accountId, zId2, false);

        defaultZuoraPluginDao.insertPaymentMethod(pm2);
        PaymentMethodEntity resPm2 = defaultZuoraPluginDao.getPaymentMethodById(pmId2);
        Assert.assertEquals(resPm2, pm2);
        Assert.assertEquals(resPm2.isDefault(), false);

        resPms = defaultZuoraPluginDao.getPaymentMethods(accountId);
        Assert.assertEquals(resPms.size(), 2);

        final String newZId1 = "newzid1";
        final PaymentMethodEntity newPm1 = new PaymentMethodEntity(pmId1, accountId, newZId1, false);
        defaultZuoraPluginDao.updatePaymentMethod(newPm1);
        resPm1 = defaultZuoraPluginDao.getPaymentMethodById(pmId1);
        Assert.assertEquals(resPm1, newPm1);
        Assert.assertEquals(resPm1.isDefault(), false);

        resPm1 = defaultZuoraPluginDao.getPaymentMethodById(pmId1);
        Assert.assertEquals(resPm1, newPm1);
        Assert.assertEquals(resPm1.isDefault(), false);


        resPm1 = defaultZuoraPluginDao.getPaymentMethodById(pmId1);
        Assert.assertEquals(resPm1, newPm1);

        defaultZuoraPluginDao.deletePaymentMethodById(pmId1);

        resPms = defaultZuoraPluginDao.getPaymentMethods(accountId);
        Assert.assertEquals(resPms.size(), 1);
        Assert.assertEquals(resPms.get(0), pm2);
    }

    @Test(groups = "slow", enabled = true)
    public void testUpdateFromDetachedEntity() throws Exception {

        final String id = "the-id";
        final String accountId = "account-id";
        final String zid = "whatever";

        setupOnePM(id, accountId, zid);

        final String newZID = "zzzzz";
        final PaymentMethodEntity newPm = new PaymentMethodEntity(id, accountId, newZID, true);
        defaultZuoraPluginDao.updatePaymentMethod(newPm);

        PaymentMethodEntity resPm = defaultZuoraPluginDao.getPaymentMethodById(id);
        Assert.assertEquals(resPm, newPm);
        Assert.assertEquals(resPm.isDefault(), true);
    }


    @Test(groups = "slow", enabled = true)
    public void testDeleteFromDetachedEntity() throws Exception {

        final String id = "the-id";
        final String accountId = "account-id";
        final String zid = "whatever";

        setupOnePM(id, accountId, zid);

        List<PaymentMethodEntity> pms = defaultZuoraPluginDao.getPaymentMethods(accountId);
        Assert.assertEquals(pms.size(), 1);

        PaymentMethodEntity resPm = defaultZuoraPluginDao.getPaymentMethodById(id);
        Assert.assertEquals(resPm.getKbPaymentMethodId(), id);
        Assert.assertEquals(resPm.getKbAccountId(), accountId);
        Assert.assertEquals(resPm.getZuoraPaymentMethodId(), zid);

        defaultZuoraPluginDao.deletePaymentMethodById(id);

        pms = defaultZuoraPluginDao.getPaymentMethods(accountId);
        Assert.assertEquals(pms.size(), 0);
    }


    private void cleanupTables() throws Exception {
        if (dataSource != null) {
            Connection conn = dataSource.getConnection();
            PreparedStatement st = conn.prepareStatement("delete from _zuora_payment_methods");
            st.execute();
            st.close();
            conn.close();
        }
    }

    private void setupOnePM(String pmId, String accountId, String zId) throws Exception {

        final PaymentMethodEntity pm1 = new PaymentMethodEntity(pmId, accountId, zId, true);
        // Insert outside of EntityManager an entity
        Connection conn = dataSource.getConnection();
        PreparedStatement st = conn.prepareStatement(String.format("insert into _zuora_payment_methods (kb_pm_id, kb_account_id, z_pm_id) values (%s, %s, %s)",
                                                                                 "'" + pmId + "'", "'" + accountId + "'", "'" + zId + "'"));
        st.execute();
        st.close();
        conn.close();
    }

    private DataSource getMysqlDataSource() {
        MysqlDataSource ds =  new MysqlDataSource();

        ds.setServerName("localhost");
        ds.setPortNumber(3306);
        ds.setDatabaseName("zuora");
        ds.setUser("root");
        ds.setPassword("root");
        return ds;
    }


    private DataSource getBoneCpDataSource() {
        final BoneCPConfig config = createConfig(JDBC_URL, JDBC_USER, JDBC_PWD);
        return new BoneCPDataSource(config);
    }

    private DataSource getC3P0DataSource() {
        ComboPooledDataSource cpds = new ComboPooledDataSource();
        cpds.setJdbcUrl(JDBC_URL);
        cpds.setUser(JDBC_USER);
        cpds.setPassword(JDBC_PWD);
        cpds.setMinPoolSize(5);
        cpds.setAcquireIncrement(5);
        cpds.setMaxPoolSize(20);
        return cpds;
    }


    private BoneCPConfig createConfig(final String dbiString, final String userName, final String pwd) {
        final BoneCPConfig dbConfig = new BoneCPConfig();
        dbConfig.setJdbcUrl(dbiString);
        dbConfig.setUsername(userName);
        dbConfig.setPassword(pwd);
        dbConfig.setMinConnectionsPerPartition(1);
        dbConfig.setMaxConnectionsPerPartition(30);
        dbConfig.setConnectionTimeout(10, TimeUnit.SECONDS);
        dbConfig.setPartitionCount(1);
        dbConfig.setDefaultTransactionIsolation("REPEATABLE_READ");
        dbConfig.setDisableJMX(false);
        dbConfig.setLazyInit(true);
        return dbConfig;
    }
}
