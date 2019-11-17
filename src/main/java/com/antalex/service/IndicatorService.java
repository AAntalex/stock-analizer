package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.Trend;

import java.math.BigDecimal;
import java.util.List;

public interface IndicatorService {
    void init();
    void calcAll(DataChart data);
    BigDecimal calc(String indicator, Integer period);
    void setTrendToIndicator(Trend trend, List<DataChart> dataList, Boolean multiple);
}
