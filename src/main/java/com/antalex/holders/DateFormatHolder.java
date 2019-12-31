package com.antalex.holders;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateFormatHolder {
    private DateFormatHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private static final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat(DATE_FORMAT));
    private static final ThreadLocal<Integer> approximationThreadLocal = new ThreadLocal<>();

    public static SimpleDateFormat getDateFormat() {
        return dateFormatThreadLocal.get();
    }

    public static Integer getApproximation() {
        return approximationThreadLocal.get();
    }

    public static void setApproximation(Integer approximation) {
        if (approximationThreadLocal.get() != null && approximationThreadLocal.get() == approximation || approximation < 0 || approximation > 10) {
            return;
        }
        approximationThreadLocal.set(approximation);
        dateFormatThreadLocal.set(new SimpleDateFormat(DATE_FORMAT.substring(0, 14 - approximation)));
    }

    public static String getStringFromDate(Date date) {
        return dateFormatThreadLocal.get().format(date);
    }

    public static Date getDateFromString(String sDate, Integer approximation) {
        if (sDate == null || sDate.isEmpty()) {
            return null;
        }
        try {
            return dateFormatThreadLocal.get().parse(sDate.substring(0, 14 - approximation));
        } catch (ParseException e) {
            log.error("Не верный формат даты " + sDate);
            e.printStackTrace();
        }
        return null;
    }

    public static Date getDateFromString(String sDate) {
        return getDateFromString(sDate, approximationThreadLocal.get());
    }
}
