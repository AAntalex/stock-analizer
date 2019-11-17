package com.antalex.service;

import com.antalex.model.CacheDadaChart;
import com.antalex.model.DataChart;
import com.antalex.model.Trend;
import com.antalex.persistence.entity.IndicatorValueEntity;
import com.antalex.persistence.entity.TraceValueEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DataChartService {
    CacheDadaChart getCache();
    void dropCache();
    BigDecimal getValue(DataChart data, String variable);
    Boolean getBool(DataChart data, String boolExpression);
    List<IndicatorValueEntity> getIndicatorValues(DataChart data);
    void startTrace();
    void stopTrace();
    List<TraceValueEntity> getTraceValues();
    Trend getTrend(Integer period, Integer offset);
}
