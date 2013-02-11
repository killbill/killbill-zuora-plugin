package com.ning.killbill.zuora.dao.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity(name = "_zuora_payment_methods")
public class PaymentMethodEntity implements Serializable {

    @Id
    @Column(name = "kb_pm_id", length=36, nullable = false)
    private String kbPaymentMethodId;

    @Column(name = "kb_account_id", length=36, nullable = false)
    private String kbAccountId;

    @Column(name = "z_pm_id", length=64, unique=true, nullable = false)
    private String zuoraPaymentMethodId;

    @Column(name = "z_default", columnDefinition = "tinyint(1) DEFAULT '0'")
    private boolean isDefault = false;

    @Version
    @Column(name = "last_updated")
    private Date updatedTime;

    public PaymentMethodEntity() {
    }

    public PaymentMethodEntity(final String kbPaymentMethodId, final String kbAccountId, final String zuoraPaymentMethodId, final boolean aDefault) {
        this.kbPaymentMethodId = kbPaymentMethodId;
        this.kbAccountId = kbAccountId;
        this.zuoraPaymentMethodId = zuoraPaymentMethodId;
        isDefault = aDefault;
    }

    public String getKbAccountId() {
        return kbAccountId;
    }

    public String getKbPaymentMethodId() {
        return kbPaymentMethodId;
    }

    public String getZuoraPaymentMethodId() {
        return zuoraPaymentMethodId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setZuoraPaymentMethodId(final String zuoraPaymentMethodId) {
        this.zuoraPaymentMethodId = zuoraPaymentMethodId;
    }

    public void setDefault(final boolean aDefault) {
        isDefault = aDefault;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentMethodEntity)) {
            return false;
        }

        final PaymentMethodEntity that = (PaymentMethodEntity) o;

        if (isDefault != that.isDefault) {
            return false;
        }
        if (kbAccountId != null ? !kbAccountId.equals(that.kbAccountId) : that.kbAccountId != null) {
            return false;
        }
        if (kbPaymentMethodId != null ? !kbPaymentMethodId.equals(that.kbPaymentMethodId) : that.kbPaymentMethodId != null) {
            return false;
        }
        if (zuoraPaymentMethodId != null ? !zuoraPaymentMethodId.equals(that.zuoraPaymentMethodId) : that.zuoraPaymentMethodId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = kbPaymentMethodId != null ? kbPaymentMethodId.hashCode() : 0;
        result = 31 * result + (kbAccountId != null ? kbAccountId.hashCode() : 0);
        result = 31 * result + (zuoraPaymentMethodId != null ? zuoraPaymentMethodId.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        return result;
    }
}
