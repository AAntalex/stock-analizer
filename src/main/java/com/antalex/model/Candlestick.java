package com.antalex.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Candlestick {
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
}
