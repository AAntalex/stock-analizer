package com.antalex.model;

import com.antalex.dto.VolumeDto;
import com.antalex.persistence.entity.AllTrades;
import com.antalex.persistence.entity.Quotes;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CacheDadaChart {
    private final Map<Date, DataChart> data = new HashMap<>();
    private final Map<String, AllTrades> allTrades = new HashMap<>();
    private final Map<String, Quotes> quotes = new HashMap<>();
    private List<VolumeDto> lastBidQuotes;
    private List<VolumeDto> lastOfferQuotes;
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
