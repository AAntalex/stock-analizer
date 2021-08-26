package com.antalex.service.impl;

import com.antalex.dto.DataChartDto;
import com.antalex.holders.DateFormatHolder;
import com.antalex.persistence.entity.AllHistoryRpt;
import com.antalex.persistence.entity.ClassSecEntity;
import com.antalex.persistence.entity.TradeClassesEntity;
import com.antalex.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class ChartServiceImpl implements ChartService {
    private static final String START_TIME = "000000";
    private static final String END_TIME = "235959";

    private ChartFormer chartFormer;
    private AllHistoryService allHistoryService;
    private IndicatorService indicatorService;
    private OrderService orderService;
    private TradeClassesService tradeClassesService;
    private ClassSecService classSecService;

    @Override
    public void init() {
        chartFormer.init();
        indicatorService.init();
        System.gc();
    }

    @Override
    public void getData(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation) {
        TradeClassesEntity tradeClassesEntity = tradeClassesService.findOneByCode(stockClass);
        chartFormer.setApproximation(approximation);
        String secClasses = secClass.isEmpty() ? classSecService.findForAutoTrade()
                .stream()
                .map(ClassSecEntity::getCode)
                .map(it -> it.concat(","))
                .reduce("", String::concat) : secClass;

        DateFormatHolder.splitDate(
                sDateBegin, sDateEnd,
                Optional.ofNullable(tradeClassesEntity.getStartTime()).orElse(START_TIME),
                Optional.ofNullable(tradeClassesEntity.getEndTime()).orElse(END_TIME),
                Calendar.DATE
        )
                .forEach(interval -> {

                    chartFormer.cutData(1000);
                    log.info("AAA INTERVAL!!! 1 = " + interval.getKey() + " 2 = " + interval.getValue() + " ThreadId: " + Thread.currentThread().getId());


                    allHistoryService.query(secClasses, interval.getKey(), interval.getValue(), stockClass)
                            .stream()
                            .sorted(Comparator.comparing(AllHistoryRpt::getUno))
                            .forEachOrdered(chartFormer::add);
                    orderService.getHistory(secClasses, stockClass, interval.getKey(), interval.getValue())
                            .forEach(chartFormer::addDealHistory);
                });
    }

    @Override
    public List<DataChartDto> query(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation) {
        getData(secClass, sDateBegin, sDateEnd, stockClass, approximation);
        return chartFormer.getDataList(sDateBegin, sDateEnd);
    }
}

