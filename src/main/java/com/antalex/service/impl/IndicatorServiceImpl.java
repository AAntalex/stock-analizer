package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.*;
import com.antalex.persistence.entity.IndicatorEntity;
import com.antalex.persistence.entity.IndicatorPeriodEntity;
import com.antalex.persistence.repository.IndicatorRepository;
import com.antalex.service.IndicatorService;
import com.udojava.evalex.*;
import com.udojava.evalex.Expression.LazyNumber;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@AllArgsConstructor
public class IndicatorServiceImpl implements IndicatorService {
    private static final String INDEX = "I";
    private static final String PERIOD = "N";
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
    private static final String OFFER_VOL = "OFFER_VOL";
    private static final String OFFER_OPEN = "OFFER_OPEN";
    private static final String OFFER_CLOSE = "OFFER_CLOSE";
    private static final String OFFER_HIGH = "OFFER_HIGH";
    private static final String OFFER_LOW = "OFFER_LOW";

    private static final String START_INDEX = "(I";
    private static final Map<String, IndicatorExpression> INDICATORS = new HashMap<>();

    private static final int PRECISION = 16;

    private final IndicatorRepository indicatorRepository;

    @Override
    public void calcAll(DataChart data) {
        DataHolder.setData(data);

        if (INDICATORS.isEmpty()) {
            indicatorRepository.findAll()
                    .forEach(it -> INDICATORS.put(it.getCode(), getIndicatorExpression(it)));
        }

        INDICATORS.values()
                .stream()
                .map(IndicatorExpression::getIndicatorEntity)
                .forEach(indicator -> indicator.getPeriods()
                        .stream()
                        .map(IndicatorPeriodEntity::getPeriod)
                        .forEach(period -> calc(indicator.getCode(), period))
                );
    }

    @Override
    public BigDecimal calc(String indicator, Integer period) {
        DataHolder.setPeriod(period);
        return evaluate(indicator, period);
    }

    @Override
    public void init() {
        INDICATORS.clear();
    }

    private BigDecimal evaluate(String indicator, Integer index) {
        DataChart data = DataHolder.data(index);
        if (data == null || INDICATORS.containsKey(indicator)
                && DataHolder.period().compareTo(BigDecimal.ZERO) > 0
                && index <= 0 )
        {
            return BigDecimal.ZERO;
        }

        String indicatorCode = Optional
                .ofNullable(INDICATORS.get(indicator))
                .map(
                        indicatorExpression ->
                                Optional.ofNullable(indicatorExpression.getTempVariable())
                                        .map(var -> indicator + '_' + var)
                                        .orElse(indicator)
                ).orElse(indicator);
        indicatorCode = index == 0 ? indicatorCode : indicatorCode + index;

        BigDecimal result = Optional.ofNullable(DataHolder.getIndicator(indicatorCode))
                .orElse(
                        Optional.ofNullable(data.getIndicators().get(index <= 0 ? indicator : indicator + index))
                                .map(Indicator::getValue)
                                .orElseGet(() -> {
                                    if (INDICATORS.containsKey(indicator)) {
                                        if (index >= 0) {
                                            return createExpression(indicator, index).eval();
                                        } else {
                                            return BigDecimal.ZERO;
                                        }
                                    } else {
                                        return getVariableValue(indicator, index);
                                    }
                                })
                );

        DataHolder.setIndicator(indicatorCode, result);

        if (index == DataHolder.period().intValue()) {
            IndicatorEntity indicatorEntity = Optional
                    .ofNullable(INDICATORS.get(indicator))
                    .map(IndicatorExpression::getIndicatorEntity)
                    .orElse(
                            IndicatorEntity
                                    .builder()
                                    .description("Технический (служебный) индикатор")
                                    .code(indicator)
                                    .type(IndicatorType.TECHNICAL)
                                    .build()
                    );
            data.getIndicators().put(indicatorCode,
                    Indicator.builder()
                            .value(result)
                            .code(indicatorCode)
                            .description(indicatorEntity.getDescription())
                            .type(indicatorEntity.getType())
                            .period(DataHolder.period().intValue())
                            .name(indicatorEntity.getCode())
                            .build()
                    );
        }

        return result;
    }

    private Expression createExpression (String indicator, Integer index) {
        IndicatorExpression indicatorExpression = INDICATORS.get(indicator);
        Expression expression = new Expression(indicatorExpression.getIndicatorEntity().getExpression());

        expression.setPrecision(PRECISION).setRoundingMode(RoundingMode.HALF_UP);
        indicatorExpression.getFunctions()
                .forEach(expression::addFunction);
        indicatorExpression.getLazyFunctions()
                .forEach(expression::addLazyFunction);
        indicatorExpression.getVariables()
                .forEach(it -> expression.and(it, getVariableValue(indicatorExpression.getVariableName(it), index)));
        return expression;
    }

    private IndicatorExpression getIndicatorExpression(IndicatorEntity indicatorEntity) {
        String expressionText = indicatorEntity.getExpression()
                .replace(' ', Character.MIN_VALUE)
                .replace('.', '_');

        List<String> functions = new ArrayList<>();
        int pos = 0;
        int len = expressionText.length();
        int startLen = START_INDEX.length();
        for (String var: getAllVariables(expressionText)) {
            if (pos < len) {
                int curPos = expressionText.indexOf(var, pos) + var.length();
                pos = curPos;
                if (curPos + startLen < len &&
                        START_INDEX.equals(expressionText.substring(curPos, curPos + startLen).toUpperCase()))
                {
                    pos = pos + startLen;
                    functions.add(var);
                }
            }
        }
        IndicatorExpression expression = new IndicatorExpression(indicatorEntity);
        expression.addLazyFunctions(sumFunction());
        functions
                .stream()
                .distinct()
                .map(this::createFunctionForIndicator)
                .forEach(expression::addFunction);

        new Expression(expressionText).getUsedVariables()
                .stream()
                .distinct()
                .forEach(expression::addVariables);

        return expression;
    }

    private List<String> getAllVariables(String expressionText) {
        Expression expression = new Expression(
                expressionText
                        .replace('(', '[')
                        .replace(')', ']')
        );
        return expression.getUsedVariables();
    }

    private BigDecimal getVariableValue(String variable, Integer index) {
        DataChart data = DataHolder.data(index);
        switch (variable.toUpperCase()) {
            case VOL: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            case HIGH: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getHigh)
                        .orElse(BigDecimal.ZERO);
            }
            case LOW: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getLow)
                        .orElse(BigDecimal.ZERO);
            }
            case CLOSE: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getClose)
                        .orElse(BigDecimal.ZERO);
            }
            case OPEN: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getOpen)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_VOL: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataBid)
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_HIGH: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataBid)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getHigh)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_LOW: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataBid)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getLow)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_CLOSE: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataBid)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getClose)
                        .orElse(BigDecimal.ZERO);
            }
            case BID_OPEN: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataBid)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getOpen)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_VOL: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataOffer)
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_HIGH: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataOffer)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getHigh)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_LOW: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataOffer)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getLow)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_CLOSE: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataOffer)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getClose)
                        .orElse(BigDecimal.ZERO);
            }
            case OFFER_OPEN: {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getDataOffer)
                        .map(DataGroup::getCandle)
                        .map(Candlestick::getOpen)
                        .orElse(BigDecimal.ZERO);
            }
            case PERIOD: {
                return DataHolder.period();
            }
            case INDEX: {
                return new BigDecimal(index);
            }
            default: {
                if (INDICATORS.containsKey(variable)) {
                    return Optional
                            .ofNullable(data)
                            .map(DataChart::getIndicators)
                            .map(it -> it.get(getIndicatorCode(variable)))
                            .map(Indicator::getValue)
                            .orElse(evaluate(variable, index));
                }
                int pos = variable.indexOf('_');
                if (pos > 0 && INDICATORS.containsKey(variable.substring(0, pos))) {
                    String indicator = variable.substring(0, pos);
                    INDICATORS.get(indicator).setTempVariable(variable.substring(pos + 1));
                    return evaluate(indicator, index);
                }
                throw new Expression.ExpressionException(String.format("Unknown variable %s", variable));
            }
        }
    }

    private Function createFunctionForIndicator(String function) {
        return new AbstractFunction(function, 1) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                return evaluate(function, parameters.get(0).intValue());
            }
        };
    }

    private String getIndicatorCode(String indicatorName) {
        return DataHolder.period().compareTo(BigDecimal.ZERO) > 0
                ? indicatorName + DataHolder.period()
                : indicatorName;
    }

    private LazyFunction sumFunction() {
        return new AbstractLazyFunction("SUM", 1) {
            private BigDecimal result;

            private LazyNumber RESULT = new LazyNumber() {
                public BigDecimal eval() {
                    return result.setScale(PRECISION, RoundingMode.HALF_UP);
                }
                public String getString() {
                    return result.toString();
                }
            };

            @Override
            public LazyNumber lazyEval(List<LazyNumber> list) {
                String variable = list.get(0).getString();
                int n = DataHolder.period().intValue();
                String indicator = getIndicatorCode(variable);
                result = getVariableValue(variable, n)
                        .add(
                                Optional
                                        .ofNullable(DataHolder.data(n - 1))
                                        .map(DataChart::getIndicators)
                                        .map(it -> it.get(indicator))
                                        .map(Indicator::getValue).orElse(BigDecimal.ZERO)
                        );

                if (n > 0) {
                    result = result.subtract(getVariableValue(variable, 0));
                }

                if (!DataHolder.data().getIndicators().containsKey(indicator)) {
                    DataHolder.data().getIndicators().put(indicator,
                            Indicator.builder()
                                    .value(result)
                                    .code(indicator)
                                    .name(variable)
                                    .description("Технический (служебный) индикатор")
                                    .period(n)
                                    .type(IndicatorType.TECHNICAL)
                                    .build()
                    );
                }

                return RESULT;
            }
        };
    }
}
