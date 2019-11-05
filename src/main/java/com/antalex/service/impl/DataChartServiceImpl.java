package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.*;
import com.antalex.persistence.entity.IndicatorValueEntity;
import com.antalex.service.DataChartService;
import com.antalex.service.TrendService;
import com.udojava.evalex.Expression;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataChartServiceImpl implements DataChartService {
    private static final String VOL = "VOL";
    private static final String OPEN = "OPEN";
    private static final String CLOSE = "CLOSE";
    private static final String HIGH = "HIGH";
    private static final String LOW = "LOW";
    private static final String BID_VOL = "BID_VOL";
    private static final String BID_OPEN = "BID_OPEN";
    private static final String BID_CLOSE = "BID_CLOSE";
    private static final String BID_HIGH = "BID_HIGH";
    private static final String BID_LOW = "BID_LOW";
    private static final String BID_UP = "BID_UP";
    private static final String BID_DOWN = "BID_DOWN";
    private static final String OFFER_UP = "OFFER_UP";
    private static final String OFFER_DOWN = "OFFER_DOWN";
    private static final String OFFER_VOL = "OFFER_VOL";
    private static final String OFFER_OPEN = "OFFER_OPEN";
    private static final String OFFER_CLOSE = "OFFER_CLOSE";
    private static final String OFFER_HIGH = "OFFER_HIGH";
    private static final String OFFER_LOW = "OFFER_LOW";
    private static final String TREND = "TREND";
    private static final String ALPHA = "ALPHA";
    private CacheDadaChart cache;

    private final TrendService trendService;

    DataChartServiceImpl(TrendService trendService) {
        this.trendService = trendService;
    }

    @Override
    public CacheDadaChart getCache() {
        if (this.cache == null) {
            this.cache = new CacheDadaChart();
        }
        return this.cache;
    }

    @Override
    public void dropCache() {
        this.cache = new CacheDadaChart();
    }

    @Override
    public BigDecimal getValue(DataChart data, String variable) {
        switch (variable.toUpperCase()) {
            case VOL: {
                return new BigDecimal(data.getData().getVolume());
            }
            case HIGH: {
                return data.getData().getCandle().getHigh();
            }
            case LOW: {
                return data.getData().getCandle().getLow();
            }
            case CLOSE: {
                return data.getData().getCandle().getClose();
            }
            case OPEN: {
                return data.getData().getCandle().getOpen();
            }
            case BID_VOL: {
                return Optional.ofNullable(data.getDataBid())
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_HIGH: {
                return Optional.ofNullable(data.getDataBid())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getHigh)
                        .orElse(null);
            }
            case BID_LOW: {
                return Optional.ofNullable(data.getDataBid())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getLow)
                        .orElse(null);
            }
            case BID_CLOSE: {
                return Optional.ofNullable(data.getDataBid())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getClose)
                        .orElse(null);
            }
            case BID_OPEN: {
                return Optional.ofNullable(data.getDataBid())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getOpen)
                        .orElse(null);
            }
            case OFFER_VOL: {
                return Optional.ofNullable(data.getDataOffer())
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_HIGH: {
                return Optional.ofNullable(data.getDataOffer())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getHigh)
                        .orElse(null);
            }
            case OFFER_LOW: {
                return Optional.ofNullable(data.getDataOffer())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getLow)
                        .orElse(null);
            }
            case OFFER_CLOSE: {
                return Optional.ofNullable(data.getDataOffer())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getClose)
                        .orElse(null);
            }
            case OFFER_OPEN: {
                return Optional.ofNullable(data.getDataOffer())
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getOpen)
                        .orElse(null);
            }
            case BID_UP: {
                return Optional.ofNullable(data.getBidUp()).orElse(BigDecimal.ZERO);
            }
            case BID_DOWN: {
                return Optional.ofNullable(data.getBidDown()).orElse(BigDecimal.ZERO);
            }
            case OFFER_UP: {
                return Optional.ofNullable(data.getOfferUp()).orElse(BigDecimal.ZERO);
            }
            case OFFER_DOWN: {
                return Optional.ofNullable(data.getOfferDown()).orElse(BigDecimal.ZERO);
            }
            default: {
                if (data.getIndicators().containsKey(variable)) {
                    return data.getIndicators().get(variable).getValue();
                }
                if (!data.getIsLast() && data.getPrev() != null &&
                        data.getPrev().getIndicators().containsKey(variable)) {
                    return data.getPrev().getIndicators().get(variable).getValue();
                }
                if (variable.startsWith(TREND)) {
                    return getTrendValue(data, variable);
                }
                return null;
            }
        }
    }

    private HashMap<String, Indicator> getIndicators(DataChart data) {
        if (!data.getIsLast() && data.getPrev() != null) {
            return data.getPrev().getIndicators();
        }
        return data.getIndicators();
    }

    @Override
    public List<IndicatorValueEntity> getIndicatorValues(DataChart data) {
        return getIndicators(data).values()
                .stream()
                .map(it -> {
                    IndicatorValueEntity entity = new IndicatorValueEntity();
                    entity.setCode(it.getCode());
                    entity.setValue(it.getValue());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Boolean getBool(DataChart data, String boolExpression) {
        Expression expression = new Expression(
                String.format("IF(%s,1,0)",
                        boolExpression
                                .replace(String.valueOf(" "), "")
                                .replace('.', '_'))
        );
        expression.setPrecision(DataHolder.PRECISION).setRoundingMode(RoundingMode.HALF_UP);
        expression.getUsedVariables()
                .stream()
                .distinct()
                .forEach(it -> expression.and(it, getValue(data, it)));
        return expression.eval().compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal getTrendValue(DataChart data, String variable) {
        String trendCode = null;
        Boolean isHigh = null;
        Boolean isAlpha = false;
        int offset = 0;
        int period = 0;
        for (String variablePart : variable.split("_")) {
            if (trendCode == null) {
                trendCode = variablePart;
                if (trendCode.length() > 5) {
                    period = Integer.parseInt(trendCode.substring(5));
                }
                continue;
            }
            if (isHigh == null && (LOW.equals(variablePart) || HIGH.equals(variablePart))) {
                isHigh = HIGH.equals(variablePart);
                continue;
            }
            if (ALPHA.equals(variablePart)) {
                isAlpha = true;
                continue;
            }
            offset = Integer.parseInt(variablePart);
        }
        if (isHigh == null) {
            throw new IllegalStateException("Incorrect format of variable TREND");
        }
        String code = trendService.getTrendCode(period, offset);
        Trend trend = DataHolder.trend(code);
        if (trend == null) {
            trend = trendService.getTrend(this.cache.getDataList(), period, offset);
        }
        if (isAlpha) {
            return isHigh ? trend.getHigh().getAlpha() : trend.getLow().getAlpha();
        }
        return isHigh ? trend.getHigh().f(data.getIdx()) : trend.getLow().f(data.getIdx());
    }
}
