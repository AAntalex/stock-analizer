package com.antalex.service.impl;

import com.antalex.persistence.entity.AllTrades;
import com.antalex.persistence.repository.*;
import com.antalex.service.DataStockService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AllTradesService implements DataStockService<AllTrades> {
    private AllTradesRepository allTradesRepository;

    public List<AllTrades> query(String secClass, String sDateBegin, String sDateEnd, String stockClass) {
        if (sDateEnd == null || sDateEnd.isEmpty()) {
            return allTradesRepository.findByCodeAndUnoGreaterThanEqualAndClassCode(secClass, sDateBegin, stockClass);
        }
        try {
            return allTradesRepository.findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(secClass, sDateBegin, sDateEnd, stockClass);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

