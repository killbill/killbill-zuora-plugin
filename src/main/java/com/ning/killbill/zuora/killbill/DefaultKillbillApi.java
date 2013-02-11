package com.ning.killbill.zuora.killbill;

import java.util.UUID;

import org.osgi.service.log.LogService;

import com.ning.billing.BillingExceptionBase;
import com.ning.billing.account.api.Account;
import com.ning.billing.account.api.AccountApiException;
import com.ning.billing.osgi.api.OSGIKillbill;
import com.ning.billing.payment.api.Payment;
import com.ning.billing.payment.api.PaymentMethod;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.TenantContext;

public class DefaultKillbillApi {

    public static final String ERROR_API_KILLBILL = "Killbill error";

    private final OSGIKillbill osgiKillbill;
    private final LogService logService;

    public DefaultKillbillApi(final OSGIKillbill osgiKillbill, final LogService logService) {
        this.osgiKillbill = osgiKillbill;
        this.logService = logService;
    }

    public String getAccountExternalKeyFromPaymentMethodId(final UUID kbPaymentMethodId, final TenantContext tenantContext) throws PaymentPluginApiException {
        try {
            final PaymentMethod paymentMethod = osgiKillbill.getPaymentApi().getPaymentMethodById(kbPaymentMethodId, tenantContext);
            final Account account = osgiKillbill.getAccountUserApi().getAccountById(paymentMethod.getAccountId(), tenantContext);
            return account.getExternalKey();
        } catch (BillingExceptionBase e) {
            logService.log(LogService.LOG_ERROR, "Failed to retrieve external key for payment methodId=" + kbPaymentMethodId, e);
            throw new PaymentPluginApiException(ERROR_API_KILLBILL, e);
        }
    }

    public String getAccountExternalKeyFromPaymentId(final UUID kbPaymentId, final TenantContext tenantContext) throws PaymentPluginApiException {
        try {
            final Payment payment = osgiKillbill.getPaymentApi().getPayment(kbPaymentId, tenantContext);
            final Account account = osgiKillbill.getAccountUserApi().getAccountById(payment.getAccountId(), tenantContext);
            return account.getExternalKey();
        } catch (BillingExceptionBase e) {
            logService.log(LogService.LOG_ERROR, "Failed to retrieve external key for paymentId=" + kbPaymentId, e);
            throw new PaymentPluginApiException(ERROR_API_KILLBILL, e);
        }
    }

    public String getAccountExternalKeyFromAccountId(final UUID kbAccountId, final TenantContext tenantContext) throws PaymentPluginApiException {
        final Account account;
        try {
            account = osgiKillbill.getAccountUserApi().getAccountById(kbAccountId, tenantContext);
        } catch (AccountApiException e) {
            logService.log(LogService.LOG_ERROR, "Failed to retrieve external key for accountId=" + kbAccountId, e);
            throw new PaymentPluginApiException(ERROR_API_KILLBILL, e);
        }
        return account.getExternalKey();
    }


}
