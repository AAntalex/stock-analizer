package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.AllHistory;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.EventEntity;

import java.math.BigDecimal;

public interface TestService {
    BigDecimal getBuyPrice(DealEntity deal, AllHistory history);
    BigDecimal getSellPrice(DealEntity deal, AllHistory history);
}
