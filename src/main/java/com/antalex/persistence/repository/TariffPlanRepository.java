package com.antalex.persistence.repository;

import com.antalex.persistence.entity.TariffPlanEntity;
import org.springframework.data.repository.CrudRepository;

public interface TariffPlanRepository extends CrudRepository<TariffPlanEntity, Long> {
    TariffPlanEntity findOneByCode(String code);
}