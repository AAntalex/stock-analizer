package com.antalex.holders;

import com.antalex.model.DataChart;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class DataHolder {
    private DataHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    private static final ThreadLocal<DataChart> dataThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<DataChart> curDataThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BigDecimal> periodThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Set<String>> processedIndicatorThreadLocal = ThreadLocal.withInitial(HashSet::new);

    public static void setData(DataChart data) {
        dataThreadLocal.set(data);
    }

    public static DataChart data() {
        return dataThreadLocal.get();
    }

    public static void setCurData(DataChart data) {
        curDataThreadLocal.set(data);
    }

    public static DataChart curData() {
        return curDataThreadLocal.get();
    }

    public static void setPeriod(Integer period) {
        periodThreadLocal.set(BigDecimal.valueOf(period));
    }

    public static BigDecimal period() {
        return periodThreadLocal.get();
    }

    public static void checkIndicator(String indicator) {
        if (processedIndicatorThreadLocal.get().contains(indicator)) {
            throw new IllegalStateException(String.format("Indicator %s was processed!!!", indicator));
        }
        processedIndicatorThreadLocal.get().add(indicator);
    }
}
