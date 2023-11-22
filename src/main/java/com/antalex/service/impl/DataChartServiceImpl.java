package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.holders.DateFormatHolder;
import com.antalex.model.*;
import com.antalex.model.enums.StatusType;
import com.antalex.model.enums.VariableType;
import com.antalex.persistence.entity.*;
import com.antalex.service.DataChartService;
import com.antalex.service.TrendService;
import com.google.common.base.Enums;
import com.udojava.evalex.Expression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class DataChartServiceImpl implements DataChartService {
    private static final String TREND = "TREND";
    private static final String ALPHA = "ALPHA";
    private static final String BETTA = "BETTA";
    private static final String WEIGHT = "WEIGHT";
    private static final String PREV = "_PREV";
    private static final Map<ClassSecEntity, CacheDadaChart> allCache = new HashMap<>();
    private static final List<ClassSecEntity> classesList = new ArrayList<>();
    private CacheDadaChart cache;
    private Boolean trace = false;

    private final TrendService trendService;

    DataChartServiceImpl(TrendService trendService) {
        this.trendService = trendService;
    }

    @Override
    public CacheDadaChart getCache() {
        if (this.cache == null) {
            throw new IllegalStateException("Not init cache!!!");
        }
        return this.cache;
    }

    @Override
    public Map<ClassSecEntity, CacheDadaChart> getAllCache() {
        return allCache;
    }

    @Override
    public CacheDadaChart getCache(ClassSecEntity sec) {
        CacheDadaChart cache = allCache.get(sec);
        if (Objects.isNull(cache)) {
            cache = new CacheDadaChart();
            allCache.put(sec, cache);
            classesList.add(sec);
        }
        return cache;
    }

    @Override
    public Stream<Map.Entry<ClassSecEntity, CacheDadaChart>> getAdditionalCache() {
        return allCache
                .entrySet()
                .stream()
                .filter(it -> !it.getKey().equals(classesList.get(0)));
    }

    @Override
    public void setCurCache(ClassSecEntity sec) {
        if (Objects.isNull(sec)) {
            this.cache = getCache(classesList.get(0));
        } else {
            this.cache = getCache(sec);
        }
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
        allCache.clear();
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
                event.getStatus() == StatusType.ENABLED &&
                !event.getTriggers().isEmpty() &&
                event.getTriggers()
                        .stream()
                        .filter(it -> it.getStatus() != StatusType.DISABLED)
                        .allMatch(it -> getBool(data, it.getTrigger().getCondition()))
                ;
    }

    private VariableType getVariableType(String variable) {
        return Enums.getIfPresent(VariableType.class, variable).or(VariableType.DEFAULT);
    }

    private BigDecimal calcValue(DataChart data, String variable) {
        if (data == null) {
            return null;
        }
        variable = variable.toUpperCase();
        switch (getVariableType(variable)) {
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
            case TIME: {
                return Optional.ofNullable(data.getHistory())
                        .map(AllHistoryRpt::getUno)
                        .map(it -> new BigDecimal(DateFormatHolder.getTimeString(it)))
                        .orElse(BigDecimal.ZERO);
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
                    String prevVariable = variable.substring(0, variable.length() - 5);
                    return calcValue(
                            data.getCalcIndicator() || getVariableType(prevVariable) != VariableType.DEFAULT
                                    ? data.getPrev()
                                    : Optional.ofNullable(data.getPrev()).map(DataChart::getPrev).orElse(null) ,
                            prevVariable
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
        Boolean isBetta = false;
        Boolean isWeight = false;
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
            if (isHigh == null && (VariableType.LOW.name().equals(variablePart) || VariableType.HIGH.name().equals(variablePart))) {
                isHigh = VariableType.HIGH.name().equals(variablePart);
                continue;
            }
            if (ALPHA.equals(variablePart)) {
                isAlpha = true;
                continue;
            }
            if (BETTA.equals(variablePart)) {
                isBetta = true;
                continue;
            }
            if (WEIGHT.equals(variablePart)) {
                isWeight = true;
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
        if (isBetta) {
            return isHigh ? trend.getHigh().getBetta() : trend.getLow().getBetta();
        }
        if (isWeight) {
            return isHigh ? trend.getHighWeight() : trend.getLowWeight();
        }
        return isHigh ? trend.getHigh().f(data.getIdx()) : trend.getLow().f(data.getIdx());
    }
}
