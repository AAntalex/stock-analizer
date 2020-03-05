package com.antalex.service.impl;

import com.antalex.holders.BatchDataHolder;
import com.antalex.holders.DataChartHolder;
import com.antalex.holders.DataHolder;
import com.antalex.model.DataChart;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.RateType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.OrderHistoryRepository;
import com.antalex.persistence.repository.OrderRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.OrderService;
import com.antalex.service.TariffPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Map<String, OrderEntity> BATCH_CACHE = new LinkedHashMap<>();

    private final OrderRepository orderRepository;
    private final DataChartService dataChartService;
    private final TariffPlanService tariffPlanService;
    private final OrderHistoryRepository orderHistoryRepository;
    private final Map<String, OrderEntity> cacheOrders;

    OrderServiceImpl(OrderRepository orderRepository,
                     DataChartService dataChartService,
                     TariffPlanService tariffPlanService,
                     OrderHistoryRepository orderHistoryRepository)
    {
        this.orderRepository = orderRepository;
        this.dataChartService = dataChartService;
        this.tariffPlanService = tariffPlanService;
        this.orderHistoryRepository = orderHistoryRepository;
        this.cacheOrders =
                orderRepository.findAllByStatusNot(OrderStatusType.CLOSED).stream()
                        .collect(Collectors.toMap(this::getHashCode, it -> it));
    }

    @Transactional
    @Override
    public OrderEntity save(OrderEntity entity) {
        if (entity == null) {
            return null;
        }
        if (DataChartHolder.isCalcCorr()) {
            return entity;
        }
        cacheOrders.putIfAbsent(getHashCode(entity), entity);
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
            BATCH_CACHE.putIfAbsent(getHashCode(entity), entity);
            if (BATCH_CACHE.size() >= BatchDataHolder.getBachSize()) {
                procBatch();
            }
            return entity;
        } else {
            return orderRepository.save(entity);
        }
    }

    @Override
    public void startBatch(Integer batchSize) {
        BatchDataHolder.setBatchSize(batchSize);
        BATCH_CACHE.clear();
    }

    @Override
    public void stopBatch() {
        BatchDataHolder.setBatchSize(0);
        procBatch();
    }

    @Override
    public List<OrderHistoryRpt> getHistory(String code, String classCode, String sDateBegin, String sDateEnd) {
        if (sDateEnd == null || sDateEnd.isEmpty()) {
            return orderHistoryRepository.findByCodeAndClassCodeAndUnoGreaterThanEqualAndUnoLessThanEqual(
                    code,
                    classCode,
                    sDateBegin,
                    sDateEnd
            );
        }
        return orderHistoryRepository.findByCodeAndClassCodeAndUnoGreaterThanEqual(
                code,
                classCode,
                sDateBegin
        );
    }

    @Transactional
    protected void procBatch() {
        BATCH_CACHE.values().forEach(orderRepository::save);
        BATCH_CACHE.clear();
    }

    @Override
    public List<OrderEntity> findAllByStatus(OrderStatusType status) {
        if (BatchDataHolder.getBachSize() > 0) {
            return cacheOrders.values().stream()
                    .filter(it -> it.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            return orderRepository.findAllByStatus(status);
        }
    }

    @Override
    public List<OrderEntity> findAllByEventAndStatus(EventEntity event, OrderStatusType status) {
        if (BatchDataHolder.getBachSize() > 0) {
            return cacheOrders.values().stream()
                    .filter(it -> it.getEvent() == event && it.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            return orderRepository.findAllByEventAndStatus(event, status);
        }
    }

    @Override
    public List<OrderEntity> findAllByEventAndStatusNot(EventEntity event, OrderStatusType status) {
        if (BatchDataHolder.getBachSize() > 0) {
            return cacheOrders.values().stream()
                    .filter(it -> it.getEvent() == event && it.getStatus() != status)
                    .collect(Collectors.toList());
        } else {
            return orderRepository.findAllByEventAndStatusNot(event, status);
        }
    }

    @Override
    public List<OrderEntity> getProcessedOrders(EventEntity event, OrderStatusType status, EventType type) {
        return orderRepository.findAllByEventAndStatusAndTypeAndResultIsNotNullOrderByUno(event, status, type);
    }

    @Override
    public OrderEntity newOrder(DataChart data,
                                EventEntity event,
                                EventType type,
                                BigDecimal price,
                                Double volume,
                                String caption,
                                OrderEntity main)
    {
        OrderEntity order = new OrderEntity();
        order.setType(type);
        order.setStatus(OrderStatusType.PREPARE);
        order.setEvent(event);
        order.setLimitPrice(price);
        order.setUno(data.getHistory().getUno());
        order.setSecId(data.getHistory().getSecId());
        order.setLotSize(data.getHistory().getLotSize());
        order.setScale(data.getHistory().getScale());
        order.setVolume(volume);
        order.setCaption(caption);
        order.setMain(main);
        order.setIndicators(dataChartService.getIndicatorValues(data));
        order.setTraceValues(dataChartService.getTraceValues());
        order.setStopLimit(getStopLimit(order, data.getData().getCandle().getClose()));
        order.setTakeProfit(getTakeProfit(order, data.getData().getCandle().getClose()));
        addHistory(order, price, order.getUno());
        return this.save(order);
    }

    @Override
    public OrderEntity procLimit(OrderEntity order, DataChart data, Boolean batch) {
        BigDecimal price = data.getData().getCandle().getClose();
        Boolean save = (setMaxPrice(order, price) || setMinPrice(order, price)) && !batch;
        OrderEntity limitOrder = null;
        if (checkStopLimit(order, data)) {
            limitOrder = this.newOrder(
                    data,
                    order.getEvent().getStopLimit().getEvent(),
                    EventType.STOP_LIMIT,
                    order.getStopLimit().getPrice(),
                    order.getVolume(),
                    "",
                    order);
            order.setStatus(OrderStatusType.DONE);
            addHistory(order, price, data.getHistory().getUno());
            save = true;
        }
        if (checkTakeProfit(order, data)) {
            limitOrder = this.newOrder(
                    data,
                    order.getEvent().getTakeProfit().getEvent(),
                    EventType.TAKE_PROFIT,
                    order.getType() == EventType.BUY
                            ? price.subtract(order.getTakeProfit().getSpread())
                            : price.add(order.getTakeProfit().getSpread()),
                    order.getVolume(),
                    "",
                    order);
            order.setStatus(OrderStatusType.DONE);
            addHistory(order, price, data.getHistory().getUno());
            save = true;
        }
        if (save) {
            this.save(order);
        }
        return limitOrder;
    }

    @Override
    public void setPrice(OrderEntity order, BigDecimal price, String uno) {
        if (price != null) {
            order.setPrice(price);
            BigDecimal sum = getSum(order);
            order.getRates().addAll(
                    tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.CASH_FLOW,
                            sum
                    )
            );
            if (order.getType() == EventType.BUY || order.getType() == EventType.SELL) {
                order.setStatus(OrderStatusType.OPEN);
            } else {
                order.setStatus(OrderStatusType.CLOSED);
                OrderEntity mainOrder = order.getMain();
                if (mainOrder != null) {
                    BigDecimal mainSum = getSum(mainOrder);
                    mainOrder.setStatus(OrderStatusType.CLOSED);
                    BigDecimal income = (
                            mainOrder.getType() == EventType.BUY
                                    ? sum.subtract(mainSum)
                                    : mainSum.subtract(sum)
                    )
                            .subtract(
                                    Stream.concat(
                                            order.getRates().stream(),
                                            mainOrder.getRates().stream()
                                    )
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            );

                    List<RateValueEntity> incomeRates = tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.INCOME,
                            income
                    );
                    mainOrder.getRates().addAll(incomeRates);
                    mainOrder.setResult(income
                            .subtract(
                                    incomeRates.stream()
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            )
                    );
                }
            }
            addHistory(order, price, uno);
            Optional.ofNullable(order.getMain())
                    .ifPresent(it -> addHistory(it, price, uno));
            this.save(order);
        }
    }

    private void addHistory(OrderEntity order, BigDecimal price, String uno) {
        if (Objects.isNull(order) || Objects.nonNull(order.getMain())) {
            return;
        }
        OrderHistoryEntity orderHistoryEntity = new OrderHistoryEntity();
        orderHistoryEntity.setPrice(price);
        orderHistoryEntity.setDate(new Date());
        orderHistoryEntity.setStatus(order.getStatus());
        orderHistoryEntity.setType(getTypeByStatus(order));
        orderHistoryEntity.setUno(uno);
        order.getHistory().add(orderHistoryEntity);
    }

    private EventType getTypeByStatus(OrderEntity order) {
        if (order.getStatus() == OrderStatusType.PREPARE || order.getStatus() == OrderStatusType.OPEN) {
            return order.getType();
        } else {
            return order.getType() == EventType.BUY ? EventType.SELL : EventType.BUY;
        }
    }

    private String getHashCode(OrderEntity entity) {
        return entity.getUno() +
                Optional.ofNullable(entity.getMain())
                        .map(it -> it.getUno() + it.getEvent().getCode())
                        .orElse(entity.getEvent().getCode());
    }

    private BigDecimal getSum(OrderEntity order) {
        return Optional.ofNullable(order)
                .map(OrderEntity::getPrice)
                .map(it ->
                        it
                                .multiply(new BigDecimal(order.getVolume()))
                                .multiply(new BigDecimal(order.getLotSize()))
                )
                .orElse(null);
    }

    private Boolean setMaxPrice(OrderEntity order, BigDecimal price) {
        if (
                Optional
                        .ofNullable(order.getMaxPrice())
                        .map(it -> it.compareTo(price) < 0)
                        .orElse(true))
        {
            order.setMaxPrice(price);
            return true;
        }
        return false;
    }

    private Boolean setMinPrice(OrderEntity order, BigDecimal price) {
        if (
                Optional
                        .ofNullable(order.getMinPrice())
                        .map(it -> it.compareTo(price) > 0)
                        .orElse(true))
        {
            order.setMinPrice(price);
            return true;
        }
        return false;
    }

    private Boolean checkStopLimit(OrderEntity order, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        return price != null && order.getStopLimit() != null && order.getPrice() != null &&
                (
                        order.getType() == EventType.BUY && price.compareTo(order.getStopLimit().getStopPrice()) <= 0 ||
                        order.getType() == EventType.SELL && price.compareTo(order.getStopLimit().getStopPrice()) >= 0
                ) || dataChartService.checkEvent(data, order.getEvent().getStopLimit().getEvent());
    }

    private Boolean checkTakeProfit(OrderEntity order, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        if (price == null || order.getTakeProfit() == null || order.getPrice() == null) {
            return false;
        }
        if (!order.getTakeProfit().getActive()) {
            order.getTakeProfit().setActive(
                    order.getType() == EventType.BUY &&
                            price.compareTo(order.getTakeProfit().getPrice()) >= 0 ||
                            order.getType() == EventType.SELL &&
                                    price.compareTo(order.getTakeProfit().getPrice()) <= 0 ||
                            dataChartService.checkEvent(data, order.getEvent().getTakeProfit().getEvent()));
        }
        return order.getTakeProfit().getActive() &&
                (
                        order.getType() == EventType.BUY &&
                                order.getMaxPrice() != null &&
                                price.compareTo(order.getMaxPrice().subtract(order.getTakeProfit().getGap())) <= 0 ||
                                order.getType() == EventType.SELL &&
                                        order.getMinPrice() != null &&
                                        price.compareTo(order.getMinPrice().add(order.getTakeProfit().getGap())) >= 0);
    }

    private StopLimitEntity getStopLimit(OrderEntity order, BigDecimal price) {
        if (order.getEvent() == null ||
                order.getEvent().getStopLimit() == null ||
                order.getType() != EventType.BUY && order.getType() != EventType.SELL)
        {
            return null;
        }
        StopLimitEntity stopLimit = new StopLimitEntity();
        stopLimit.setVolume(order.getVolume());
        StopLimitTuneEntity tune = order.getEvent().getStopLimit();
        if (order.getType() == EventType.BUY) {
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

    private TakeProfitEntity getTakeProfit(OrderEntity order, BigDecimal price) {
        if (order.getEvent() == null ||
                order.getEvent().getTakeProfit() == null ||
                order.getType() != EventType.BUY && order.getType() != EventType.SELL)
        {
            return null;
        }
        TakeProfitEntity takeProfit = new TakeProfitEntity();
        takeProfit.setActive(false);
        takeProfit.setVolume(order.getVolume());
        TakeProfitTuneEntity tune = order.getEvent().getTakeProfit();
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
        if (order.getType() == EventType.BUY) {
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

