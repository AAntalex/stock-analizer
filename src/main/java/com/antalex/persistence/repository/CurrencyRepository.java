package com.antalex.persistence.repository;

import com.antalex.persistence.entity.CurrencyEntity;
import org.springframework.data.repository.Repository;

public interface CurrencyRepository extends Repository<CurrencyEntity, Long> {
    CurrencyEntity findByCurShort(String curShort);
}