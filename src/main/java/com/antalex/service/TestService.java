package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.AllHistory;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.EventEntity;

import java.io.IOException;
import java.math.BigDecimal;

public interface TestService {
    void test(DataChart data);
    void saveResult() throws IOException;
    void init();
}
