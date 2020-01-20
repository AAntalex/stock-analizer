package com.antalex.service;

import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.DealHistoryRpt;
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
    void startBatch(Integer batchSize);
    void stopBatch();
    DealEntity procLimit(DealEntity deal, DataChart data, Boolean batch);
    void setPrice(DealEntity deal, BigDecimal price, String uno);
    List<DealEntity> findAllByStatus(DealStatusType status);
    List<DealEntity> findAllByEventAndStatus(EventEntity event, DealStatusType status);
    List<DealEntity> findAllByEventAndStatusNot(EventEntity event, DealStatusType status);
    List<DealEntity> getProcessedDeals(EventEntity event, DealStatusType status, EventType type);
    List<DealHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd);

    default DealEntity procLimit(DealEntity deal, DataChart data) {
        return procLimit(deal, data, false);
    }
}
