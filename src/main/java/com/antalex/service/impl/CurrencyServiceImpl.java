package com.antalex.service.impl;

import com.antalex.persistence.entity.CurrencyEntity;
import com.antalex.persistence.repository.CurrencyRepository;
import com.antalex.service.CurrencyService;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyEntity defaultCurrency;

    CurrencyServiceImpl(CurrencyRepository currencyRepository) {
        this.defaultCurrency = currencyRepository.findByCurShort("RUB");
    }

    @Override
    public CurrencyEntity defaultCurrency() {
        return this.defaultCurrency;
    }
}
