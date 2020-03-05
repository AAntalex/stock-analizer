package com.antalex.persistence.repository;

import com.antalex.persistence.entity.TradeClassesEntity;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TradeClassesRepository extends Repository<TradeClassesEntity, String> {
    List<TradeClassesEntity> findAll();
    TradeClassesEntity findOneByCode(String code);
}