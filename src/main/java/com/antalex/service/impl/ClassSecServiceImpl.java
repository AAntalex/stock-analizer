package com.antalex.service.impl;

import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.service.ClassSecService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class ClassSecServiceImpl implements ClassSecService {
    @Override
    public Double getVolume(ClassSecEntity sec, BigDecimal amount, BigDecimal price) {
        return  Optional
                .ofNullable(price)
                .map(it ->
                        it.multiply(
                                BigDecimal.valueOf(
                                        Optional.ofNullable(sec)
                                                .map(ClassSecEntity::getLotSize)
                                                .orElse(0)
                                )
                        )
                )
                .filter(it -> it.compareTo(BigDecimal.ZERO) > 0)
                .map(
                        it -> Optional
                                .ofNullable(amount)
                                .map(a -> a.divide(it, 2, RoundingMode.HALF_UP))
                                .orElse(BigDecimal.ZERO)
                )
                .orElse(BigDecimal.ZERO)
                .toBigInteger()
                .doubleValue();
    }
}
