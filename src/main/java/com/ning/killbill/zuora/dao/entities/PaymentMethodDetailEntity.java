package com.ning.killbill.zuora.dao.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity(name = "_zuora_payment_methods_details")
public class PaymentMethodDetailEntity {

    @Id
    @Column(name = "z_pm_id", length=64, unique=true, nullable = false)
    private String zPmId;

    @Column(name = "type", length=32, nullable = false)
    private String type;

    @Column(name = "cc_name", length=64, nullable = true)
    private String ccName;

    @Column(name = "cc_type", length=32, nullable = true)
    private String ccType;

    @Column(name = "cc_expriration_month", length=4, nullable = true)
    private String ccExprirationMonth;

    @Column(name = "cc_expriration_year", length=8, nullable = true)
    private String ccExprirationYear;

    @Column(name = "cc_last4", length=32, nullable = true)
    private String ccLast4;

    @Column(name = "address1", length=255, nullable = true)
    private String address1;

    @Column(name = "address2", length=255, nullable = true)
    private String address2;

    @Column(name = "city", length=40, nullable = true)
    private String city;

    @Column(name = "state", length=50, nullable = true)
    private String state;

    @Column(name = "zip", length=20, nullable = true)
    private String zip;

    @Column(name = "country", length=40, nullable = true)
    private String country;


    @Version
    @Column(name = "last_updated")
    private Date updatedTime;


    public PaymentMethodDetailEntity() {
    }

    public PaymentMethodDetailEntity(final String zPmId, final String type, final String ccName, final String ccType,
                                     final String ccExprirationMonth, final String ccExprirationYear, final String ccLast4,
                                     final String address1, final String address2, final String city, final String state,
                                     final String zip, final String country) {
        this.zPmId = zPmId;
        this.type = type;
        this.ccName = ccName;
        this.ccType = ccType;
        this.ccExprirationMonth = ccExprirationMonth;
        this.ccExprirationYear = ccExprirationYear;
        this.ccLast4 = ccLast4;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.country = country;
    }

    public String getzPmId() {
        return zPmId;
    }

    public String getType() {
        return type;
    }

    public String getCcName() {
        return ccName;
    }

    public String getCcType() {
        return ccType;
    }

    public String getCcExprirationMonth() {
        return ccExprirationMonth;
    }

    public String getCcExprirationYear() {
        return ccExprirationYear;
    }

    public String getCcLast4() {
        return ccLast4;
    }

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public String getCountry() {
        return country;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PaymentMethodDetailEntity)) {
            return false;
        }

        final PaymentMethodDetailEntity that = (PaymentMethodDetailEntity) o;

        if (address1 != null ? !address1.equals(that.address1) : that.address1 != null) {
            return false;
        }
        if (address2 != null ? !address2.equals(that.address2) : that.address2 != null) {
            return false;
        }
        if (ccExprirationMonth != null ? !ccExprirationMonth.equals(that.ccExprirationMonth) : that.ccExprirationMonth != null) {
            return false;
        }
        if (ccExprirationYear != null ? !ccExprirationYear.equals(that.ccExprirationYear) : that.ccExprirationYear != null) {
            return false;
        }
        if (ccLast4 != null ? !ccLast4.equals(that.ccLast4) : that.ccLast4 != null) {
            return false;
        }
        if (ccName != null ? !ccName.equals(that.ccName) : that.ccName != null) {
            return false;
        }
        if (ccType != null ? !ccType.equals(that.ccType) : that.ccType != null) {
            return false;
        }
        if (city != null ? !city.equals(that.city) : that.city != null) {
            return false;
        }
        if (country != null ? !country.equals(that.country) : that.country != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }
        if (!zPmId.equals(that.zPmId)) {
            return false;
        }
        if (zip != null ? !zip.equals(that.zip) : that.zip != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = zPmId.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (ccName != null ? ccName.hashCode() : 0);
        result = 31 * result + (ccType != null ? ccType.hashCode() : 0);
        result = 31 * result + (ccExprirationMonth != null ? ccExprirationMonth.hashCode() : 0);
        result = 31 * result + (ccExprirationYear != null ? ccExprirationYear.hashCode() : 0);
        result = 31 * result + (ccLast4 != null ? ccLast4.hashCode() : 0);
        result = 31 * result + (address1 != null ? address1.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (zip != null ? zip.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}
