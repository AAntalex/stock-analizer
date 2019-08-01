package com.antalex.model;

import com.antalex.persistence.entity.AllTrades;
import lombok.Data;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class CacheDadaChart {
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private final Map<Date, DataChart> data = new HashMap<>();
    private final Map<String, AllTrades> allTrades = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    private int approximation = 0;
    private DataChart lastData;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;

    public void setApproximation(int approximation) {
        if (this.approximation == approximation || approximation < 0 || approximation > 10) {
            return;
        }
        this.approximation = approximation;
        dateFormat = new SimpleDateFormat(DATE_FORMAT.substring(0, 14 - approximation));
    }
}
