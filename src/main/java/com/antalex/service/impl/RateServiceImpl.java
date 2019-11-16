package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.persistence.entity.RateEntity;
import com.antalex.persistence.entity.RateValueEntity;
import com.antalex.service.RateService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RateServiceImpl implements RateService {
    @Override
    public RateValueEntity calc(RateEntity rate, BigDecimal sum) {
        RateValueEntity rateValue = new RateValueEntity();
        rateValue.setRate(rate);
        rateValue.setValue(
                Optional.ofNullable(rate)
                        .map(RateEntity::getRate)
                        .map(it -> it.divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP))
                        .map(it ->
                                it.multiply(
                                        Optional.ofNullable(sum)
                                                .orElse(BigDecimal.ZERO)
                                                .max(BigDecimal.ZERO)
                                )
                        )
                        .map(it -> it.max(Optional.ofNullable(rate.getMinPrice()).orElse(BigDecimal.ZERO)))
                        .map(it -> it.min(Optional.ofNullable(rate.getMaxPrice()).orElse(it)))
                        .orElse(null)
        );
        return rateValue;
    }
}

