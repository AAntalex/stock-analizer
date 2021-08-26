package com.antalex.service;

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
    void startCache(Integer batchSize);
    void stopCache();
    OrderEntity procLimit(OrderEntity order, DataChart data);
    void process(OrderEntity order, DataChart data);
    void processAll(DataChart data);
    List<OrderEntity> findAllBySecAndStatus(ClassSecEntity sec, OrderStatusType status);
    List<OrderEntity> findAllBySecAndStatusNot(ClassSecEntity sec, OrderStatusType status);
    List<OrderEntity> findAllBySecAndEventAndStatus(ClassSecEntity sec, EventEntity event, OrderStatusType status);
    List<OrderHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd);
    Double getBalance(OrderEntity order);
    BigDecimal getTotalSum(OrderEntity order);

    default OrderEntity save(OrderEntity entity) {
        return save(entity, false);
    }
}
