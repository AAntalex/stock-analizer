package com.antalex.service;

import com.antalex.model.CacheDadaChart;
import com.antalex.model.DataChart;
import com.antalex.model.Trend;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.EventEntity;
import com.antalex.persistence.entity.IndicatorValueEntity;
import com.antalex.persistence.entity.TraceValueEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface DataChartService {
    Map<ClassSecEntity, CacheDadaChart> getAllCache();
    CacheDadaChart getCache();
    CacheDadaChart getCache(ClassSecEntity sec);
    void setCurCache(ClassSecEntity sec);
    void dropCache();
    BigDecimal getValue(DataChart data, String variable);
    Boolean getBool(DataChart data, String boolExpression);
    BigDecimal getExpValue(DataChart data, String boolExpression);
    List<IndicatorValueEntity> getIndicatorValues(DataChart data);
    Stream<Map.Entry<ClassSecEntity, CacheDadaChart>> getAdditionalCache();
    void startTrace();
    void stopTrace();
    List<TraceValueEntity> getTraceValues();
    Trend getTrend(Integer period, Integer offset);
    String normalizeExpression(String expression);
    Boolean checkEvent(DataChart data, EventEntity event);
}
