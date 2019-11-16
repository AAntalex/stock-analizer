package com.antalex.service;

import com.antalex.persistence.entity.RateEntity;
import com.antalex.persistence.entity.RateValueEntity;

import java.math.BigDecimal;

public interface RateService {
    RateValueEntity calc(RateEntity rate, BigDecimal sum);
}
