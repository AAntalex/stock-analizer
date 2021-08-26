package com.antalex.model;

import com.antalex.dto.VolumeDto;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class CacheDadaChart {
    private List<DataChart> dataList = new ArrayList<>();
    private List<VolumeDto> lastBidQuotes;
    private List<VolumeDto> lastOfferQuotes;
    private DataChart firstData;
    private DataChart lastData;
    private DataChart curData;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private Trend curTrend;
    private final Map<Integer, TrendSnapShot> trends = new HashMap<>();
    private final Map<String, BigDecimal> traceCalc = new HashMap<>();

    public void setLastData(DataChart data) {
        this.dataList.add(data);
        data.setPrev(this.lastData);
        this.lastData = data;

        if (this.firstData == null) {
            this.firstData = data;
        }
    }

    public void cutData(int newRange) {
        if (this.dataList.size() > newRange) {
            this.dataList = this.dataList.subList(this.dataList.size() - newRange, this.dataList.size());
        }
    }
}
