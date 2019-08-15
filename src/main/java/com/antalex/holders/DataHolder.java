package com.antalex.holders;

import com.antalex.model.DataChart;

import java.math.BigDecimal;
import java.util.*;

public class DataHolder {
    private DataHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    private static final ThreadLocal<List<DataChart>> dataThreadLocal = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<BigDecimal> periodThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, BigDecimal>> processedIndicatorThreadLocal = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<DataChart> firstDataThreadLocal = new ThreadLocal<>();

    public static void setData(DataChart data) {
        dataThreadLocal.get().clear();
        processedIndicatorThreadLocal.get().clear();
        dataThreadLocal.get().add(data);
    }

    public static DataChart data() {
        return data(period().intValue());
    }

    public static DataChart data(int index) {
        if (dataThreadLocal.get().isEmpty()) {
            return null;
        }
        index = period().intValue() - index;
        for (int i = dataThreadLocal.get().size(); i <= index; i++) {
            DataChart data = dataThreadLocal.get().get(i - 1);
            if (data.getPrev() == null) {
                return null;
            }
            dataThreadLocal.get().add(data.getPrev());
        }
        return dataThreadLocal.get().get(index);
    }

    public static void setPeriod(Integer period) {
        periodThreadLocal.set(BigDecimal.valueOf(period));
    }

    public static BigDecimal period() {
        return periodThreadLocal.get();
    }

    public static BigDecimal getIndicator(String indicator) {
        BigDecimal result = processedIndicatorThreadLocal.get().get(indicator);
        if (result == null) {
            if (processedIndicatorThreadLocal.get().containsKey(indicator)) {
                throw new IllegalStateException(String.format("Indicator %s was processed!!!", indicator));
            }
            processedIndicatorThreadLocal.get().put(indicator, null);
        }
        return result;
    }

    public static void setIndicator(String indicator, BigDecimal value) {
        processedIndicatorThreadLocal.get().put(indicator, value);
    }

    public static DataChart firstData() {
        return firstDataThreadLocal.get();
    }

    public static void setFirstData(DataChart data) {
        firstDataThreadLocal.set(data);
    }
}
