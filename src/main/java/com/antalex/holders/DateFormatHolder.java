package com.antalex.holders;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public static Date getDateFromString(String sDate) {
        if (sDate == null || sDate.isEmpty()) {
            return null;
        }
        try {
            return dateFormatThreadLocal.get().parse(sDate.substring(0, 14 - approximationThreadLocal.get()));
        } catch (ParseException e) {
            log.error("Не верный формат даты " + sDate);
            e.printStackTrace();
        }
        return null;
    }

    public static String getTimeString(String sDate) {
        return sDate.substring(8, 14);
    }

    private static Date getMinDate(Date date1, Date date2) {
        return Optional.ofNullable(date2).orElse(new Date()).compareTo(date1) < 0 ? date2 : date1;
    }

    public static List<Pair<String, String>> splitDate(String sDateBegin,
                                                       String sDateEnd,
                                                       String startTime,
                                                       String endTime)
    {
        Integer oldApproximation = getApproximation();
        setApproximation(0);
        List<Pair<String, String>> result = new ArrayList<>();

        Date dateBegin = getDateFromString(sDateBegin);
        String startTimeString = getTimeString(sDateBegin);
        if (startTimeString.compareTo(endTime) > 0) {
            dateBegin = setTimeToDate(getNextDate(dateBegin), startTime);
        }
        if (startTimeString.compareTo(startTime) < 0) {
            dateBegin = setTimeToDate(dateBegin, startTime);
        }
        Date dateEnd = DateFormatHolder.getDateFromString(sDateEnd);
        Date nextDate = getMinDate(setTimeToDate(dateBegin, endTime), dateEnd);
        while (Objects.nonNull(nextDate) && dateBegin.compareTo(nextDate) < 0) {
            result.add(
                    new Pair<>(
                            DateFormatHolder.getStringFromDate(dateBegin),
                            DateFormatHolder.getStringFromDate(nextDate)
                    )
            );
            dateBegin = getNextDate(dateBegin);
            if (startTimeString.compareTo(startTime) > 0) {
                dateBegin = setTimeToDate(dateBegin, startTime);
                startTimeString = startTime;
            }
            nextDate = getMinDate(setTimeToDate(dateBegin, endTime), dateEnd);
        }
        if (Objects.isNull(nextDate)) {
            result.add(
                    new Pair<>(DateFormatHolder.getStringFromDate(dateBegin), "")
            );
        }

        setApproximation(oldApproximation);
        return result;
    }

    private static Date getNextDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    private static Date setTimeToDate(Date date, String time) {
        return getDateFromString(getStringFromDate(date).substring(0, 8).concat(time));
    }
}
