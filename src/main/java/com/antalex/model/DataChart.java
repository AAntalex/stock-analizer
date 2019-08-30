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
    private HashMap<BigDecimal, DataGroup> quotesBid = new HashMap<>();
    private HashMap<BigDecimal, DataGroup> quotesOffer = new HashMap<>();
    private HashMap<String, Indicator> indicators = new HashMap<>();
}
