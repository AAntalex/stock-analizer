package com.antalex.model;

import com.antalex.holders.DataHolder;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@AllArgsConstructor
public class TrendLine {
    private Integer x1;
    private BigDecimal y1;
    private Integer x2;
    private BigDecimal y2;
    private BigDecimal alpha;
    private BigDecimal betta;

    TrendLine(Integer x1, BigDecimal y1, Integer x2, BigDecimal y2, Boolean isHigh) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public BigDecimal getAlpha() {
        this.alpha = Optional.ofNullable(this.alpha)
                .orElse(y2.subtract(y1).divide(BigDecimal.valueOf(x2 - x1), DataHolder.PRECISION, RoundingMode.HALF_UP));
        return this.alpha;
    }

    public BigDecimal getBetta() {
        this.betta = Optional.ofNullable(this.betta)
                .orElse(y1.subtract(getAlpha().multiply(BigDecimal.valueOf(x1))));
        return this.betta;
    }

    public BigDecimal f(Integer x) {
        return getAlpha().multiply(BigDecimal.valueOf(x)).add(getBetta());
    }

    protected void setX1(Integer x, BigDecimal y) {
        this.alpha = null;
        this.betta = null;
        this.x1 = x;
        this.y1 = y;
    }

    protected void setX2(Integer x, BigDecimal y) {
        this.alpha = null;
        this.betta = null;
        this.x2 = x;
        this.y2 = y;
    }

    protected Integer getX1() {
        return this.x1;
    }

    protected Integer getX2() {
        return this.x2;
    }
}
