package com.antalex.holders;

import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

public class DataChartHolder {
    private DataChartHolder() {
        throw new IllegalStateException("Cache holder class!!!");
    }

    public static Boolean isTest() {
        Boolean result = (Boolean) RequestContextHolder.getRequestAttributes()
                .getAttribute("test", SCOPE_REQUEST);
        return Optional.ofNullable(result).orElse(false);
    }

    public static void setTest(Boolean test) {
        RequestContextHolder.getRequestAttributes().setAttribute("test", test, SCOPE_REQUEST);
    }

    public static Boolean isCalcCorr() {
        Boolean result = (Boolean) RequestContextHolder.getRequestAttributes()
                .getAttribute("calcCorr", SCOPE_REQUEST);
        return Optional.ofNullable(result).orElse(false);
    }

    public static void setCalcCorr(Boolean test) {
        RequestContextHolder.getRequestAttributes().setAttribute("calcCorr", test, SCOPE_REQUEST);
    }
}
