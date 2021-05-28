package com.antalex.service;

import com.antalex.holders.BatchDataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.*;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    OrderEntity newOrder(DataChart data,
                         EventEntity event,
                         EventType type,
                         BigDecimal price,
                         AccountEntity account,
                         Double volume,
                         String caption,
                         OrderEntity main);
    OrderEntity save(OrderEntity entity, Boolean force);
    void startBatch(Integer batchSize);
    void stopBatch();
    OrderEntity procLimit(OrderEntity order, DataChart data);
    void process(OrderEntity order, DataChart data);
    void processAll(DataChart data);
    List<OrderEntity> findAllByStatus(OrderStatusType status);
    List<OrderEntity> findAllByStatusNot(OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatus(EventEntity event, OrderStatusType status);
    List<OrderHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd);
    Double getBalance(OrderEntity order);
    BigDecimal getTotalSum(OrderEntity order);

    default OrderEntity save(OrderEntity entity) {
        return save(entity, false);
    }
}
