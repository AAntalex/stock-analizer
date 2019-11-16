package com.antalex.model;

import com.antalex.persistence.entity.IndicatorEntity;
import com.udojava.evalex.Function;
import com.udojava.evalex.LazyFunction;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class IndicatorExpression {
    private static final String TEMP_VARIABLE = "X";

    private String tempVariable;
    private String expression;
    private IndicatorEntity indicatorEntity;
    private List<String> variables = new ArrayList<>();
    private List<Function> functions = new ArrayList<>();
    private List<LazyFunction> lazyFunctions = new ArrayList<>();

    public IndicatorExpression(IndicatorEntity indicatorEntity) {
        this.indicatorEntity = indicatorEntity;
    }

    public IndicatorExpression addFunction(Function function) {
        this.functions.add(function);
        return this;
    }

    public IndicatorExpression addLazyFunctions (LazyFunction function) {
        this.lazyFunctions.add(function);
        return this;
    }

    public IndicatorExpression addVariables(String variable) {
        this.variables.add(variable);
        return this;
    }

    public String getVariableName(String variableName) {
        if (variableName == null) {
            return null;
        }
        if (TEMP_VARIABLE.equals(variableName.toUpperCase())) {
            return this.tempVariable;
        }
        return variableName;
    }
}
