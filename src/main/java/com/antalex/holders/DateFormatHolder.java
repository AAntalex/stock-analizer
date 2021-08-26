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
        return Optional.ofNullable(approximationThreadLocal.get()).orElse(0);
    }

    public static void setApproximation(Integer approximation) {
        approximation = Optional.ofNullable(approximation).orElse(0);
        if (getApproximation().equals(approximation) || approximation < 0 || approximation > 10) {
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
            return dateFormatThreadLocal.get().parse(
                    sDate.substring(
                            0,
                            Integer.min(14 - getApproximation(), sDate.length())
                    )
            );
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
                                                       String endTime,
                                                       int calendarType)
    {
        Integer oldApproximation = getApproximation();
        setApproximation(0);
        List<Pair<String, String>> result = new ArrayList<>();

        Date dateBegin = getDateFromString(sDateBegin);
        String startTimeString = getTimeString(sDateBegin);
        if (startTimeString.compareTo(endTime) > 0) {
            dateBegin = setTimeToDate(getNextDate(dateBegin, Calendar.DATE, 1), startTime);
        }
        if (startTimeString.compareTo(startTime) < 0) {
            dateBegin = setTimeToDate(dateBegin, startTime);
        }
        Date dateEnd = DateFormatHolder.getDateFromString(sDateEnd);
        Date nextDate = getMinDate(getEndPeriod(dateBegin, endTime, calendarType), dateEnd);
        while (Objects.nonNull(nextDate) && dateBegin.compareTo(nextDate) < 0) {
            result.add(
                    new Pair<>(
                            DateFormatHolder.getStringFromDate(dateBegin),
                            DateFormatHolder.getStringFromDate(nextDate)
                    )
            );
            dateBegin = getNextDate(dateBegin, calendarType, 1);
            if (startTimeString.compareTo(startTime) > 0) {
                dateBegin = setTimeToDate(dateBegin, startTime);
                startTimeString = startTime;
            }
            nextDate = getMinDate(getEndPeriod(dateBegin, endTime, calendarType), dateEnd);
        }
        if (Objects.isNull(nextDate)) {
            result.add(
                    new Pair<>(DateFormatHolder.getStringFromDate(dateBegin), "")
            );
        }

        setApproximation(oldApproximation);
        return result;
    }

    private static Date getEndPeriod(Date date, String endTime, int calendarType) {
        if (calendarType == Calendar.DATE) {
            return setTimeToDate(date, endTime);
        }
        return setTimeToDate(
                getNextDate(
                        getNextDate(date, calendarType, 1),
                        Calendar.DATE,
                        -1
                ),
                endTime
        );
    }

    public static Date setTimeToDate(Date date, String time) {
        return getDateFromString(getStringFromDate(date).substring(0, 8).concat(time));
    }

    public static Date getNextDate(Date date, int type, int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(type, offset);
        return calendar.getTime();
    }
}
