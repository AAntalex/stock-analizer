package com.antalex.model;

import com.antalex.persistence.entity.AllHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;

@Data
public class DataChart {
    private AllHistory history;
    private Date date;
    private Boolean calcIndicator;
    private Integer idx;
    private DataGroup data;
    private DataGroup dataBid;
    private DataGroup dataOffer;
    private BigDecimal maxPrice;
    private BigDecimal minPrice;
    private DataChart prev;
    private HashMap<BigDecimal, QuoteGroup> quotes = new HashMap<>();
    private HashMap<String, Indicator> indicators = new HashMap<>();
    private BigDecimal bidUp = BigDecimal.ZERO;
    private BigDecimal bidDown = BigDecimal.ZERO;
    private BigDecimal offerUp = BigDecimal.ZERO;
    private BigDecimal offerDown = BigDecimal.ZERO;
}
