package com.ning.killbill.zuora.api;

import java.util.List;

import com.ning.billing.account.api.Account;
import com.ning.billing.payment.api.PaymentMethodPlugin;
import com.ning.billing.payment.plugin.api.PaymentPluginApiException;
import com.ning.billing.util.callcontext.CallContext;
import com.ning.billing.util.callcontext.TenantContext;

/**
 * Additional APIs exported by the plugin which are not seen by Killbill
 *
 * Those can be used to REST endpoint or for other killbill bundles; they are required
 * to setup zuora
 */
public interface ZuoraPrivateApi {

    /**
     *
     * @param account the Killbill Account info
     * @param context a Killbill context
     * @return the zuora accountId
     * @throws PaymentPluginApiException
     */
    public String createPaymentProviderAccount(Account account, CallContext context)
            throws PaymentPluginApiException;

    /**
     *
     * @param accountKey the accountKey (that zuora knows about)
     * @param context    a killbill context
     * @return all the payment methods returned by Zuora for that account
     * @throws PaymentPluginApiException
     */
    public List<PaymentMethodPlugin> getPaymentMethodDetails(String accountKey, TenantContext context)
            throws PaymentPluginApiException;

    /**
     *
     * @param accountKey  the accountKey (that zuora knows about)
     * @param paymentMethodId the paymentMethodId  (that zuora knows about)
     * @param context a Killbill context
     * @return the paymentMethod info associated with that externalPaymentMethodId
     * @throws PaymentPluginApiException
     */
    public PaymentMethodPlugin getPaymentMethodDetail(String accountKey, String paymentMethodId, TenantContext context)
            throws PaymentPluginApiException;


    /**
     * Updates the DEFAULT zuora payment method
     *
     * @param accountKey   the accountKey (that zuora knows about)
     * @param paymentMethodProps the new payment info for that paymentMethod
     * @param context
     * @throws PaymentPluginApiException
     */
    public void updatePaymentMethod(String accountKey, PaymentMethodPlugin paymentMethodProps, CallContext context)
            throws PaymentPluginApiException;


}
