package com.antalex.service.impl;

import com.antalex.dto.DataChartDto;
import com.antalex.holders.DateFormatHolder;
import com.antalex.service.ChartFormer;
import com.antalex.service.ChartService;
import com.antalex.service.IndicatorService;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class ChartServiceImpl implements ChartService {
    private ChartFormer chartFormer;
    private AllHistoryService allHistoryService;
    private IndicatorService indicatorService;

    @Override
    public void init() {
        chartFormer.init();
        indicatorService.init();
    }

    @Override
    public List<DataChartDto> query(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation) {
        chartFormer.setApproximation(approximation);
        splitDate(sDateBegin, sDateEnd)
                .forEach(interval ->
                        allHistoryService.query(secClass, interval.getKey(), interval.getValue(), stockClass)
                                .forEach(chartFormer::add)
                );
        allHistoryService.query(secClass, sDateBegin, sDateEnd, stockClass).forEach(chartFormer::add);
        return chartFormer.getDataList(sDateBegin, sDateEnd);
    }

    private List<Pair<String, String>> splitDate(String sDateBegin, String sDateEnd) {
        Integer oldApproximation = DateFormatHolder.getApproximation();
        chartFormer.setApproximation(0);

        List<Pair<String, String>> result = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date dateBegin = DateFormatHolder.getDateFromString(sDateBegin, 0);
        Date dateEnd = DateFormatHolder.getDateFromString(sDateEnd, 0);
        calendar.setTime(dateBegin);
        Date nextDate = getNextDate(calendar, dateEnd);
        while (dateBegin.compareTo(nextDate) < 0) {
            result.add(
                    new Pair<>(
                            DateFormatHolder.getStringFromDate(dateBegin),
                            DateFormatHolder.getStringFromDate(nextDate)
                    )
            );
            dateBegin = nextDate;
            nextDate = getNextDate(calendar, dateEnd);
        }
        chartFormer.setApproximation(oldApproximation);
        return result;
    }

    private Date getNextDate(Calendar calendar, Date dateEnd) {
        calendar.add(Calendar.DATE, 1);
        Date result = calendar.getTime();
        if (dateEnd.compareTo(result) < 0) {
            return dateEnd;
        }
        return result;
    }
}

