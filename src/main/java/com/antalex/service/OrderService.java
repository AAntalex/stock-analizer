package com.antalex.service;

import com.antalex.holders.BatchDataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.OrderEntity;
import com.antalex.persistence.entity.OrderHistoryRpt;
import com.antalex.persistence.entity.EventEntity;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderEntity newOrder(DataChart data,
                         EventEntity event,
                         EventType type,
                         BigDecimal price,
                         Double volume,
                         String caption,
                         OrderEntity main);
    OrderEntity save(OrderEntity entity);
    void startBatch(Integer batchSize);
    void stopBatch();
    OrderEntity procLimit(OrderEntity order, DataChart data, Boolean batch);
    void process(OrderEntity order);
    void processAll();
    List<OrderEntity> findAllByStatus(OrderStatusType status);
    List<OrderEntity> findAllByStatusNot(OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatus(EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatusNot(EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllBySecAndTypeAndStatusAndUnoLessThan
            (
                    ClassSecEntity sec
                    , EventType type
                    , OrderStatusType status
                    , String uno
            );
    List<OrderHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd);

    default OrderEntity procLimit(OrderEntity order, DataChart data) {
        return procLimit(order, data, BatchDataHolder.getBachSize() > 0);
    }
}
