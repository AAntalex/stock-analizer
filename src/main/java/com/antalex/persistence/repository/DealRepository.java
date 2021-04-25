package com.antalex.persistence.repository;

import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.DealEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DealRepository extends CrudRepository<DealEntity, Long> {
    List<DealEntity> findAllBySecAndTypeAndUnoLessThanAndBalanceGreaterThan(
            ClassSecEntity sec
            , EventType type
            , String uno
            , Double balance
    );
}