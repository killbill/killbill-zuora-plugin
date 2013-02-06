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

package com.ning.killbill.zuora.zuora;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class ZuoraDateUtils {

    static final DateTimeZone ZUORA_TZ = DateTimeZone.forOffsetHours(-8);

    private ZuoraDateUtils() {
    }

    /**
     * Current time
     * @return Current time in Zuora's timezone
     */
    public static DateTime now() {
        return new DateTime(ZUORA_TZ);
    }

    /**
     * Today's date
     * @return Today's date in Zuora's timezone (without time)
     */
    public static DateTime today() {
        return new LocalDate(ZUORA_TZ).toDateTimeAtStartOfDay(ZUORA_TZ);
    }

    /**
     * Today's day of the month
     * @return Today's day of the month in Zuora's timezone
     */
    public static int dayOfMonth() {
        return new LocalDate(ZUORA_TZ).getDayOfMonth();
    }

    /**
     * Converts DateTime to Zuora's DateTime
     * @param dateTime DateTime to convert to Zuora's timezone
     * @return DateTime in Zuora's timezone
     */
    public static DateTime toDateTime(DateTime dateTime) {
        return dateTime != null ? dateTime.toDateTime(ZUORA_TZ) : null;
    }

    /**
     * Converts DateTime to Zuora's Date
     * @param dateTime DateTime to convert to Zuora's timezone
     * @return Date in Zuora's timezone
     */
    public static DateTime toDate(DateTime dateTime) {
        return dateTime != null ? toDateTime(dateTime).toLocalDate().toDateTimeAtStartOfDay(ZUORA_TZ) : null;
    }

    /**
     * Converts DateTime to day of the month
     * @param dateTime DateTime to convert
     * @return day of the month in Zuora's timezone
     */
    public static int toDayOfMonth(DateTime dateTime) {
        return dateTime == null ? dayOfMonth() : toDateTime(dateTime).getDayOfMonth();
    }
}
