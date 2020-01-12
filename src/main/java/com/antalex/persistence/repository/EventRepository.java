package com.antalex.persistence.repository;

import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import com.antalex.persistence.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<EventEntity, Long> {
    EventEntity findByCode(String code);
    List<EventEntity> findAllByStatusAndType(StatusType status, EventType type);
}