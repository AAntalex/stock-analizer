package com.antalex.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AnaliseResultRow {
    private String uno;
    private BigDecimal result;
    private List<BigDecimal> factors;
}
