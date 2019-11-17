package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.Trend;
import com.antalex.service.TrendService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class TrendServiceImpl implements TrendService {
    private static final String TREND = "TREND";

    @Override
    public Trend getTrend(List<DataChart> dataList, Integer period, Integer offset) {
        if (dataList.isEmpty() || dataList.size() < period + offset) {
            return null;
        }
        int start = period > 0 ? dataList.size() - period - offset : 0;
        int end = dataList.size() - offset - 1;
        if (start >= 0 && end > start) {
            Trend trend = new Trend(
                    start,
                    dataList.get(start).getData().getCandle(),
                    end,
                    dataList.get(end).getData().getCandle(),
                    period,
                    offset
            );

            IntStream.rangeClosed(start, end)
                    .forEach(i -> trend.setPoint(i, dataList.get(i).getData().getCandle()));
            return trend;
        }
        return null;
    }

    @Override
    public String getTrendCode(Integer period, Integer offset) {
        StringBuilder codeBuilder = new StringBuilder(TREND);
        if (period > 0) {
            codeBuilder
                    .append(period);
        }
        if (offset > 0) {
            codeBuilder
                    .append('_')
                    .append(offset);
        }
        return codeBuilder.toString();
    }
}

