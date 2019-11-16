package com.antalex.service;

import com.antalex.model.enums.RateType;
import com.antalex.persistence.entity.RateValueEntity;
import com.antalex.persistence.entity.TariffPlanEntity;

import java.math.BigDecimal;
import java.util.List;

public interface TariffPlanService {
    TariffPlanEntity findOneByCode(String code);
    TariffPlanEntity getMain();
    List<RateValueEntity> applyForType(TariffPlanEntity tariff, RateType type, BigDecimal sum);
}
