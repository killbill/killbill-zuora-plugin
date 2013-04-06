package com.ning.killbill.zuora.dao.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;


@Entity(name = "_zuora_payments")
public class PaymentEntity implements Serializable {

    @Id
    @Column(name = "kb_p_id", length=36, nullable = false)
    private String kbPaymentId;

    @Column(name = "kb_account_id", length=36, nullable = false)
    private String kbAccountId;

    @Column(name = "z_p_id", length=64, unique=true, nullable = false)
    private String zuoraPaymentId;

    @Column(name = "z_created_date", nullable = false)
    private Date createdDate;

    @Column(name = "z_effective_date", nullable = false)
    private Date effectiveDate;

    @Column(name = "z_amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "z_status", length=64, nullable = false)
    private String status;

    @Column(name = "z_gateway_error", length=64, nullable = true)
    private String gatewayError;

    @Column(name = "z_gateway_error_code", length=32, nullable = true)
    private String gatewayErrorCode;

    @Column(name = "z_reference_id", length=36, nullable = false)
    private String referenceId;

    @Column(name = "z_snd_reference_id", length=36, nullable = false)
    private String secondReferenceId;

    @Version
    @Column(name = "last_updated")
    private Date updatedTime;

    public PaymentEntity() {

    }

    public PaymentEntity(final String kbPaymentId, final String kbAccountId, final String zuoraPaymentId,
                         final Date createdDate, final Date effectiveDate,
                         final BigDecimal amount, final String status,
                         final String gatewayError, final String gatewayErrorCode,
                         final String referenceId, final String secondReferenceId) {
        this.kbPaymentId = kbPaymentId;
        this.kbAccountId = kbAccountId;
        this.zuoraPaymentId = zuoraPaymentId;
        this.createdDate = createdDate;
        this.effectiveDate = effectiveDate;
        this.amount = amount;
        this.status = status;
        this.gatewayError = gatewayError;
        this.gatewayErrorCode = gatewayErrorCode;
        this.referenceId = referenceId;
        this.secondReferenceId = secondReferenceId;
    }

    public String getKbPaymentId() {
        return kbPaymentId;
    }

    public String getKbAccountId() {
        return kbAccountId;
    }

    public String getZuoraPaymentId() {
        return zuoraPaymentId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getGatewayError() {
        return gatewayError;
    }

    public String getGatewayErrorCode() {
        return gatewayErrorCode;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getSecondReferenceId() {
        return secondReferenceId;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentEntity)) {
            return false;
        }

        final PaymentEntity that = (PaymentEntity) o;

        if (amount != null ? (amount.compareTo(that.amount)  == 0) : that.amount != null) {
            return false;
        }
        if (createdDate != null ? (createdDate.compareTo(that.createdDate) == 0) : that.createdDate != null) {
            return false;
        }
        if (effectiveDate != null ? (effectiveDate.compareTo(that.effectiveDate) == 0) : that.effectiveDate != null) {
            return false;
        }
        if (gatewayError != null ? !gatewayError.equals(that.gatewayError) : that.gatewayError != null) {
            return false;
        }
        if (gatewayErrorCode != null ? !gatewayErrorCode.equals(that.gatewayErrorCode) : that.gatewayErrorCode != null) {
            return false;
        }
        if (kbAccountId != null ? !kbAccountId.equals(that.kbAccountId) : that.kbAccountId != null) {
            return false;
        }
        if (kbPaymentId != null ? !kbPaymentId.equals(that.kbPaymentId) : that.kbPaymentId != null) {
            return false;
        }
        if (referenceId != null ? !referenceId.equals(that.referenceId) : that.referenceId != null) {
            return false;
        }
        if (secondReferenceId != null ? !secondReferenceId.equals(that.secondReferenceId) : that.secondReferenceId != null) {
            return false;
        }
        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        if (zuoraPaymentId != null ? !zuoraPaymentId.equals(that.zuoraPaymentId) : that.zuoraPaymentId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = kbPaymentId != null ? kbPaymentId.hashCode() : 0;
        result = 31 * result + (kbAccountId != null ? kbAccountId.hashCode() : 0);
        result = 31 * result + (zuoraPaymentId != null ? zuoraPaymentId.hashCode() : 0);
        result = 31 * result + (createdDate != null ? createdDate.hashCode() : 0);
        result = 31 * result + (effectiveDate != null ? effectiveDate.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (gatewayError != null ? gatewayError.hashCode() : 0);
        result = 31 * result + (gatewayErrorCode != null ? gatewayErrorCode.hashCode() : 0);
        result = 31 * result + (referenceId != null ? referenceId.hashCode() : 0);
        result = 31 * result + (secondReferenceId != null ? secondReferenceId.hashCode() : 0);
        return result;
    }
}
