package com.antalex.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Trend {
    private Integer start;
    private TrendLine high;
    private TrendLine low;
    private Integer end;
    private Integer period;
    private Integer offset;
    private List<Candlestick> points = new ArrayList<>();

    public Trend(
            Integer start,
            Candlestick startCandle,
            Integer end,
            Candlestick endCandle,
            Integer period,
            Integer offset
    ) {
        this.start = start;
        this.end = end;
        this.period = period;
        this.offset = offset;
        this.high = new TrendLine(start, startCandle.getHigh(), end, endCandle.getHigh(), true);
        this.low = new TrendLine(start, startCandle.getLow(), end, endCandle.getLow(), false);
    }

    public Boolean checkPointHigh(Integer x, Candlestick y) {
        return this.high.f(x).compareTo(y.getHigh()) >= 0;
    }

    public Boolean checkPointLow(Integer x, Candlestick y) {
        return this.low.f(x).compareTo(y.getLow()) <= 0;
    }

    public Boolean checkPoint(Integer x, Candlestick y) {
        return checkPointHigh(x, y) && checkPointLow(x, y);
    }

    private Integer getIdx(Integer idx) {
        return idx - this.start;
    }

    public void setPoint(Integer x, Candlestick y) {
        this.points.add(y);
        if (x.equals(this.start) || x.equals(this.end)) {
            return;
        }
        if (x > this.end) {
            this.end = x;
        }
        if (checkPointHigh(x, y)) {
            if (isRightPoint(x) && !isRightPoint(this.high.getX2())) {
                this.high.setX1(this.high.getX2(), this.points.get(getIdx(this.high.getX2())).getHigh());
                this.high.setX2(x, y.getHigh());
                for (int i = this.high.getX1(); i < x; i++) {
                    if (!checkPointHigh(i, points.get(getIdx(i)))) {
                        this.high.setX2(i, points.get(getIdx(i)).getHigh());
                    }
                }
            }
        } else {
            if (isRightPoint(x)) {
                this.high.setX2(x, y.getHigh());
                for (int i = this.high.getX1(); i >= this.start; i--) {
                    if (!checkPointHigh(i, points.get(getIdx(i)))) {
                        this.high.setX1(i, points.get(getIdx(i)).getHigh());
                    }
                }
            } else {
                this.high.setX1(x, y.getHigh());
            }
        }
        if (checkPointLow(x, y)) {
            if (isRightPoint(x) && !isRightPoint(this.low.getX2())) {
                this.low.setX1(this.low.getX2(), this.points.get(getIdx(this.low.getX2())).getLow());
                this.low.setX2(x, y.getLow());
                for (int i = this.low.getX1(); i < x; i++) {
                    if (!checkPointLow(i, points.get(getIdx(i)))) {
                        this.low.setX2(i, points.get(getIdx(i)).getLow());
                    }
                }
            }
        } else {
            if (isRightPoint(x)) {
                this.low.setX2(x, y.getLow());
                for (int i = this.low.getX1(); i >= this.start; i--) {
                    if (!checkPointLow(i, points.get(getIdx(i)))) {
                        this.low.setX1(i, points.get(getIdx(i)).getLow());
                    }
                }
            } else {
                this.low.setX1(x, y.getLow());
            }
        }
    }

    private Boolean isRightPoint(Integer x) {
        return x > (this.start + this.end) / 2;
    }

}
