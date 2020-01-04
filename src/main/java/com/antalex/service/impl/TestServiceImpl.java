package com.antalex.service.impl;

import com.antalex.model.AnaliseResultRow;
import com.antalex.model.DataChart;
import com.antalex.model.AnaliseResultTable;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.*;
import com.antalex.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TestServiceImpl implements TestService {
    private static final BigDecimal TIME_OUT = BigDecimal.valueOf(1);

    private AnaliseResultTable traceResultSell;
    private AnaliseResultTable deltaResultSell;
    private AnaliseResultTable boolResultSell;
    private AnaliseResultTable traceResultBuy;
    private AnaliseResultTable deltaResultBuy;
    private AnaliseResultTable boolResultBuy;

    private List<DealEntity> dealList;
    private final DealService dealService;
    private final DataChartService dataChartService;
    private final EventEntity testEvent;
    private final AnaliseService analiseService;

    private int k = 0;
    Calendar calendar;

    TestServiceImpl(DealService dealService,
                    DataChartService dataChartService,
                    EventService eventService,
                    AnaliseService analiseService)
    {
        this.dealService = dealService;
        this.dataChartService = dataChartService;
        this.analiseService = analiseService;
        this.testEvent = eventService.findByCode("TEST");
    }

    @Override
    public void init() {
        traceResultSell = new AnaliseResultTable();
        deltaResultSell = new AnaliseResultTable();
        boolResultSell = new AnaliseResultTable();
        traceResultBuy = new AnaliseResultTable();
        deltaResultBuy = new AnaliseResultTable();
        boolResultBuy = new AnaliseResultTable();

        dealList = new ArrayList<>();
    }

    @Override
    public void saveResult() throws IOException {
        analiseService.save(traceResultSell, "Result/RESULT_SELL_TRACE.csv");
        analiseService.save(boolResultSell, "Result/RESULT_SELL_BOOL.csv");
        analiseService.save(deltaResultSell, "Result/RESULT_SELL_DELTA.csv");

        analiseService.save(traceResultBuy, "Result/RESULT_BUY_TRACE.csv");
        analiseService.save(boolResultBuy, "Result/RESULT_BUY_BOOL.csv");
        analiseService.save(deltaResultBuy, "Result/RESULT_BUY_DELTA.csv");

        Integer steps = 20;

        analiseService.saveCorrelations(traceResultSell, "Result/CORR_SELL_TRACE.csv", steps);
        analiseService.saveCorrelations(boolResultSell, "Result/CORR_SELL_BOOL.csv", 1);
        analiseService.saveCorrelations(deltaResultSell, "Result/CORR_SELL_DELTA.csv", steps);

        analiseService.saveCorrelations(traceResultBuy, "Result/CORR_BUY_TRACE.csv", steps);
        analiseService.saveCorrelations(boolResultBuy, "Result/CORR_BUY_BOOL.csv", 1);
        analiseService.saveCorrelations(deltaResultBuy, "Result/CORR_BUY_DELTA.csv", steps);

        log.info("Result Saved k = " + k + " size " + dealList.size() + " all " + dataChartService.getCache().getAllHistory().size());
    }

    @Override
    public void test(DataChart data) {
        Integer count = dataChartService.getCache().getDataList().size();
        if (count < 120) {
            return;
        }

        if (this.calendar == null) {
            this.calendar = Calendar.getInstance();
            this.calendar.setTime(data.getDate());
            this.calendar.add(Calendar.HOUR, 1);
        }
        if (data.getDate().compareTo(this.calendar.getTime()) >= 0) {
            log.info(String.format("Process %d records. Time (%s)", count, data.getDate()));
            this.calendar.setTime(data.getDate());
            this.calendar.add(Calendar.HOUR, 1);
        }

        dataChartService.startTrace();

        getDealList(DealStatusType.PREPARE)
                .forEach(it -> setPrice(it, data.getHistory()));
        dealList.addAll(getDealList(DealStatusType.OPEN)
                .stream()
                .map(it -> dealService.procLimit(it, data, true))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        dealList
                .stream()
                .filter(it ->
                        it.getStatus() == DealStatusType.CLOSED &&
                                (
                                        it.getType() == EventType.BUY ||
                                                it.getType() == EventType.SELL
                                )
                )
                .sorted(Comparator.comparing(DealEntity::getUno))
                .forEach(it -> {
                    if (it.getType() == EventType.SELL) {
                        setBoolResult(it, boolResultSell);
                        setDeltaResult(it, deltaResultSell);
                        setTraceResult(it, traceResultSell);
                    } else {
                        setBoolResult(it, boolResultBuy);
                        setDeltaResult(it, deltaResultBuy);
                        setTraceResult(it, traceResultBuy);
                    }
                });

        dealList = dealList
                .stream()
                .filter(it -> it.getStatus() != DealStatusType.CLOSED)
                .collect(Collectors.toList());

        List<BigDecimal> boolTriggerValues = getCheckValues(data, testEvent);
        List<BigDecimal> deltaTriggerValues = getDeltaValues(data, testEvent);

        DealEntity deal = dealService.newDeal(
                data,
                testEvent,
                EventType.BUY,
                null,
                10d,
                "",
                null
        );
        deal.setBoolTriggerValues(boolTriggerValues);
        deal.setDeltaTriggerValues(deltaTriggerValues);
        dealList.add(deal);

        deal = dealService.newDeal(
                data,
                testEvent,
                EventType.SELL,
                null,
                10d,
                "",
                null
        );
        deal.setBoolTriggerValues(boolTriggerValues);
        deal.setDeltaTriggerValues(deltaTriggerValues);
        dealList.add(deal);

        k = k + 2;

        dataChartService.stopTrace();
    }

    private void setBoolResult(DealEntity deal, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(getCheckHeaders(testEvent));
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        deal.getUno(),
                        deal.getResult().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE : BigDecimal.ZERO,
                        deal.getBoolTriggerValues()
                )
        );
    }

    private void setDeltaResult(DealEntity deal, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(getDeltaHeaders(testEvent));
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        deal.getUno(),
                        deal.getResult(),
                        deal.getDeltaTriggerValues()
                )
        );
    }

    private void setTraceResult(DealEntity deal, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(
                    deal.getTraceValues()
                            .stream()
                            .map(TraceValueEntity::getCode)
                            .collect(Collectors.toList())
            );
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        deal.getUno(),
                        deal.getResult(),
                        deal.getTraceValues()
                                .stream()
                                .map(TraceValueEntity::getValue)
                                .collect(Collectors.toList())
                )
        );
    }

    private List<DealEntity> getDealList(DealStatusType dealStatusType) {
        return dealList
                .stream()
                .filter(it -> it.getStatus() == dealStatusType)
                .collect(Collectors.toList());
    }

    private void setPrice(DealEntity deal, AllHistory history) {
        if (deal.getType() == EventType.SELL ||
                (deal.getType() == EventType.TAKE_PROFIT ||
                        deal.getType() == EventType.STOP_LIMIT) &&
                        deal.getMain().getType() == EventType.BUY)
        {
            dealService.setPrice(deal, getSellPrice(deal, history));
        } else {
            dealService.setPrice(deal, getBuyPrice(deal, history));
        }
    }

    private BigDecimal getBuyPrice(DealEntity deal, AllHistory history) {
        return Optional.ofNullable(history)
                .filter(AllHistory::getBidFlag)
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(deal.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistory::getPrice)
                .filter(
                        it -> Optional.ofNullable(deal.getLimitPrice())
                                .map(limit -> limit.compareTo(it) >= 0)
                                .orElse(true)
                )
                .orElse(null);
    }

    private BigDecimal getSellPrice(DealEntity deal, AllHistory history) {
        return Optional.ofNullable(history)
                .filter(it -> !it.getBidFlag())
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(deal.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistory::getPrice)
                .filter(
                        it -> Optional.ofNullable(deal.getLimitPrice())
                                .map(limit -> limit.compareTo(it) <= 0)
                                .orElse(true)
                )
                .orElse(null);
    }

    private List<BigDecimal> getCheckValues(DataChart data, EventEntity event) {
        return event.getTriggers()
                .stream()
                .sorted(Comparator.comparingInt(EventTriggerEntity::getOrder))
                .map(it -> dataChartService.getExpValue(data, it.getTrigger().getCondition()))
                .collect(Collectors.toList());
    }

    private List<String> getCheckHeaders(EventEntity event) {
        return event.getTriggers()
                .stream()
                .sorted(Comparator.comparingInt(EventTriggerEntity::getOrder))
                .map(EventTriggerEntity::getTrigger)
                .map(TriggerEntity::getCondition)
                .collect(Collectors.toList());
    }

    private List<BigDecimal> getDeltaValues(DataChart data, EventEntity event) {
        return event.getTriggers()
                .stream()
                .sorted(Comparator.comparingInt(EventTriggerEntity::getOrder))
                .map(it -> getDeltaValue(data, it.getTrigger().getCondition()))
                .collect(Collectors.toList());
    }

    private List<String> getDeltaHeaders(EventEntity event) {
        return event.getTriggers()
                .stream()
                .sorted(Comparator.comparingInt(EventTriggerEntity::getOrder))
                .map(EventTriggerEntity::getTrigger)
                .map(TriggerEntity::getCondition)
                .map(it -> dataChartService.normalizeExpression(it).replaceAll("[<>=]+", "-"))
                .collect(Collectors.toList());
    }

    private BigDecimal getDeltaValue(DataChart data, String boolExpression) {
        return dataChartService.getExpValue(
                data,
                dataChartService.normalizeExpression(boolExpression)
                        .replaceAll("[<>=]+", "-")
        );
    }
}

