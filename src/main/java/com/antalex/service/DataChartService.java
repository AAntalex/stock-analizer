package com.antalex.service;

import com.antalex.model.DataChart;

import java.math.BigDecimal;

public interface DataChartService {
    BigDecimal getValue(DataChart data, String variable);
    Boolean getBool(DataChart data, String boolExpression);
}
