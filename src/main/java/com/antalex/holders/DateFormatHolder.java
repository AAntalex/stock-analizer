package com.antalex.holders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static void setApproximation(int approximation) {
        if (approximationThreadLocal.get() != null && approximationThreadLocal.get() == approximation || approximation < 0 || approximation > 10) {
            return;
        }
        approximationThreadLocal.set(approximation);
        dateFormatThreadLocal.set(new SimpleDateFormat(DATE_FORMAT.substring(0, 14 - approximation)));
    }


    public static Date getDateFromString(String sDate) {
        if (sDate == null || sDate.isEmpty()) {
            return null;
        }
        try {
            return dateFormatThreadLocal.get().parse(sDate.substring(0, 14 - approximationThreadLocal.get()));
        } catch (ParseException e) {
            System.out.println("Не верный формат даты " + sDate);
            e.printStackTrace();
        }
        return null;
    }
}
