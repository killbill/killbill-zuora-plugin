package com.ning.killbill.zuora.method;

public interface CreditCardProperties extends PaymentMethodProperties {

    public final static String TYPE_VALUE = "CreditCard";
    
    public final static String CARD_TYPE = "cardType";
    public final static String CARD_HOLDER_NAME = "cardHolderName";
    public final static String EXPIRATION_DATE = "expirationDate";    
    public final static String MASK_NUMBER = "maskNumber";        
    public final static String ADDRESS1 = "address1";
    public final static String ADDRESS2 = "address2";    
    public final static String CITY = "city";            
    public final static String COUNTRY = "country";
    public final static String POSTAL_CODE = "postalCode";
    public final static String STATE = "state";            
}
