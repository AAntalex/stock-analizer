package com.antalex.persistence.repository;

import com.antalex.persistence.entity.IndicatorEntity;
import org.springframework.data.repository.CrudRepository;

public interface IndicatorRepository extends CrudRepository<IndicatorEntity, Long> {
}