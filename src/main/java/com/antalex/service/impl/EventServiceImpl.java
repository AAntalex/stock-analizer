package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.EventRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.OrderService;
import com.antalex.service.EventService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final DataChartService dataChartService;
    private final OrderService orderService;
    private final List<EventEntity> eventList;


    EventServiceImpl (EventRepository eventRepository,
                      DataChartService dataChartService,
                      OrderService orderService)
    {
        this.eventRepository = eventRepository;
        this.dataChartService = dataChartService;
        this.orderService = orderService;
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



            if (orderService.findAllByEventAndStatusNot(event, OrderStatusType.CLOSED).isEmpty()) {



                orderService.newOrder(
                        data,
                        event,
                        event.getType(),
                        null,
                        1d,
                        null,
                        null
                );



            }


        }
        orderService.findAllByEventAndStatus(event, OrderStatusType.OPEN)
                .forEach(it -> orderService.procLimit(it, data));
    }

    @Override
    public void applyAll(DataChart data) {
        eventList.forEach(event -> apply(data, event));
    }
}

