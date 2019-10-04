package com.antalex.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
public class DataChart {
    private Date date;
    private DataGroup data;
    private DataGroup dataBid;
    private DataGroup dataOffer;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private DataChart prev;
    private HashMap<BigDecimal, QuoteGroup> quotes = new HashMap<>();
    private HashMap<String, Indicator> indicators = new HashMap<>();
    private BigDecimal bidUp;
    private BigDecimal bidDown;
    private BigDecimal offerUp;
    private BigDecimal offerDown;
}
