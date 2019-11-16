package com.antalex.persistence.repository;

import com.antalex.model.enums.DealStatusType;
import com.antalex.persistence.entity.DealEntity;
import com.antalex.persistence.entity.EventEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DealRepository extends CrudRepository<DealEntity, Long> {
    List<DealEntity> findAllByStatus(DealStatusType status);
    List<DealEntity> findAllByEvent(EventEntity event);
}