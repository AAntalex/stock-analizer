package com.antalex.model;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
public class TrendLine {
    private final static Integer precision = 16;

    private Integer x1;
    private BigDecimal y1;
    private Integer x2;
    private BigDecimal y2;
    private BigDecimal alpha;
    private BigDecimal betta;
    private Boolean isActual = false;

    TrendLine(Integer x1, BigDecimal y1, Integer x2, BigDecimal y2, Boolean isHigh) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    private BigDecimal getAlpha() {
        return y2.subtract(y1).divide(BigDecimal.valueOf(x2 - x1), precision, RoundingMode.HALF_UP);
    }

    private BigDecimal getBetta() {
        return y1.subtract(alpha.multiply(BigDecimal.valueOf(x1)));
    }

    public BigDecimal f(Integer x) {
        actual();
        return alpha.multiply(BigDecimal.valueOf(x)).add(betta);
    }

    private void actual() {
        if (!this.isActual) {
            this.alpha = getAlpha();
            this.betta = getBetta();
        }
    }

    protected void setX1(Integer x, BigDecimal y) {
        this.x1 = x;
        this.y1 = y;
    }

    protected void setX2(Integer x, BigDecimal y) {
        this.x2 = x;
        this.y2 = y;
    }

    protected Integer getX1() {
        return this.x1;
    }
}
