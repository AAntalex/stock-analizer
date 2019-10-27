package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.Trend;

import java.util.List;

public interface TrendService {
    Trend getTrend(List<DataChart> dataList, Integer period, Integer offset);
    void setTrendToIndicator(Trend trend, List<DataChart> dataList);
}
