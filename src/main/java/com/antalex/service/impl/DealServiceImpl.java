package com.antalex.service.impl;

import com.antalex.holders.BatchDataHolder;
import com.antalex.holders.DataChartHolder;
import com.antalex.holders.DataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.RateType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.DealHistoryRepository;
import com.antalex.persistence.repository.DealRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import com.antalex.service.TariffPlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class DealServiceImpl implements DealService {
    private static final Map<String, DealEntity> CACHE = new LinkedHashMap<>();

    private final DealRepository dealRepository;
    private final DataChartService dataChartService;
    private final TariffPlanService tariffPlanService;
    private final DealHistoryRepository dealHistoryRepository;

    @Transactional
    @Override
    public DealEntity save(DealEntity entity) {
        if (entity == null) {
            return null;
        }
        if (DataChartHolder.isCalcCorr()) {
            return entity;
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
        if (BatchDataHolder.getBachSize() > 0) {
            CACHE.put(getHashCode(entity), entity);
            if (CACHE.size() >= BatchDataHolder.getBachSize()) {
                procBatch();
            }
            return entity;
        } else {
            return dealRepository.save(entity);
        }
    }

    @Override
    public void startBatch(Integer batchSize) {
        BatchDataHolder.setBatchSize(batchSize);
        CACHE.clear();
    }

    @Override
    public void stopBatch() {
        BatchDataHolder.setBatchSize(0);
        procBatch();
    }

    @Override
    public List<DealHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd) {
        if (sDateEnd == null || sDateEnd.isEmpty()) {
            return dealHistoryRepository.findByCodeAndClassCodeAndUnoGreaterThanEqualAndUnoLessThanEqual(
                    code,
                    classCode,
                    sDateBegin,
                    sDateEnd
            );
        }
        return dealHistoryRepository.findByCodeAndClassCodeAndUnoGreaterThanEqual(
                code,
                classCode,
                sDateBegin
        );
    }

    @Transactional
    protected void procBatch() {
        CACHE.values().forEach(dealRepository::save);
        CACHE.clear();
    }

    @Override
    public List<DealEntity> findAllByStatus(DealStatusType status) {
        return dealRepository.findAllByStatus(status);
    }

    @Override
    public List<DealEntity> findAllByEventAndStatus(EventEntity event, DealStatusType status) {
        return dealRepository.findAllByEventAndStatus(event, status);
    }

    @Override
    public List<DealEntity> findAllByEventAndStatusNot(EventEntity event, DealStatusType status) {
        return dealRepository.findAllByEventAndStatusNot(event, status);
    }

    @Override
    public List<DealEntity> getProcessedDeals(EventEntity event, DealStatusType status, EventType type) {
        return dealRepository.findAllByEventAndStatusAndTypeAndResultIsNotNullOrderByUno(event, status, type);
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
        addHistory(deal, price, deal.getUno());
        return this.save(deal);
    }

    @Override
    public DealEntity procLimit(DealEntity deal, DataChart data, Boolean batch) {
        BigDecimal price = data.getData().getCandle().getClose();
        Boolean save = (setMaxPrice(deal, price) || setMinPrice(deal, price)) && !batch;
        DealEntity limitDeal = null;
        if (checkStopLimit(deal, data)) {
            limitDeal = this.newDeal(
                    data,
                    deal.getEvent().getStopLimit().getEvent(),
                    EventType.STOP_LIMIT,
                    deal.getStopLimit().getPrice(),
                    deal.getVolume(),
                    "",
                    deal);
            deal.setStatus(DealStatusType.DONE);
            addHistory(deal, price, data.getHistory().getUno());
            save = true;
        }
        if (checkTakeProfit(deal, data)) {
            limitDeal = this.newDeal(
                    data,
                    deal.getEvent().getTakeProfit().getEvent(),
                    EventType.TAKE_PROFIT,
                    deal.getType() == EventType.BUY
                            ? price.subtract(deal.getTakeProfit().getSpread())
                            : price.add(deal.getTakeProfit().getSpread()),
                    deal.getVolume(),
                    "",
                    deal);
            deal.setStatus(DealStatusType.DONE);
            addHistory(deal, price, data.getHistory().getUno());
            save = true;
        }
        if (save) {
            this.save(deal);
        }
        return limitDeal;
    }

    @Override
    public void setPrice(DealEntity deal, BigDecimal price, String uno) {
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
                DealEntity mainDeal = deal.getMain();
                if (mainDeal != null) {
                    BigDecimal mainSum = getSum(mainDeal);
                    mainDeal.setStatus(DealStatusType.CLOSED);
                    BigDecimal income = (
                            mainDeal.getType() == EventType.BUY
                                    ? sum.subtract(mainSum)
                                    : mainSum.subtract(sum)
                    )
                            .subtract(
                                    Stream.concat(
                                            deal.getRates().stream(),
                                            mainDeal.getRates().stream()
                                    )
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            );

                    List<RateValueEntity> incomeRates = tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.INCOME,
                            income
                    );
                    mainDeal.getRates().addAll(incomeRates);
                    mainDeal.setResult(income
                            .subtract(
                                    incomeRates.stream()
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            )
                    );
                }
            }
            addHistory(deal, price, uno);
            Optional.ofNullable(deal.getMain())
                    .ifPresent(it -> addHistory(it, price, uno));
            this.save(deal);
        }
    }

    private void addHistory(DealEntity deal, BigDecimal price, String uno) {
        if (Objects.isNull(deal) || Objects.nonNull(deal.getMain())) {
            return;
        }
        DealHistoryEntity dealHistoryEntity = new DealHistoryEntity();
        dealHistoryEntity.setPrice(price);
        dealHistoryEntity.setDate(new Date());
        dealHistoryEntity.setStatus(deal.getStatus());
        dealHistoryEntity.setType(getTypeByStatus(deal));
        dealHistoryEntity.setUno(uno);
        deal.getHistory().add(dealHistoryEntity);
    }

    private EventType getTypeByStatus(DealEntity deal) {
        if (deal.getStatus() == DealStatusType.PREPARE || deal.getStatus() == DealStatusType.OPEN) {
            return deal.getType();
        } else {
            return deal.getType() == EventType.BUY ? EventType.SELL : EventType.BUY;
        }
    }

    private String getHashCode(DealEntity entity) {
        return entity.getUno() +
                entity.getType().name() +
                Optional.ofNullable(entity.getMain()).map(DealEntity::getUno).orElse("");
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

    private Boolean checkStopLimit(DealEntity deal, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        return price != null && deal.getStopLimit() != null && deal.getPrice() != null &&
                (
                        deal.getType() == EventType.BUY && price.compareTo(deal.getStopLimit().getStopPrice()) <= 0 ||
                        deal.getType() == EventType.SELL && price.compareTo(deal.getStopLimit().getStopPrice()) >= 0
                ) || dataChartService.checkEvent(data, deal.getEvent().getStopLimit().getEvent());
    }

    private Boolean checkTakeProfit(DealEntity deal, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        if (price == null || deal.getTakeProfit() == null || deal.getPrice() == null) {
            return false;
        }
        if (!deal.getTakeProfit().getActive()) {
            deal.getTakeProfit().setActive(
                    deal.getType() == EventType.BUY &&
                            price.compareTo(deal.getTakeProfit().getPrice()) >= 0 ||
                            deal.getType() == EventType.SELL &&
                                    price.compareTo(deal.getTakeProfit().getPrice()) <= 0 ||
                            dataChartService.checkEvent(data, deal.getEvent().getTakeProfit().getEvent()));
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
        if (deal.getEvent() == null ||
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
        if (deal.getEvent() == null ||
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

