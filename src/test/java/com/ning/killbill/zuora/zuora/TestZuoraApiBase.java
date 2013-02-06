package com.ning.killbill.zuora.zuora;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import com.ning.killbill.zuora.method.CreditCardProperties;
import com.ning.killbill.zuora.method.PaymentMethodProperties;
import com.ning.killbill.zuora.method.PaypalProperties;
import com.ning.killbill.zuora.api.ZuoraPaymentMethodPlugin;
import com.ning.killbill.zuora.zuora.setup.ZuoraConfig;
import com.ning.killbill.zuora.util.Either;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.skife.config.ConfigurationObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.ImmutableMap;
import com.ning.billing.catalog.api.Currency;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.zuora.api.object.Account;

public class TestZuoraApiBase {

    private static final Logger log = LoggerFactory.getLogger(TestZuoraApiBase.class);

    // STEPH : change with not Ning test accounts
    protected String PAYPAL_EMAIL = "fedde_1300733427_biz@ning.com";
    protected String PAYPAL_BAID = "B-5A1817678F543884D";


    protected final static String KILLBILL_PAYMENT_ID = "43e91c3b-b09c-4771-893f-9083e5f19a37";


    protected final static String EXTERNAL_NAME = "trsgafuwg";
    protected final static UUID ACCOUNT_ID = UUID.randomUUID();

    protected ZuoraConfig zuoraConfig;
    protected ZuoraApi zuoraApi;
    protected ConnectionFactory connectionFactory;
    protected ConnectionPool pool;
    protected Account account;
    protected LogService logService;

    protected final PaymentConverter paymentConverter = new PaymentConverter();
    protected final AccountConverter accountConverter = new AccountConverter();

    private final static String instanceName = "test-zuora";

    private static Properties loadSystemPropertiesFromClasspath(final String resource) {

        Properties props = new Properties();
        final URL url = TestZuoraApiBase.class.getResource(resource);
        assertNotNull(url);

        try {
            props.load(url.openStream());
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public void setup() {
        // Specify test variables
        //
        //
        //

        final Properties props = System.getProperties();
        final ConfigurationObjectFactory factory = new ConfigurationObjectFactory(props);
        logService = new LogServiceTest(log);

        zuoraConfig = factory.buildWithReplacements(ZuoraConfig.class,
                ImmutableMap.of("pluginInstanceName", instanceName));
        zuoraApi = new ZuoraApi(zuoraConfig, logService);
        connectionFactory = new ConnectionFactory(zuoraConfig, zuoraApi, logService);
        pool = new ConnectionPool(connectionFactory, zuoraConfig);
    }


    @BeforeMethod
    public void setupMethod() {
        final com.ning.billing.account.api.Account accountTmp = createAccount(ACCOUNT_ID, EXTERNAL_NAME);
        this.account = withConnection(new ConnectionCallback<Account>() {
            @Override
            public Account withConnection(ZuoraConnection connection) {

                Either<ZuoraError, Account> resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);
                if (resultGetSnAccount.isRight()) {
                    zuoraApi.deleteAccount(connection, resultGetSnAccount.getRight());
                    log.info("Deleted zuora account {}", resultGetSnAccount.getRight().getId());
                }

                Either<ZuoraError, String> resultAccount = zuoraApi.createPaymentProviderAccount(connection, accountTmp);
                assertTrue(resultAccount.isRight());

                resultGetSnAccount = zuoraApi.getByAccountName(connection, EXTERNAL_NAME);

                assertTrue(resultGetSnAccount.isRight());
                assertEquals(EXTERNAL_NAME, resultGetSnAccount.getRight().getAccountNumber());

                return resultGetSnAccount.getRight();
            }
        });
        log.info("Created zuora account {}", account.getId());
    }

    @AfterMethod
    public void afterMethod() {
        withConnection(new ConnectionCallback<Void>() {
            @Override
            public Void withConnection(ZuoraConnection connection) {
                return null;
            }
        });
    }

    protected static interface ConnectionCallback<T> {
        T withConnection(ZuoraConnection connection);
    }

    protected <T> T withConnection(ConnectionCallback<T> callback) {
        ZuoraConnection connection = pool.borrowFromPool();

        try {
            return callback.withConnection(connection);
        }
        finally {
            if (connection != null) {
                try {
                    pool.returnToPool(connection);
                }
                catch (PoolException ex) {
                    fail(ex.getMessage());
                }
            }
        }
    }


    protected com.ning.billing.account.api.Account createAccount(final UUID accountId, final String externalKey) {
        com.ning.billing.account.api.Account account = mock(com.ning.billing.account.api.Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getEmail()).thenReturn("yo@yahoo.com");
        when(account.getExternalKey()).thenReturn(externalKey);
        when(account.getCurrency()).thenReturn(Currency.USD);
        when(account.getFirstNameLength()).thenReturn(5);
        when(account.getName()).thenReturn("zobie la mouche");
        when(account.getCity()).thenReturn("san francisco");
        when(account.getCountry()).thenReturn("france");
        return account;
     }

    protected PaymentMethodPlugin createPaypalPaymentMethod(String defaultPaymentMethodId, boolean isDefault) {
        ZuoraPaymentMethodPlugin result = new ZuoraPaymentMethodPlugin(defaultPaymentMethodId, isDefault);
        result.addProperty(PaymentMethodProperties.TYPE, PaypalProperties.TYPE_VALUE, false);
        result.addProperty(PaypalProperties.EMAIL, PAYPAL_EMAIL, false);
        result.addProperty(PaypalProperties.BAID, PAYPAL_BAID, false);
        return result;
    }


    protected PaymentMethodPlugin createCreditCardPaymentMethod(String defaultPaymentMethodId, boolean isDefault, String expirationDate) {
        ZuoraPaymentMethodPlugin result = new ZuoraPaymentMethodPlugin(defaultPaymentMethodId, isDefault);
        if (defaultPaymentMethodId == null) {
            result.addProperty(PaymentMethodProperties.TYPE, CreditCardProperties.TYPE_VALUE, false);
            result.addProperty(CreditCardProperties.CARD_TYPE, "Visa", false);
            result.addProperty(CreditCardProperties.MASK_NUMBER, "4111111111111111", false);
            result.addProperty(CreditCardProperties.CARD_HOLDER_NAME, "booboo", false);
        }
        result.addProperty(CreditCardProperties.ADDRESS1, "12 peralta ave", false);
        result.addProperty(CreditCardProperties.ADDRESS2, "suite 300", false);
        result.addProperty(CreditCardProperties.CITY, "San Francisco", false);
        result.addProperty(CreditCardProperties.COUNTRY, "USA", false);
        result.addProperty(CreditCardProperties.POSTAL_CODE, "94110", false);
        result.addProperty(CreditCardProperties.EXPIRATION_DATE, expirationDate, false);
        result.addProperty(CreditCardProperties.STATE, "CA", false);
        return result;
    }
}
