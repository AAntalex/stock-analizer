package com.antalex.service.impl;

import com.antalex.dto.DataChartDto;
import com.antalex.holders.DateFormatHolder;
import com.antalex.persistence.entity.TradeClassesEntity;
import com.antalex.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class ChartServiceImpl implements ChartService {
    private static final String START_TIME = "000000";
    private static final String END_TIME = "235959";

    private ChartFormer chartFormer;
    private AllHistoryService allHistoryService;
    private IndicatorService indicatorService;
    private OrderService orderService;
    private TradeClassesService tradeClassesService;

    @Override
    public void init() {
        chartFormer.init();
        indicatorService.init();
    }

    @Override
    public List<DataChartDto> query(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation) {
        TradeClassesEntity tradeClassesEntity = tradeClassesService.findOneByCode(stockClass);
        chartFormer.setApproximation(approximation);
        DateFormatHolder.splitDate(
                sDateBegin, sDateEnd,
                Optional.ofNullable(tradeClassesEntity.getStartTime()).orElse(START_TIME),
                Optional.ofNullable(tradeClassesEntity.getEndTime()).orElse(END_TIME),
                Calendar.DATE
        )
                .forEach(interval -> {
                    allHistoryService.query(secClass, interval.getKey(), interval.getValue(), stockClass)
                            .forEach(chartFormer::add);
                    orderService.getHistory(secClass, stockClass, interval.getKey(), interval.getValue())
                            .forEach(chartFormer::addDealHistory);
                });
        return chartFormer.getDataList(sDateBegin, sDateEnd);
    }
}

