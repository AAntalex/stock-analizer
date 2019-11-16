package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.*;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import com.antalex.service.EventService;
import com.antalex.service.TestService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl implements TestService {
    private static final BigDecimal TIME_OUT = BigDecimal.valueOf(2);

    private List<DealEntity> dealList;
    private Map<String, DealEntity> dealMap;
    private final DealService dealService;
    private final DataChartService dataChartService;
    private final EventEntity testEvent;

    TestServiceImpl(DealService dealService,
                    DataChartService dataChartService,
                    EventService eventService)
    {
        this.dealService = dealService;
        this.dataChartService = dataChartService;
        this.testEvent = eventService.findByCode("TEST");
    }

    @Override
    public void init() {
        dealList = dealService.findAllByEvent(this.testEvent);
        dealMap = dealList
                .stream()
                .collect(Collectors.toMap(k -> k.getUno() + k.getType(), v -> v, (a, b) -> a));
    }

    @Override
    public void test(DataChart data) {
        if (dataChartService.getCache().getDataList().size() < 50) {
            return;
        }

        dataChartService.startTrace();


        System.out.println(
                "AAA threadId: " + Thread.currentThread().getId()
                        + " DATE: " + new Date()
                        + " UNO: " + data.getHistory().getUno()
                        + " PRICE: " + data.getHistory().getPrice()
                        + " SIZE: " + dealList.size()
        );


        String checkCode = getCheckCode(data, testEvent);
        getDealList(DealStatusType.PREPARE)
                .forEach(it -> setPrice(it, data.getHistory()));
        dealList.addAll(getDealList(DealStatusType.OPEN)
                .stream()
                .map(it -> dealService.procLimit(it, data, true))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        dealList = dealList
                .stream()
                .filter(it -> it.getStatus() != DealStatusType.CLOSED)
                .collect(Collectors.toList());

        if (!dealMap.containsKey(data.getHistory().getUno() + EventType.BUY)) {
            dealList.add(dealService.newDeal(
                    data,
                    testEvent,
                    EventType.BUY,
                    null,
                    10d,
                    checkCode,
                    null
            ));
        }
        if (!dealMap.containsKey(data.getHistory().getUno() + EventType.SELL)) {
            dealList.add(dealService.newDeal(
                    data,
                    testEvent,
                    EventType.SELL,
                    null,
                    10d,
                    checkCode,
                    null
            ));
        }
        dataChartService.stopTrace();
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

    private String getCheckCode(DataChart data, EventEntity event) {
        return event.getTriggers()
                .stream()
                .sorted(Comparator.comparingInt(EventTriggerEntity::getOrder))
                .map(it -> dataChartService.getBool(data, it.getTrigger().getCondition()) ? "1" : "0")
                .reduce(String::concat)
                .orElse("");
    }
}

