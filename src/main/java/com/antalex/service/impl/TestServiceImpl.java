package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.persistence.entity.*;
import com.antalex.service.TestService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TestServiceImpl implements TestService {
    private static final BigDecimal TIME_OUT = BigDecimal.valueOf(2);

    @Override
    public BigDecimal getBuyPrice(DealEntity deal, AllHistory history) {
        return Optional.ofNullable(history)
                .filter(AllHistory::getBidFlag)
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(deal.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistory::getPrice)
                .orElse(null);
    }

    @Override
    public BigDecimal getSellPrice(DealEntity deal, AllHistory history) {
        return Optional.ofNullable(history)
                .filter(it -> !it.getBidFlag())
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(deal.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistory::getPrice)
                .orElse(null);
    }
}

