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
    private final Map<Date, DataChart> data = new HashMap<>();
    private final Map<String, AllTrades> allTrades = new HashMap<>();
    private DataChart firstData;
    private DataChart lastData;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;

    public void setLastData(DataChart data) {
        this.lastData = data;
        if (this.firstData == null) {
            this.firstData = data;
        }
    }
}
