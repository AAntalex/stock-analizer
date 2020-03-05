package com.antalex.service.impl;

import com.antalex.persistence.entity.TradeClassesEntity;
import com.antalex.persistence.repository.TradeClassesRepository;
import com.antalex.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TradeClassesServiceImpl implements TradeClassesService {
    private final TradeClassesRepository tradeClassesRepository;

    @Override
    public List<TradeClassesEntity> findAll() {
        return tradeClassesRepository.findAll();
    }

    @Override
    public TradeClassesEntity findOneByCode(String code) {
        return tradeClassesRepository.findOneByCode(code);
    }
}

