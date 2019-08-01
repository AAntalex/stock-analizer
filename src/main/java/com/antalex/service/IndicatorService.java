package com.antalex.service;

import com.antalex.model.DataChart;

import java.math.BigDecimal;

public interface IndicatorService {
    BigDecimal calc(DataChart data, String indicator, Integer period);

    default BigDecimal calc(DataChart data, String indicator) {
        return calc(data, indicator, 0);
    }
}
