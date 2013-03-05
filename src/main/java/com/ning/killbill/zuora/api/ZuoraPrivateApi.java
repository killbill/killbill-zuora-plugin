package com.ning.killbill.zuora.api;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.TenantContext;

import com.zuora.api.object.Invoice;

/**
 * Additional APIs exported by the plugin which are not seen by Killbill
 *
 * Those can be used to REST endpoint or for other killbill bundles; they are required
 * to setup zuora
 */
public interface ZuoraPrivateApi {

    /**
     *
     * @param accountId the Killbill accountId
     * @param context a Killbill context
     * @return the zuora accountId
     * @throws PaymentPluginApiException
     */
    public String createPaymentProviderAccount(UUID accountId, TenantContext context)
            throws PaymentPluginApiException;

    /**
     *
     * @param accountId the Killbill accountId
     * @param context    a killbill context
     * @return all the payment methods returned by Zuora for that account
     * @throws PaymentPluginApiException
     */
    public List<PaymentMethodPlugin> getPaymentMethodDetails(UUID accountId, TenantContext context)
            throws PaymentPluginApiException;

    /**
     * Updates the DEFAULT zuora payment method
     *
     * @param accountId the Killbill accountId
     * @param paymentMethodProps the new payment info for that paymentMethod
     * @param context
     * @throws PaymentPluginApiException
     */
    public void updateDefaultPaymentMethod(UUID accountId, PaymentMethodPlugin paymentMethodProps, TenantContext context)
            throws PaymentPluginApiException;


    /**
     *
     * @param paymentMethodId the kb payment method Id
     * @return
     * @throws PaymentPluginApiException
     */
    public String getExternalPaymentMethodId(UUID paymentMethodId)
            throws PaymentPluginApiException;

    /**
     * Get Zuora invoices
     *
     * @param accountId the Killbill accountId
     * @param from      earliest invoice posted date
     * @param to        latest invoice posted date
     * @param context   tenant context
     * @return Zuora invoices
     */
    public List<Invoice> getInvoices(UUID accountId, @Nullable DateTime from, @Nullable DateTime to, TenantContext context) throws PaymentPluginApiException;
}
