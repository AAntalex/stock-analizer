package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.*;
import com.antalex.persistence.entity.EventEntity;
import com.antalex.persistence.entity.IndicatorValueEntity;
import com.antalex.persistence.entity.TraceValueEntity;
import com.antalex.service.DataChartService;
import com.antalex.service.TrendService;
import com.udojava.evalex.Expression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataChartServiceImpl implements DataChartService {
    private static final String VOL = "VOL";
    private static final String PRICE = "PRICE";
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
    private static final String PREV = "_PREV";
    private CacheDadaChart cache;
    private Boolean trace = false;

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
    public void startTrace() {
        this.cache.getTraceCalc().clear();
        this.trace = true;
    }

    @Override
    public void stopTrace() {
        this.trace = false;
    }

    @Override
    public List<TraceValueEntity> getTraceValues() {
        return this.cache.getTraceCalc()
                .entrySet()
                .stream()
                .map(it -> {
                    TraceValueEntity entity = new TraceValueEntity();
                    entity.setCode(it.getKey());
                    entity.setValue(it.getValue());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void dropCache() {
        this.cache = new CacheDadaChart();
    }

    @Override
    public BigDecimal getValue(DataChart data, String variable) {
        BigDecimal result = calcValue(data, variable);
        if (this.trace) {
            this.cache.getTraceCalc().put(variable, result);
        }
        return result;
    }

    @Override
    public Boolean checkEvent(DataChart data, EventEntity event) {
        return !Objects.isNull(event) &&
                (event.getTriggers().isEmpty() ||
                        event.getTriggers()
                                .stream()
                                .allMatch(it -> getBool(data, it.getTrigger().getCondition()))
                );
    }

    private BigDecimal calcValue(DataChart data, String variable) {
        if (data == null) {
            return null;
        }
        variable = variable.toUpperCase();
        switch (variable) {
            case VOL: {
                return new BigDecimal(data.getData().getVolume());
            }
            case HIGH: {
                return data.getData().getCandle().getHigh();
            }
            case LOW: {
                return data.getData().getCandle().getLow();
            }
            case PRICE:
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
                if (!data.getCalcIndicator() && data.getPrev() != null &&
                        data.getPrev().getIndicators().containsKey(variable)) {
                    return data.getPrev().getIndicators().get(variable).getValue();
                }
                if (variable.startsWith(TREND)) {
                    return getTrendValue(data, variable);
                }
                if (variable.endsWith(PREV)) {
                    return calcValue(
                            data.getCalcIndicator()
                                    ? data.getPrev()
                                    : Optional.ofNullable(data.getPrev()).map(DataChart::getPrev).orElse(null) ,
                            variable.substring(0, variable.length() - 5)
                    );
                }
                return null;
            }
        }
    }

    private HashMap<String, Indicator> getIndicators(DataChart data) {
        if (!data.getCalcIndicator() && data.getPrev() != null) {
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
    public BigDecimal getExpValue(DataChart data, String expressionString) {
        Expression expression = new Expression(normalizeExpression(expressionString));
        expression.setPrecision(DataHolder.PRECISION).setRoundingMode(RoundingMode.HALF_UP);
        for (String variable : expression.getUsedVariables()) {
            BigDecimal value = getValue(data, variable);
            if (value == null) {
                return null;
            }
            expression.and(variable, value);
        }
        return expression.eval();
    }

    @Override
    public Boolean getBool(DataChart data, String boolExpression) {
        return Optional.ofNullable(getExpValue(data, boolExpression))
                .orElse(BigDecimal.ZERO)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String normalizeExpression(String expression) {
        return expression
                .replace(String.valueOf(" "), "")
                .replace("->", "_");
    }

    @Override
    public Trend getTrend(Integer period, Integer offset) {
        String code = trendService.getTrendCode(period, offset);
        Trend trend = DataHolder.trend(code);
        if (trend == null) {
            trend = trendService.getTrend(this.cache.getDataList(), period, offset);
            DataHolder.setTrend(code, trend);
        }
        return trend;
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
        Trend trend = getTrend(period, offset);
        if (trend == null) {
            return null;
        }
        if (isAlpha) {
            return isHigh ? trend.getHigh().getAlpha() : trend.getLow().getAlpha();
        }
        return isHigh ? trend.getHigh().f(data.getIdx()) : trend.getLow().f(data.getIdx());
    }
}
