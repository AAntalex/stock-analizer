package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.enums.DealStatusType;
import com.antalex.model.enums.EventType;
import com.antalex.model.enums.StatusType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.EventRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import com.antalex.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final DataChartService dataChartService;
    private final DealService dealService;

    @Override
    public EventEntity findByCode(String code) {
        return eventRepository.findByCode(code);
    }

    @Override
    public void apply(DataChart data, EventEntity event) {
        if (
                (event.getType() == EventType.BUY || event.getType() == EventType.SELL) &&
                        dataChartService.checkEvent(data, event))
        {



            if (dealService.findAllByEventAndStatusNot(event, DealStatusType.CLOSED).isEmpty()) {



                dealService.newDeal(
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
        dealService.findAllByEventAndStatus(event, DealStatusType.OPEN)
                .forEach(it -> dealService.procLimit(it, data));
    }

    @Override
    public void applyAll(DataChart data) {
        eventRepository.findAllByStatusAndType(StatusType.ENABLED, EventType.BUY)
                .forEach(event -> apply(data, event));
        eventRepository.findAllByStatusAndType(StatusType.ENABLED, EventType.SELL)
                .forEach(event -> apply(data, event));
    }
}

