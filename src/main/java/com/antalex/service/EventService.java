package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.persistence.entity.EventEntity;

public interface EventService {
    void apply(DataChart data, EventEntity event);
    String getCheckCode(DataChart data, EventEntity event);
    EventEntity findByCode(String code);
}
