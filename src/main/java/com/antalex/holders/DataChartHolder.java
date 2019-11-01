package com.antalex.holders;

import com.antalex.model.CacheDadaChart;
import org.springframework.web.context.request.RequestContextHolder;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

public class DataChartHolder {
    private DataChartHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    public static void setCacheDataChart(CacheDadaChart data) {
    }

    public static CacheDadaChart cacheDadaChart() {
        return (CacheDadaChart) RequestContextHolder.getRequestAttributes()
                .getAttribute("cacheDadaChart", SCOPE_SESSION);
    }

    public static void setCacheDadaChart(CacheDadaChart cacheDadaChart) {
        RequestContextHolder.getRequestAttributes().setAttribute("cacheDadaChart", cacheDadaChart, SCOPE_SESSION);
    }
}
