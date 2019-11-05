package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.EventEntity;

import java.util.List;

public interface DealService {
    DealEntity newDeal(DataChart data, EventEntity event, EventType type, Double volume, String caption);
    DealEntity save(DealEntity entity);
    List<DealEntity> findAllByStatus(DealStatusType status);
}
