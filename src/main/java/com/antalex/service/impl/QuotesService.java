package com.antalex.service.impl;

import com.antalex.persistence.entity.Quotes;
import com.antalex.persistence.repository.QuotesRepository;
import com.antalex.service.DataStockService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class QuotesService implements DataStockService<Quotes> {
    private QuotesRepository quotesRepository;

    public List<Quotes> query(String secClass, String sDateBegin, String sDateEnd, String stockClass) {
        if (sDateEnd == null || sDateEnd.isEmpty()) {
            return quotesRepository.findByCodeAndUnoGreaterThanEqualAndClassCodeAndQuotesIsNotNull(secClass, sDateBegin, stockClass);
        }
        try {
            return quotesRepository.findByCodeAndUnoGreaterThanEqualAndUnoLessThanEqualAndClassCodeAndQuotesIsNotNull(secClass, sDateBegin, sDateEnd, stockClass);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

