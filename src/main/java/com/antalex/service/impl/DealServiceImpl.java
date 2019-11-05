package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.DealRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealRepository dealRepository;
    private final DataChartService dataChartService;

    @Transactional
    @Override
    public DealEntity save(DealEntity entity) {
        return Optional
                .ofNullable(entity)
                .map(dealRepository::save)
                .orElse(null);
    }

    @Override
    public List<DealEntity> findAllByStatus(DealStatusType status) {
        return dealRepository.findAllByStatus(status);
    }

    @Override
    public DealEntity newDeal(DataChart data, EventEntity event, EventType type, Double volume, String caption) {
        DealEntity deal = new DealEntity();
        deal.setType(type);
        deal.setStatus(DealStatusType.OPEN);
        deal.setEvent(event);
        deal.setUno(data.getUno());
        deal.setSecId(data.getSecId());
        deal.setVolume(volume);
        deal.setCaption(caption);
        deal.setIndicators(dataChartService.getIndicatorValues(data));
        deal.setStopLimit(getStopLimit(deal, data.getData().getCandle().getClose()));
        deal.setTakeProfit(getTakeProfit(deal, data.getData().getCandle().getClose()));
        return save(deal);
    }

    private StopLimitEntity getStopLimit(DealEntity deal, BigDecimal price) {
        if (deal.getEvent().getStopLimit() == null) return null;
        StopLimitEntity stopLimit = new StopLimitEntity();
        stopLimit.setVolume(deal.getVolume());
        StopLimitTuneEntity tune = deal.getEvent().getStopLimit();
        if (deal.getType() == EventType.BUY) {
            stopLimit.setStopPrice(
                    price.multiply(
                            BigDecimal.ONE.subtract(
                                    tune.getStopPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
            stopLimit.setPrice(
                    price.multiply(
                            BigDecimal.ONE.subtract(
                                    tune.getPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
        } else {
            stopLimit.setStopPrice(
                    price.multiply(
                            BigDecimal.ONE.add(
                                    tune.getStopPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
            stopLimit.setPrice(
                    price.multiply(
                            BigDecimal.ONE.add(
                                    tune.getPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
        }
        return stopLimit;
    }

    private TakeProfitEntity getTakeProfit(DealEntity deal, BigDecimal price) {
        if (deal.getEvent() == null && deal.getEvent().getTakeProfit() == null) return null;
        TakeProfitEntity takeProfit = new TakeProfitEntity();
        takeProfit.setVolume(deal.getVolume());
        TakeProfitTuneEntity tune = deal.getEvent().getTakeProfit();
        takeProfit.setGap(
                price.multiply(
                        tune.getGap()
                                .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                )
        );
        takeProfit.setSpread(
                price.multiply(
                        tune.getSpread()
                                .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                )
        );
        if (deal.getType() == EventType.BUY) {
            takeProfit.setPrice(
                    price.multiply(
                            BigDecimal.ONE.add(
                                    tune.getPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
        } else {
            takeProfit.setPrice(
                    price.multiply(
                            BigDecimal.ONE.subtract(
                                    tune.getPrice()
                                            .divide(BigDecimal.valueOf(100), DataHolder.PRECISION, RoundingMode.HALF_UP)
                            )
                    )
            );
        }
        return takeProfit;
    }
}

