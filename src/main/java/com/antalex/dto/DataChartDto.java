package com.antalex.dto;

import com.antalex.model.Indicator;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DataChartDto {
    private long date;
    private DataGroupDto data;
    private DataGroupDto dataBid;
    private DataGroupDto dataOffer;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private BigDecimal maxPercent;
    private BigDecimal minPercent;
    private List<Indicator> indicators;
    private List<QuoteGroupDto> quotes;
    private BigDecimal bidUp;
    private BigDecimal bidDown;
    private BigDecimal offerUp;
    private BigDecimal offerDown;
}
