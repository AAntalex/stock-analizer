package com.antalex.service.impl;

import com.antalex.model.enums.RateType;
import com.antalex.persistence.entity.RateValueEntity;
import com.antalex.persistence.entity.TariffPlanEntity;
import com.antalex.persistence.repository.TariffPlanRepository;
import com.antalex.service.RateService;
import com.antalex.service.TariffPlanService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TariffPlanServiceImpl implements TariffPlanService {
    private static final String MAIN = "MAIN";
    private final TariffPlanRepository tariffPlanRepository;
    private final RateService rateService;
    private final TariffPlanEntity main;

    TariffPlanServiceImpl(TariffPlanRepository tariffPlanRepository,
                          RateService rateService) {
        this.tariffPlanRepository = tariffPlanRepository;
        this.rateService = rateService;
        this.main = findOneByCode(MAIN);
    }

    @Override
    public TariffPlanEntity findOneByCode(String code) {
        return tariffPlanRepository.findOneByCode(code);
    }

    @Override
    public TariffPlanEntity getMain() {
        return this.main;
    }

    @Override
    public List<RateValueEntity> applyForType(TariffPlanEntity tariff, RateType type, BigDecimal sum) {
        return tariff.getRates()
                .stream()
                .filter(it -> it.getType() == type)
                .map(it -> rateService.calc(it, sum))
                .filter(it ->
                        Optional.ofNullable(it.getValue())
                                .map(value -> value.compareTo(BigDecimal.ZERO) > 0)
                                .orElse(false)
                )
                .collect(Collectors.toList());
    }
}

