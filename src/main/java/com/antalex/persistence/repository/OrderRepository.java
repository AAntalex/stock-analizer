package com.antalex.persistence.repository;

import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.OrderEntity;
import com.antalex.persistence.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {
    List<OrderEntity> findAllByStatus(OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatus(EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatusNot(EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllByStatusNot(OrderStatusType status);
    List<OrderEntity> findAllBySecAndTypeAndStatusAndUnoLessThan
            (
                    ClassSecEntity sec
                    , EventType type
                    , OrderStatusType status
                    , String uno
            );
}