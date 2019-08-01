package com.antalex.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Indicator {
    private BigDecimal value;
    private Boolean isPublic;
    private String name;
}
