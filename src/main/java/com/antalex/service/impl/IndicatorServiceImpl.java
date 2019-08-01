package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.DataGroup;
import com.antalex.service.IndicatorService;
import com.udojava.evalex.AbstractFunction;
import com.udojava.evalex.Expression;
import com.udojava.evalex.Function;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class IndicatorServiceImpl implements IndicatorService {
    private static final String INDEX = "I";
    private static final String START_INDEX = "(I";
    private static final Map<String, Expression> INDICATORS = new HashMap<>();

    @Override
    public BigDecimal calc(DataChart data, String indicator, Integer period) {
        if (!INDICATORS.containsKey(indicator)) {
            String expressionText = "ALLVOL(i-1)+VOL";


            INDICATORS.put(indicator, getIndicatorExpression(expressionText));
        }
        Expression expression = INDICATORS.get(indicator).with(INDEX, BigDecimal.valueOf(period));
        getVariables(expression)
                .stream()
                .distinct()
                .forEach(it -> expression.and(it, getVariableValue(it, data)));

        return expression.eval();
    }

    private Expression getIndicatorExpression(String expressionText) {
        expressionText = expressionText
                .replace(' ', Character.MIN_VALUE)
                .toUpperCase();

        List<String> functions = new ArrayList<>();
        int pos = 0;
        int len = expressionText.length();
        int startLen = START_INDEX.length();
        StringBuilder expressionBuilder = new StringBuilder();
        for (String var: getAllVariables(expressionText)) {
            if (pos < len) {
                int curPos = expressionText.indexOf(var, pos) + var.length();
                expressionBuilder.append(expressionText.substring(pos, curPos));
                pos = curPos;
                if (curPos + startLen < len && START_INDEX.equals(expressionText.substring(curPos, curPos + startLen))) {
                    expressionBuilder
                            .append(START_INDEX)
                            .append(',')
                            .append(INDEX);
                    pos = pos + startLen;
                    functions.add(var);
                }
            }
        }
        if (pos < len) {
            expressionBuilder.append(expressionText.substring(pos));
        }
        Expression expression = new Expression(expressionBuilder.toString());
        functions
                .stream()
                .distinct()
                .map(this::createFunctionForIndicator)
                .forEach(expression::addFunction);

        return expression;
    }

    private List<String> getAllVariables(String expressionText) {
        Expression expression = new Expression(
                expressionText
                        .replace('(', '[')
                        .replace(')', ']')
        );
        return getVariables(expression);
    }

    private List<String> getVariables(Expression expression) {
        return expression
                .getUsedVariables()
                .stream()
                .filter(it -> !INDEX.equals(it))
                .collect(Collectors.toList());
    }

    private BigDecimal getVariableValue(String variable, DataChart data) {
        switch (variable) {
            case "VOL": {
                return Optional
                        .ofNullable(data)
                        .map(DataChart::getData)
                        .map(DataGroup::getVolume)
                        .map(BigDecimal::new)
                        .orElse(BigDecimal.ZERO);
            }
            default: {
                throw new Expression.ExpressionException(String.format("Unknown variable %s", variable));
            }
        }
    }

    private Function createFunctionForIndicator(String function) {
        return new AbstractFunction(function, 2) {
            @Override
            public BigDecimal eval(List<BigDecimal> parameters) {
                if (parameters.size() != 2) {
                    throw new Expression.ExpressionException("Requires two parameters");
                }
                BigDecimal n = parameters.get(0);
                BigDecimal i = parameters.get(1);

                return i.add(n);
            }
        };
    }

}
