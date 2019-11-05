package com.antalex.persistence.repository;

import com.antalex.persistence.entity.TriggerEntity;
import org.springframework.data.repository.CrudRepository;

public interface TriggerRepository extends CrudRepository<TriggerEntity, Long> {
}