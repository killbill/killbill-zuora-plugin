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
