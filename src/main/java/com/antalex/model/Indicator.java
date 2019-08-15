package com.antalex.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Indicator {
    private BigDecimal value;
    private IndicatorType type;
    private String description;
    private String name;
    private String code;
    private Integer period;
}
