package com.antalex.service;

import com.antalex.model.DataChart;

import java.math.BigDecimal;

public interface IndicatorService {
    void init();
    void calcAll(DataChart data);
    BigDecimal calc(String indicator, Integer period);
}
