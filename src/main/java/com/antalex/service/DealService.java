package com.antalex.service;

import com.antalex.persistence.entity.DealEntity;

import java.math.BigDecimal;

public interface DealService {
    Boolean quitDeals(DealEntity deal, DealEntity dealForQuit);
}
