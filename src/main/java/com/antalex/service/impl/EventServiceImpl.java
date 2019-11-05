package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.enums.EventType;
import com.antalex.persistence.entity.*;
import com.antalex.persistence.repository.EventRepository;
import com.antalex.service.DataChartService;
import com.antalex.service.DealService;
import com.antalex.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private static final String TEST = "TEST";

    private final EventRepository eventRepository;
    private final DataChartService dataChartService;
    private final DealService dealService;

    @Override
    public String getCheckCode(DataChart data, EventEntity event) {
        return event.getTriggers()
                .stream()
                .map(it -> dataChartService.getBool(data, it.getTrigger().getCondition()) ? "1" : "0")
                .reduce("", (a, b) -> a + b);
    }

    @Override
    public EventEntity findByCode(String code) {
        return eventRepository.findByCode(code);
    }

    @Override
    public void apply(DataChart data, EventEntity event) {
        if (isTest(event) || check(data, event)) {
            if (!isTest(event)) {
                dealService.newDeal(data, event, event.getType(), null);
            } else {
                String checkCode = getCheckCode(data, event);
                dealService.newDeal(data, event, EventType.BUY, checkCode);
                dealService.newDeal(data, event, EventType.SELL, checkCode);
            }
        }
    }

    private Boolean check(DataChart data, EventEntity event) {
        return !event.getTriggers()
                .stream()
                .allMatch(it -> dataChartService.getBool(data, it.getTrigger().getCondition()));
    }

    private Boolean isTest(EventEntity event) {
        return TEST.equals(event.getCode());
    }
}

