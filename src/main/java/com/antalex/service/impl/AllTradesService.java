package com.antalex.service.impl;

import com.antalex.persistence.entity.AllTradesRpt;
import com.antalex.persistence.repository.*;
import com.antalex.service.DataStockService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class AllTradesService implements DataStockService<AllTradesRpt> {
    private AllTradesRepository allTradesRepository;

    public List<AllTradesRpt> query(String secClass, String sDateBegin, String sDateEnd, String stockClass) {
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

