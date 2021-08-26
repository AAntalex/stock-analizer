package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.EventRepository;
import com.antalex.service.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final DataChartService dataChartService;
    private final OrderService orderService;
    private final AccountService accountService;
    private final ClassSecService classSecService;
    private final List<EventEntity> eventList;


    EventServiceImpl (EventRepository eventRepository,
                      DataChartService dataChartService,
                      OrderService orderService,
                      AccountService accountService,
                      ClassSecService classSecService)
    {
        this.eventRepository = eventRepository;
        this.dataChartService = dataChartService;
        this.orderService = orderService;
        this.accountService = accountService;
        this.classSecService = classSecService;
        this.eventList = Stream.concat(
                eventRepository.findAllByStatusAndType(StatusType.ENABLED, EventType.BUY).stream(),
                eventRepository.findAllByStatusAndType(StatusType.ENABLED, EventType.SELL).stream()
        ).collect(Collectors.toList());
    }

    @Override
    public EventEntity findByCode(String code) {
        return eventRepository.findByCode(code);
    }

    @Override
    public void apply(DataChart data, EventEntity event) {
        if (
                (event.getType() == EventType.BUY || event.getType() == EventType.SELL) &&
                        dataChartService.checkEvent(data, event) &&
                        Optional.ofNullable(event.getTakeProfit())
                                .map(TakeProfitTuneEntity::getEvent)
                                .map(it -> !dataChartService.checkEvent(data, it))
                                .orElse(true) &&
                        Optional.ofNullable(event.getStopLimit())
                                .map(StopLimitTuneEntity::getEvent)
                                .map(it -> !dataChartService.checkEvent(data, it))
                                .orElse(true)
                )
        {
            accountService
                    .findAll()
                    .forEach(acc ->
                        Optional.of(accountService.getAvailableAmount(acc, data.getHistory().getSec().getCur()))
                                .filter(it -> it.compareTo(BigDecimal.ZERO) > 0)
                                .map(
                                        it -> classSecService.getVolume(
                                                data.getHistory().getSec()
                                                , it
                                                , data.getData().getCandle().getClose()
                                        )
                                )
                                .filter(it -> it.compareTo(0d) > 0)
                                .map(it ->
                                        orderService.newOrder(
                                                data,
                                                event,
                                                event.getType(),
                                                null,
                                                acc,
                                                it,
                                                null,
                                                null
                                        )
                                )
                    );
        }
        orderService.findAllBySecAndEventAndStatus(data.getHistory().getSec(), event, OrderStatusType.OPEN)
                .forEach(it -> orderService.procLimit(it, data));
    }

    @Override
    public void applyAll(DataChart data) {
        eventList.forEach(event -> apply(data, event));
    }
}

