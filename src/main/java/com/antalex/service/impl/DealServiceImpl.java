package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.RateType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.DealRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import com.antalex.service.TariffPlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {
    private final DealRepository dealRepository;
    private final DataChartService dataChartService;
    private final TariffPlanService tariffPlanService;

    @Transactional
    @Override
    public DealEntity save(DealEntity entity) {
        if (entity == null) {
            return null;
        }
/*

        System.out.println(
                "AAA threadId: " + Thread.currentThread().getId()
                        + " DATE: " + new Date()
                        + " ID: " + entity.getId()
                        + " UNO: " + entity.getUno()
                        + " PRICE: " + entity.getPrice()
                        + " NAX_PRICE: " + entity.getMaxPrice()
                        + " NIN_PRICE: " + entity.getMinPrice()
                        + " STATUS: " + entity.getStatus()
                        + " TYPE: " + entity.getType()
        );

*/
        return dealRepository.save(entity);
    }

    @Override
    public List<DealEntity> findAllByStatus(DealStatusType status) {
        return dealRepository.findAllByStatus(status);
    }

    @Override
    public List<DealEntity> findAllByEvent(EventEntity event) {
        return dealRepository.findAllByEvent(event);
    }

    @Override
    public DealEntity newDeal(DataChart data,
                              EventEntity event,
                              EventType type,
                              BigDecimal price,
                              Double volume,
                              String caption,
                              DealEntity main)
    {
        DealEntity deal = new DealEntity();
        deal.setType(type);
        deal.setStatus(DealStatusType.PREPARE);
        deal.setEvent(event);
        deal.setLimitPrice(price);
        deal.setUno(data.getHistory().getUno());
        deal.setSecId(data.getHistory().getSecId());
        deal.setLotSize(data.getHistory().getLotSize());
        deal.setScale(data.getHistory().getScale());
        deal.setVolume(volume);
        deal.setCaption(caption);
        deal.setMain(main);
        deal.setIndicators(dataChartService.getIndicatorValues(data));
        deal.setTraceValues(dataChartService.getTraceValues());
        deal.setStopLimit(getStopLimit(deal, data.getData().getCandle().getClose()));
        deal.setTakeProfit(getTakeProfit(deal, data.getData().getCandle().getClose()));
        return save(deal);
    }

    @Override
    public DealEntity procLimit(DealEntity deal, DataChart data, Boolean batch) {
        BigDecimal price = data.getData().getCandle().getClose();
        Boolean save = (setMaxPrice(deal, price) || setMinPrice(deal, price)) && !batch;
        DealEntity limitDeal = null;
        if (checkStopLimit(deal, price)) {
            limitDeal = this.newDeal(
                    data,
                    deal.getEvent(),
                    EventType.STOP_LIMIT,
                    deal.getStopLimit().getPrice(),
                    deal.getVolume(),
                    "",
                    deal);
            deal.setStatus(DealStatusType.DONE);
            save = true;
        }
        if (checkTakeProfit(deal, price)) {
            limitDeal = this.newDeal(
                    data,
                    deal.getEvent(),
                    EventType.TAKE_PROFIT,
                    deal.getType() == EventType.BUY
                            ? price.subtract(deal.getTakeProfit().getSpread())
                            : price.add(deal.getTakeProfit().getSpread()),
                    deal.getVolume(),
                    "",
                    deal);
            deal.setStatus(DealStatusType.DONE);
            save = true;
        }
        if (save) {
            this.save(deal);
        }
        return limitDeal;
    }

    @Override
    public void setPrice(DealEntity deal, BigDecimal price) {
        if (price != null) {
            deal.setPrice(price);
            BigDecimal sum = getSum(deal);
            deal.getRates().addAll(
                    tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.CASH_FLOW,
                            sum
                    )
            );
            if (deal.getType() == EventType.BUY || deal.getType() == EventType.SELL) {
                deal.setStatus(DealStatusType.OPEN);
            } else {
                deal.setStatus(DealStatusType.CLOSED);
                if (deal.getMain() != null) {
                    BigDecimal mainSum = getSum(deal.getMain());
                    deal.getMain().setStatus(DealStatusType.CLOSED);
                    BigDecimal income = (
                            deal.getMain().getType() == EventType.BUY
                                    ? sum.subtract(mainSum)
                                    : mainSum.subtract(sum)
                    )
                            .subtract(
                                    Stream.concat(
                                            deal.getRates().stream(),
                                            deal.getMain().getRates().stream()
                                    )
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            );

                    List<RateValueEntity> incomeRates = tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.INCOME,
                            income
                    );
                    deal.getMain().getRates().addAll(incomeRates);
                    deal.getMain().setResult(income
                            .subtract(
                                    incomeRates.stream()
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            )
                    );
                }
            }
            this.save(deal);
        }
    }

    private BigDecimal getSum(DealEntity deal) {
        return Optional.ofNullable(deal)
                .map(DealEntity::getPrice)
                .map(it ->
                        it
                                .multiply(new BigDecimal(deal.getVolume()))
                                .multiply(new BigDecimal(deal.getLotSize()))
                )
                .orElse(null);
    }

    private Boolean setMaxPrice(DealEntity deal, BigDecimal price) {
        if (
                Optional
                        .ofNullable(deal.getMaxPrice())
                        .map(it -> it.compareTo(price) < 0)
                        .orElse(true))
        {
            deal.setMaxPrice(price);
            return true;
        }
        return false;
    }

    private Boolean setMinPrice(DealEntity deal, BigDecimal price) {
        if (
                Optional
                        .ofNullable(deal.getMinPrice())
                        .map(it -> it.compareTo(price) > 0)
                        .orElse(true))
        {
            deal.setMinPrice(price);
            return true;
        }
        return false;
    }

    private Boolean checkStopLimit(DealEntity deal, BigDecimal price) {
        return price != null && deal.getStopLimit() != null && deal.getPrice() != null &&
                (deal.getType() == EventType.BUY && price.compareTo(deal.getStopLimit().getStopPrice()) <= 0 ||
                deal.getType() == EventType.SELL && price.compareTo(deal.getStopLimit().getStopPrice()) >= 0);
    }

    private Boolean checkTakeProfit(DealEntity deal, BigDecimal price) {
        if (price == null || deal.getTakeProfit() == null || deal.getPrice() == null) {
            return false;
        }
        if (!deal.getTakeProfit().getActive()) {
            deal.getTakeProfit().setActive(
                    deal.getType() == EventType.BUY &&
                            price.compareTo(deal.getTakeProfit().getPrice()) >= 0 ||
                            deal.getType() == EventType.SELL &&
                                    price.compareTo(deal.getTakeProfit().getPrice()) <= 0);
        }
        return deal.getTakeProfit().getActive() &&
                (
                        deal.getType() == EventType.BUY &&
                                deal.getMaxPrice() != null &&
                                price.compareTo(deal.getMaxPrice().subtract(deal.getTakeProfit().getGap())) <= 0 ||
                                deal.getType() == EventType.SELL &&
                                        deal.getMinPrice() != null &&
                                        price.compareTo(deal.getMinPrice().add(deal.getTakeProfit().getGap())) >= 0);
    }

    private StopLimitEntity getStopLimit(DealEntity deal, BigDecimal price) {
        if (deal.getEvent() == null &&
                deal.getEvent().getStopLimit() == null ||
                deal.getType() != EventType.BUY && deal.getType() != EventType.SELL)
        {
            return null;
        }
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
        if (deal.getEvent() == null &&
                deal.getEvent().getTakeProfit() == null ||
                deal.getType() != EventType.BUY && deal.getType() != EventType.SELL)
        {
            return null;
        }
        TakeProfitEntity takeProfit = new TakeProfitEntity();
        takeProfit.setActive(false);
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

