package com.antalex.service.impl;

import com.antalex.model.DataChart;
import com.antalex.model.Indicator;
import com.antalex.model.enums.IndicatorType;
import com.antalex.model.Trend;
import com.antalex.service.TrendService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class TrendServiceImpl implements TrendService {
    private static final String TREND = "TREND";
    private static final String HIGH = "HIGH";
    private static final String LOW = "LOW";

    @Override
    public Trend getTrend(List<DataChart> dataList, Integer period, Integer offset) {
        if (dataList.isEmpty() || dataList.size() < period + offset) {
            return null;
        }
        int start = period > 0 ? dataList.size() - period - offset : 0;
        int end = dataList.size() - offset - 1;
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

    @Override
    public void setTrendToIndicator(Trend trend, List<DataChart> dataList, Boolean multiple) {
        if (trend == null) {
            return;
        }
        StringBuilder codeBuilder = new StringBuilder(getTrendCode(trend.getPeriod(), trend.getOffset()));
        if (multiple) {
            codeBuilder
                    .append('_')
                    .append(trend.getStart())
                    .append('_')
                    .append(trend.getEnd() - trend.getStart() + 1);
        }
        String code = codeBuilder.append('_').toString();
        int start = Integer.max(trend.getStart() - trend.getOffset(), 0);
        int end = Integer.min(trend.getEnd() + trend.getOffset() + 1, dataList.size());
        IntStream.range(start, end)
                .forEach(
                        idx -> {
                            HashMap<String, Indicator> indicators = dataList.get(idx).getIndicators();
                            indicators.put(
                                    code + HIGH,
                                    Indicator.builder()
                                            .period(trend.getPeriod())
                                            .value(trend.getHigh().f(idx))
                                            .code(code + HIGH)
                                            .name(TREND)
                                            .type(IndicatorType.TREND)
                                            .build()
                            );
                            indicators.put(
                                    code + LOW,
                                    Indicator.builder()
                                            .period(trend.getPeriod())
                                            .value(trend.getLow().f(idx))
                                            .code(code + LOW)
                                            .name(TREND)
                                            .type(IndicatorType.TREND)
                                            .build()
                            );
                        });
    }

}

