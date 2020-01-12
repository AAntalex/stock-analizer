package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.persistence.entity.EventEntity;

public interface EventService {
    void apply(DataChart data, EventEntity event);
    void applyAll(DataChart data);
    EventEntity findByCode(String code);
}
