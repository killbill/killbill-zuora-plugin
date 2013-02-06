/*
 * Copyright 2010-2013 Ning, Inc.
 *
 *  Ning licenses this file to you under the Apache License, version 2.0
 *  (the "License"); you may not use this file except in compliance with the
 *  License.  You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

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
