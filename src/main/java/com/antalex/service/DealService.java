package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.EventEntity;

import java.math.BigDecimal;
import java.util.List;

public interface DealService {
    DealEntity newDeal(DataChart data,
                       EventEntity event,
                       EventType type,
                       BigDecimal price,
                       Double volume,
                       String caption,
                       DealEntity main);
    DealEntity save(DealEntity entity);
    DealEntity procLimit(DealEntity deal, DataChart data, Boolean batch);
    void setPrice(DealEntity deal, BigDecimal price);
    List<DealEntity> findAllByStatus(DealStatusType status);
    List<DealEntity> findAllByEvent(EventEntity event);
}
