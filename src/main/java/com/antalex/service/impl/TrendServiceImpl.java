package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.Trend;
import com.antalex.service.TrendService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class TrendServiceImpl implements TrendService {
    @Override
    public Trend getTrend(List<DataChart> dataList, Integer period) {
        if (dataList.isEmpty()) {
            return null;
        }
        int start = period > 0 ? dataList.size() - period : 0;
        int end = dataList.size() - 1;
        Trend trend = new Trend(
                start,
                dataList.get(start).getData().getCandle(),
                end,
                dataList.get(end).getData().getCandle()
        );

        IntStream.rangeClosed(start, end)
                .forEach(i -> trend.setPoint(i, dataList.get(i).getData().getCandle()));

        return trend;
    }
}

