package com.antalex.persistence.repository;

import com.antalex.persistence.entity.EventTriggerEntity;
import org.springframework.data.repository.CrudRepository;

public interface EventTriggerRepository extends CrudRepository<EventTriggerEntity, Long> {
}