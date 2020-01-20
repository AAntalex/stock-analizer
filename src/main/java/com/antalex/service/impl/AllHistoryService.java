package com.antalex.service.impl;

import com.antalex.persistence.entity.AllHistoryRpt;
import com.antalex.persistence.repository.AllHistoryRepository;
import com.antalex.service.DataStockService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class AllHistoryService implements DataStockService<AllHistoryRpt> {
    private AllHistoryRepository allHistoryRepository;

    public List<AllHistoryRpt> query(String secClass, String sDateBegin, String sDateEnd, String stockClass) {
        if (sDateEnd == null || sDateEnd.isEmpty()) {
            return allHistoryRepository.findByCodeAndUnoGreaterThanEqualAndClassCode(secClass, sDateBegin, stockClass);
        }
        try {
            return allHistoryRepository.findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCode(secClass, sDateBegin, sDateEnd, stockClass);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

