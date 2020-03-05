package com.antalex.service;

import com.antalex.persistence.entity.TradeClassesEntity;

import java.util.List;

public interface TradeClassesService {
    List<TradeClassesEntity> findAll();
    TradeClassesEntity findOneByCode(String code);
}
