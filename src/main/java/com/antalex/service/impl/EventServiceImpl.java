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
                        check(data, event))
        {
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

    private Boolean check(DataChart data, EventEntity event) {
        return !event.getTriggers()
                .stream()
                .allMatch(it -> dataChartService.getBool(data, it.getTrigger().getCondition()));
    }
}

