package com.antalex.service.impl;

import com.antalex.model.AnaliseResultRow;
import com.antalex.model.DataChart;
import com.antalex.model.AnaliseResultTable;
import com.antalex.model.enums.OrderStatusType;
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

    private List<OrderEntity> orderList;
    private final OrderService orderService;
    private final DataChartService dataChartService;
    private final EventEntity testEvent;
    private final AnaliseService analiseService;
    private final EventService eventService;

    private int k = 0;
    Calendar calendar;
    BigDecimal totalSum = BigDecimal.valueOf(100000);

    TestServiceImpl(OrderService orderService,
                    DataChartService dataChartService,
                    EventService eventService,
                    AnaliseService analiseService)
    {
        this.orderService = orderService;
        this.dataChartService = dataChartService;
        this.analiseService = analiseService;
        this.eventService = eventService;
        this.testEvent = this.eventService.findByCode("TEST");
    }

    @Override
    public void init() {
        traceResultSell = new AnaliseResultTable();
        deltaResultSell = new AnaliseResultTable();
        boolResultSell = new AnaliseResultTable();
        traceResultBuy = new AnaliseResultTable();
        deltaResultBuy = new AnaliseResultTable();
        boolResultBuy = new AnaliseResultTable();

        orderList = new ArrayList<>();
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

        log.info("Result Saved k = " + k + " size " + orderList.size() + " all " + dataChartService.getCache().getAllHistory().size());
    }

    @Override
    public void test(DataChart data) {
        Integer count = dataChartService.getCache().getDataList().size();
        if (count <= 480) {
            return;
        }

        dataChartService.startTrace();

        eventService.applyAll(data);

        orderService.findAllByStatus(OrderStatusType.PREPARE)
                .forEach(it -> setPrice(it, data.getHistory()));

        dataChartService.stopTrace();

        printLog(data);
    }

    @Override
    public void calcCorr(DataChart data) {
        Integer count = dataChartService.getCache().getDataList().size();
        if (count <= 120) {
            return;
        }

        printLog(data);

        dataChartService.startTrace();

        getOrderList(OrderStatusType.PREPARE)
                .forEach(it -> setPrice(it, data.getHistory()));

        orderList.addAll(getOrderList(OrderStatusType.OPEN)
                .stream()
                .map(it -> orderService.procLimit(it, data, true))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        orderList
                .stream()
                .filter(it ->
                        it.getStatus() == OrderStatusType.CLOSED &&
                                (
                                        it.getType() == EventType.BUY ||
                                                it.getType() == EventType.SELL
                                )
                )
                .sorted(Comparator.comparing(OrderEntity::getUno))
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

        orderList = orderList
                .stream()
                .filter(it -> it.getStatus() != OrderStatusType.CLOSED)
                .collect(Collectors.toList());

        List<BigDecimal> boolTriggerValues = getCheckValues(data, testEvent);
        List<BigDecimal> deltaTriggerValues = getDeltaValues(data, testEvent);

        OrderEntity order = orderService.newOrder(
                data,
                testEvent,
                EventType.BUY,
                null,
                10d,
                "",
                null
        );
        order.setBoolTriggerValues(boolTriggerValues);
        order.setDeltaTriggerValues(deltaTriggerValues);
        orderList.add(order);

        order = orderService.newOrder(
                data,
                testEvent,
                EventType.SELL,
                null,
                10d,
                "",
                null
        );
        order.setBoolTriggerValues(boolTriggerValues);
        order.setDeltaTriggerValues(deltaTriggerValues);
        orderList.add(order);

        k = k + 2;

        dataChartService.stopTrace();
    }

    private void printLog(DataChart data) {
        if (this.calendar == null) {
            this.calendar = Calendar.getInstance();
            this.calendar.setTime(data.getDate());
            this.calendar.add(Calendar.HOUR, 1);
        }
        if (data.getDate().compareTo(this.calendar.getTime()) >= 0) {
            log.info(String.format(
                    "Process %d records. Time (%s)",
                    dataChartService.getCache().getDataList().size(),
                    data.getDate())
            );
            this.calendar.setTime(data.getDate());
            this.calendar.add(Calendar.HOUR, 1);
        }
    }

    private void setBoolResult(OrderEntity order, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(getCheckHeaders(testEvent));
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        order.getUno(),
                        order.getResult().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE : BigDecimal.ZERO,
                        order.getBoolTriggerValues()
                )
        );
    }

    private void setDeltaResult(OrderEntity order, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(getDeltaHeaders(testEvent));
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        order.getUno(),
                        order.getResult(),
                        order.getDeltaTriggerValues()
                )
        );
    }

    private void setTraceResult(OrderEntity order, AnaliseResultTable analiseResultTable) {
        if (analiseResultTable.getHeaders() == null) {
            analiseResultTable.setHeaders(
                    order.getTraceValues()
                            .stream()
                            .map(TraceValueEntity::getCode)
                            .collect(Collectors.toList())
            );
        }
        analiseResultTable.getData().add(
                new AnaliseResultRow(
                        order.getUno(),
                        order.getResult(),
                        order.getTraceValues()
                                .stream()
                                .map(TraceValueEntity::getValue)
                                .collect(Collectors.toList())
                )
        );
    }

    private List<OrderEntity> getOrderList(OrderStatusType orderStatusType) {
        return orderList
                .stream()
                .filter(it -> it.getStatus() == orderStatusType)
                .collect(Collectors.toList());
    }

    private void setPrice(OrderEntity order, AllHistoryRpt history) {
        if (order.getType() == EventType.SELL ||
                (order.getType() == EventType.TAKE_PROFIT ||
                        order.getType() == EventType.STOP_LIMIT) &&
                        order.getMain().getType() == EventType.BUY)
        {
            orderService.setPrice(order, getSellPrice(order, history), history.getUno());
        } else {
            orderService.setPrice(order, getBuyPrice(order, history), history.getUno());
        }
    }

    private BigDecimal getBuyPrice(OrderEntity order, AllHistoryRpt history) {
        return Optional.ofNullable(history)
                .filter(AllHistoryRpt::getBidFlag)
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(order.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistoryRpt::getPrice)
                .filter(
                        it -> Optional.ofNullable(order.getLimitPrice())
                                .map(limit -> limit.compareTo(it) >= 0)
                                .orElse(true)
                )
                .orElse(null);
    }

    private BigDecimal getSellPrice(OrderEntity order, AllHistoryRpt history) {
        return Optional.ofNullable(history)
                .filter(it -> !it.getBidFlag())
                .filter(it -> new BigDecimal(it.getUno().substring(0, 14))
                        .subtract(new BigDecimal(order.getUno().substring(0, 14)))
                        .compareTo(TIME_OUT) >= 0)
                .map(AllHistoryRpt::getPrice)
                .filter(
                        it -> Optional.ofNullable(order.getLimitPrice())
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

