package com.antalex.persistence.repository;

import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.OrderEntity;
import com.antalex.persistence.entity.EventEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<OrderEntity, Long> {
    @Query(value = "SELECT TRANS_SEQ.NEXTVAL FROM dual", nativeQuery = true)
    Long getTransId();
    List<OrderEntity> findAllByStatus(OrderStatusType status);
    List<OrderEntity> findAllByEventAndStatus(EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllBySecAndEventAndStatusNot(ClassSecEntity sec, EventEntity event, OrderStatusType status);
    List<OrderEntity> findAllByStatusNot(OrderStatusType status);
    List<OrderEntity> findAllBySecAndTypeAndStatusAndUnoLessThan
            (
                    ClassSecEntity sec
                    , EventType type
                    , OrderStatusType status
                    , String uno
            );


}