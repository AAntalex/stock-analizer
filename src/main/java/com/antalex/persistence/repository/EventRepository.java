package com.antalex.persistence.repository;

import com.antalex.persistence.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<EventEntity, Long> {
    EventEntity findByCode(String code);
}