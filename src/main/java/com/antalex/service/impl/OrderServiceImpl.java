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
import com.antalex.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {
    private static final Map<String, OrderEntity> BATCH_CACHE = new LinkedHashMap<>();
    private static final BigDecimal K = BigDecimal.valueOf(1.1);
    private static final long TIME_OUT = 60;
    private Date flushTime = new Date();

    private final OrderRepository orderRepository;
    private final DataChartService dataChartService;
    private final TariffPlanService tariffPlanService;
    private final OrderHistoryRepository orderHistoryRepository;
    private final Map<String, OrderEntity> cacheOrders;
    private final DealService dealService;
    private final AccountService accountService;

    OrderServiceImpl(OrderRepository orderRepository,
                     DataChartService dataChartService,
                     TariffPlanService tariffPlanService,
                     OrderHistoryRepository orderHistoryRepository,
                     DealService dealService,
                     AccountService accountService)
    {
        this.orderRepository = orderRepository;
        this.dataChartService = dataChartService;
        this.tariffPlanService = tariffPlanService;
        this.orderHistoryRepository = orderHistoryRepository;
        this.dealService = dealService;
        this.accountService = accountService;
        this.cacheOrders =
                orderRepository.findAllByStatusNot(OrderStatusType.CLOSED).stream()
                        .collect(Collectors.toMap(this::getHashCode, it -> it));
    }

    @Transactional
    @Override
    public OrderEntity save(OrderEntity entity, Boolean force) {
        if (entity == null) {
            return null;
        }
        if (DataChartHolder.isCalcCorr()) {
            return entity;
        }
        if (BatchDataHolder.getBachSize() > 0) {
            cacheOrders.putIfAbsent(getHashCode(entity), entity);
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
        if (!force && BatchDataHolder.getBachSize() > 0) {
            BATCH_CACHE.putIfAbsent(getHashCode(entity), entity);
            if (BATCH_CACHE.size() >= BatchDataHolder.getBachSize() || needFlush()) {
                procBatch();
            }
            return entity;
        } else {
            return orderRepository.save(entity);
        }
    }

    private Boolean needFlush() {
        if (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - this.flushTime.getTime()) >= TIME_OUT) {
            this.flushTime = new Date();
            return true;
        }
        return false;
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
    public List<OrderEntity> findAllByStatusNot(OrderStatusType status) {
        if (BatchDataHolder.getBachSize() > 0) {
            return new ArrayList<>(cacheOrders.values());
        } else {
            return orderRepository.findAllByStatusNot(status);
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
    public Double getBalance(OrderEntity order) {
        return order.getDeals()
                .stream()
                .map(DealEntity::getBalance)
                .reduce(0d, (sum, it) -> sum + it );
    }

    @Override
    public BigDecimal getTotalSum(OrderEntity order) {
        return order.getDeals()
                .stream()
                .map(DealEntity::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderEntity> findAllBySecAndTypeAndStatusAndUnoLessThan
            (
                    ClassSecEntity sec
                    , EventType type
                    , OrderStatusType status
                    , String uno
            )
    {
        if (BatchDataHolder.getBachSize() > 0) {
            return cacheOrders.values().stream()
                    .filter(
                            it ->
                                    it.getSec() == sec
                                            && it.getType() == type
                                            && it.getStatus() == status
                                            && it.getUno().compareTo(uno) < 0
                    )
                    .sorted(Comparator.comparing(OrderEntity::getUno))
                    .collect(Collectors.toList());
        } else {
            return orderRepository.findAllBySecAndTypeAndStatusAndUnoLessThan(sec, type, status, uno)
                    .stream()
                    .sorted(Comparator.comparing(OrderEntity::getUno))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public OrderEntity newOrder(DataChart data,
                                EventEntity event,
                                EventType type,
                                BigDecimal price,
                                AccountEntity account,
                                Double volume,
                                String caption,
                                OrderEntity main)
    {
        if (volume.equals(0d)) {
            return null;
        }
        BigDecimal curPrice = data.getData().getCandle().getClose();
        OrderEntity order = new OrderEntity();
        order.setType(type);
        order.setStatus(OrderStatusType.PREPARE);
        order.setBalance(0d);
        order.setEvent(event);
        order.setLimitPrice(price);
        order.setUno(data.getHistory().getUno());
        order.setSec(data.getHistory().getSec());
        order.setAccount(account);
        order.setVolume(volume);
        order.setCaption(caption);
        order.setMain(main);
        order.setIndicators(dataChartService.getIndicatorValues(data));
        order.setTraceValues(dataChartService.getTraceValues());
        order.setStopLimit(getStopLimit(order, curPrice));
        order.setTakeProfit(getTakeProfit(order, curPrice));
        order.setTransId(orderRepository.getTransId());
        order.setOrderNum("SND#" + order.getTransId().toString());
        order.setDate(new Date());

        addHistory(order, price, order.getUno());
        this.setLockedSum(
                order,
                Optional
                        .ofNullable(price)
                        .orElse(curPrice.multiply(K))
                        .multiply(new BigDecimal(volume))
                        .multiply(new BigDecimal(data.getHistory().getSec().getLotSize()))
        );
        return this.save(order, true);
    }

    @Override
    public OrderEntity procLimit(OrderEntity order, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        Boolean save = setMaxPrice(order, price) || setMinPrice(order, price);
        OrderEntity limitOrder = null;
        if (checkStopLimit(order, data)) {
            limitOrder = this.newOrder(
                    data,
                    order.getEvent().getStopLimit().getEvent(),
                    EventType.STOP_LIMIT,
                    order.getStopLimit().getPrice(),
                    order.getAccount(),
                    order.getBalance(),
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
                    order.getAccount(),
                    order.getBalance(),
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
    public void process(OrderEntity order, DataChart data) {
        Optional.ofNullable(order)
                .map(it -> procCancel(it, data))
                .map(it -> procActive(it, data))
                .map(it -> procOpen(it, data));
    }

    private void setLockedSum(OrderEntity order, BigDecimal lockedSum) {
        if (Objects.isNull(order.getEvent()) || Objects.nonNull(order.getMain())) {
            return;
        }
        accountService.getMoneyPosition(order.getAccount(), order.getSec().getCur())
                .ifPresent(it ->
                        it.setUsedAmount(
                                Optional
                                        .ofNullable(lockedSum)
                                        .orElse(BigDecimal.ZERO)
                                        .subtract(
                                                Optional
                                                        .ofNullable(order.getLockedSum())
                                                        .orElse(BigDecimal.ZERO)
                                        )
                                        .add(
                                                Optional
                                                        .ofNullable(it.getUsedAmount())
                                                        .orElse(BigDecimal.ZERO)
                                        )
                        ));
        order.setLockedSum(lockedSum);
    }

    private List<OrderEntity> getOrdersForQuit(OrderEntity order) {
        if (Objects.isNull(order.getEvent())) {
            return findAllBySecAndTypeAndStatusAndUnoLessThan(
                    order.getSec(),
                    order.getType() == EventType.BUY ? EventType.SELL : EventType.BUY,
                    OrderStatusType.OPEN,
                    order.getUno());
        } else {
           if (Objects.isNull(order.getMain())) {
               return Collections.emptyList();
           } else {
               return Collections.singletonList(order.getMain());
           }
        }
    }

    private Boolean quitDealWithOrder(DealEntity deal, OrderEntity order) {
        return order.getDeals()
                .stream()
                .filter(it -> it.getBalance().compareTo(0d) > 0)
                .anyMatch(it -> dealService.quitDeals(deal, it));
    }

    private Boolean quitOrder(OrderEntity order) {
        List<OrderEntity> ordersForQuit = getOrdersForQuit(order);
        return !ordersForQuit.isEmpty() && order.getDeals().stream()
                .noneMatch(
                        deal -> ordersForQuit
                                .stream()
                                .noneMatch(it -> quitDealWithOrder(deal, it))
                );
    }

    private OrderEntity procCancel(OrderEntity order, DataChart data) {
        if (order.getStatus() == OrderStatusType.CANCELED) {
            if (order.getDeals().size() == 0) {
                order.setStatus(OrderStatusType.CLOSED);
            } else {
                order.setStatus(OrderStatusType.ACTIVE);
            }
            addHistory(order, data.getData().getCandle().getClose(), data.getHistory().getUno());
            this.save(order);
        }
        return order;
    }

    private OrderEntity procActive(OrderEntity order, DataChart data) {
        if (order.getStatus() == OrderStatusType.ACTIVE) {
            order.setBalance(getBalance(order));
            order.setTotalSum(getTotalSum(order));
            this.setLockedSum(order, order.getTotalSum());
            quitOrder(order);
            order.getRates().addAll(
                    tariffPlanService.applyForType(
                            tariffPlanService.getMain(),
                            RateType.CASH_FLOW,
                            order.getTotalSum()
                    )
            );
            order.setStatus(OrderStatusType.OPEN);
            addHistory(order, data.getData().getCandle().getClose(), data.getHistory().getUno());
            this.save(order);
        }
        return order;
    }

    private OrderEntity procOpen(OrderEntity order, DataChart data) {
        if ((order.getStatus() == OrderStatusType.OPEN || order.getStatus() == OrderStatusType.DONE)
                && order.getDeals().stream().noneMatch(it -> it.getBalance().compareTo(0d) > 0))
        {
            order.setResult(
                    order.getDeals()
                            .stream()
                            .map(DealEntity::getResult)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .subtract(
                                    order.getRates()
                                            .stream()
                                            .map(RateValueEntity::getValue)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            )
                            .add(
                                    Optional.ofNullable(order.getResult())
                                            .orElse(BigDecimal.ZERO)
                            )
            );
            Optional.ofNullable(order.getMain())
                    .ifPresent(
                            mainOrder ->
                                    mainOrder.setResult(
                                            Optional.ofNullable(mainOrder.getResult())
                                                    .orElse(BigDecimal.ZERO)
                                                    .add(order.getResult())
                                    )
                    );
            this.setLockedSum(order, BigDecimal.ZERO);
            order.setStatus(OrderStatusType.CLOSED);
            addHistory(order, data.getData().getCandle().getClose(), data.getHistory().getUno());
            this.save(order);
        }
        return order;
    }

    @Override
    public void processAll(DataChart data) {
        findAllByStatusNot(OrderStatusType.CLOSED).stream()
                .sorted(Comparator.comparing(OrderEntity::getUno))
                .forEach(it -> process(it, data));
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
                        .map(it -> it.getUno() + Optional.ofNullable(it.getEvent()).map(EventEntity::getCode))
                        .orElse(Optional.ofNullable(entity.getEvent()).map(EventEntity::getCode).orElse(""));
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
        return price != null && order.getStopLimit() != null &&
                (
                        order.getType() == EventType.BUY && price.compareTo(order.getStopLimit().getStopPrice()) <= 0 ||
                        order.getType() == EventType.SELL && price.compareTo(order.getStopLimit().getStopPrice()) >= 0
                ) || dataChartService.checkEvent(data, order.getEvent().getStopLimit().getEvent());
    }

    private Boolean checkTakeProfit(OrderEntity order, DataChart data) {
        BigDecimal price = data.getData().getCandle().getClose();
        if (price == null || order.getTakeProfit() == null) {
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

