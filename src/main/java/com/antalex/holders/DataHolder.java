package com.antalex.holders;

import com.antalex.model.DataChart;
import com.antalex.model.Trend;

import java.math.BigDecimal;
import java.util.*;

public class DataHolder {
    public static final int PRECISION = 16;
    private DataHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }
    private static final String NOT_ENOUGH_PERIODS = "Not enough periods for calc %d. Need %d periods.";
    private static final String EMPTY_DATA = "Data for calc is not set.";

    private static final ThreadLocal<List<DataChart>> dataThreadLocal = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<BigDecimal> periodThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, BigDecimal>> processedIndicatorThreadLocal = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<DataChart> firstDataThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, Trend>> trendsThreadLocal = ThreadLocal.withInitial(HashMap::new);

    public static void setData(DataChart data) {
        dataThreadLocal.get().clear();
        processedIndicatorThreadLocal.get().clear();
        dataThreadLocal.get().add(data);
        trendsThreadLocal.get().clear();
    }

    public static DataChart data() {
        return data(period().intValue());
    }

    public static List<DataChart> dataList() {
        return dataThreadLocal.get();
    }

    public static DataChart data(int index) {
        if (dataThreadLocal.get().isEmpty()) {
            throw new IllegalStateException(EMPTY_DATA);
        }
        if (index <= 0 && !addData(index)) {
            return null;
        }
        return dataThreadLocal.get().get(period().intValue() - index);
    }


    private static Boolean addData(Integer period) {
        if (dataThreadLocal.get().isEmpty()) {
            throw new IllegalStateException(EMPTY_DATA);
        }
        int maxIndex = period().intValue() - period;
        if (maxIndex > 0) {
            for (int i = dataThreadLocal.get().size() - 1; i < maxIndex; i++) {
                DataChart data = dataThreadLocal.get().get(i);
                data = data.getPrev();
                if (data == null) {
                    return false;
                }
                dataThreadLocal.get().add(data);
            }
        }
        return true;
    }

    public static void setPeriod(Integer period) {
        periodThreadLocal.set(BigDecimal.valueOf(period));
        if (!addData(1)) {
            throw new IllegalStateException(String.format(NOT_ENOUGH_PERIODS, dataThreadLocal.get().size(), period));
        }
    }

    public static BigDecimal period() {
        return periodThreadLocal.get();
    }

    public static BigDecimal getIndicator(String indicator) {
        BigDecimal result = processedIndicatorThreadLocal.get().get(indicator);
        if (result == null) {
            if (processedIndicatorThreadLocal.get().containsKey(indicator)) {
                return BigDecimal.ZERO;
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

    public static void setTrend(String trendCode, Trend trend) {
        trendsThreadLocal.get().put(trendCode, trend);
    }

    public static Trend trend(String trendCode) {
        return trendsThreadLocal.get().get(trendCode);
    }

}
