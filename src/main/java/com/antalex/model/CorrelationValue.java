package com.antalex.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CorrelationValue {
    private BigDecimal value;
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal allResult;
    private BigDecimal result;
    private Integer size;
}
