package com.antalex.model;

import com.antalex.dto.VolumeDto;
import com.antalex.persistence.entity.AllHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class CacheDadaChart {
    private final Map<Date, DataChart> data = new HashMap<>();
    private final Map<String, AllHistory> allHistory = new HashMap<>();
    private List<VolumeDto> lastBidQuotes;
    private List<VolumeDto> lastOfferQuotes;
    private DataChart firstData;
    private DataChart lastData;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private Trend curTrend;

    public void setLastData(DataChart data) {
        this.lastData = data;

        if (this.firstData == null) {
            this.firstData = data;
        }
    }
}
