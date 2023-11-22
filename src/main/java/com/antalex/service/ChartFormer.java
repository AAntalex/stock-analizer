package com.antalex.service;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.VolumeDto;
import com.antalex.holders.DataChartHolder;
import com.antalex.holders.DataHolder;
import com.antalex.holders.DateFormatHolder;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.*;
import com.antalex.model.enums.OrderStatusType;
import com.antalex.persistence.entity.AllHistoryRpt;
import com.antalex.persistence.entity.OrderHistoryRpt;
import com.antalex.persistence.entity.History;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ChartFormer {
    private static final String START_TIME = "100000";
    private static final String END_TIME = "183959";
    private final DtoMapper dtoMapper;
    private final IndicatorService indicatorService;
    private final TrendService trendService;
    private final DataChartService dataChartService;
    private final TestService testService;

    @Autowired
    ChartFormer(DtoMapper dtoMapper,
                IndicatorService indicatorService,
                TrendService trendService,
                DataChartService dataChartService,
                TestService testService) {
        this.dtoMapper = dtoMapper;
        this.indicatorService = indicatorService;
        this.trendService = trendService;
        this.dataChartService = dataChartService;
        this.testService = testService;
    }

    public void setApproximation(Integer approximation) {
        DateFormatHolder.setApproximation(approximation);
    }

    private Boolean checkTime(String uno) {
        String curTime = DateFormatHolder.getTimeString(uno);
        return curTime.compareTo(START_TIME) >= 0 && curTime.compareTo(END_TIME) <= 0;
    }

    private DataChart getDataChart(Date date) {
        return dataChartService.getCache().getDataList()
                .stream()
                .filter(it -> date.equals(it.getDate()))
                .findFirst()
                .orElse(null);
    }


    private DataChart getDataChart(AllHistoryRpt history) {
        dataChartService.setCurCache(history.getSec());
        DataChart dataChart = dataChartService.getCache().getCurData();
        if (Optional
                .ofNullable(dataChart)
                .map(DataChart::getDate)
                .map(it -> it.compareTo(history.getDate()) < 0)
                .orElse(true))
        {
            dataChart = new DataChart();
            dataChart.setDate(history.getDate());
            dataChart.setCalcIndicator(false);
            dataChart.setIdx(dataChartService.getCache().getDataList().size());
            dataChartService.getCache().setCurData(dataChart);
        }
        dataChart.setHistory(history);
        return dataChart;
    }

    private void setTrend() {
        setTrend(0, 0);
    }

    private void setTrend(int period) {
        setTrend(period, 0);
    }

    private void setTrend(int period, int offset) {
        indicatorService.setTrendToIndicator(
                dataChartService.getTrend(period, offset),
                dataChartService.getCache().getDataList(),
                false
        );
    }

    private void addPointToTrend(Integer period) {
        if (!dataChartService.getCache().getTrends().containsKey(period)) {
            dataChartService.getCache().getTrends().put(period, new TrendSnapShot());
        }
        TrendSnapShot snapShot = dataChartService.getCache().getTrends().get(period);
        List<DataChart> dataList = dataChartService.getCache().getDataList();
        Trend trend = snapShot.getTrend();
        if (trend == null) {
            if (dataList.size() - period >= snapShot.getStart()) {
                snapShot.setTrend(trendService.getTrend(dataList, period, 0));
            }
        } else {
            int x = dataList.size() - 1;
            Candlestick y = dataList.get(dataList.size() - 1).getData().getCandle();
            if (trend.checkPoint(x, y)) {
                snapShot.getTrend().setPoint(x, y);
            } else {
                indicatorService.setTrendToIndicator(trend, dataList, true);
                snapShot.setStart(x);
                snapShot.setTrend(null);
            }
        }
    }

    private void addPoint(DataChart dataChart){
        if (Optional.ofNullable(dataChartService.getCache().getLastData())
                .map(DataChart::getDate)
                .map(it -> it.compareTo(dataChart.getDate()) != 0)
                .orElse(true))
        {
            if (Objects.nonNull(dataChartService.getCache().getLastData())) {
/*

                addPointToTrend(30);
                addPointToTrend(60);
                addPointToTrend(120);

*/
                indicatorService.calcAll(dataChartService.getCache().getLastData());
            }
            dataChartService.getCache().setLastData(dataChart);
        }
        if (DataChartHolder.isCalcCorr()) {
            testService.calcCorr(dataChart);
        }
        if (DataChartHolder.isTest()) {
            testService.test(dataChart);
        }
    }

    public void addDealHistory(OrderHistoryRpt history) {
        dataChartService.setCurCache(history.getSec());
        DataChart dataChart = getDataChart(history.getDate());
        if (
                (
                        history.getStatus() == OrderStatusType.OPEN ||
                                history.getStatus() == OrderStatusType.CLOSED
                ) &&
                        Optional.ofNullable(dataChart)
                                .map(DataChart::getOrderHistory)
                                .map(it -> !it.containsKey(history.getUno()))
                                .orElse(false))
        {
            dataChart.getOrderHistory().put(history.getUno(), history);
        }
    }

    public synchronized void add(AllHistoryRpt history) {
        dataChartService.setCurCache(history.getSec());
        String uno = history.getUno();
        if (checkTime(uno) && Optional
                .ofNullable(dataChartService.getCache().getCurData())
                .map(DataChart::getHistory)
                .map(AllHistoryRpt::getUno)
                .map(it -> it.compareTo(uno) < 0)
                .orElse(true))
        {
            if (history.getQuotes() != null) {
                addQuotes(history);
            }
            if (history.getTradeNum() != null) {
                addPoint(addOrder(history));
            }




//            test(history, dataChart);












        }
    }

    private DataChart addQuotes(AllHistoryRpt history) {
        DataChart dataChart = getDataChart(history);
        List<String> quotesList = Arrays.asList(history.getQuotes().split(";"));
        HashMap<BigDecimal, BigDecimal> currentQuotesBid = new HashMap<>();
        HashMap<BigDecimal, BigDecimal> currentQuotesOffer = new HashMap<>();
        for (int i = 0; i < quotesList.size(); i+=2) {
            BigDecimal price = new BigDecimal(quotesList.get(i));
            BigDecimal volume = new BigDecimal(quotesList.get(i + 1));
            if (price.compareTo(BigDecimal.ZERO) > 0) {
                currentQuotesBid.put(price, volume);
            } else {
                currentQuotesOffer.put(price.negate(), volume);
            }
        }
        Boolean isNew = dataChart.getQuotes().size() == 0;
        addDataQuotes(dataChart, currentQuotesBid, true, isNew);
        addDataQuotes(dataChart, currentQuotesOffer, false, isNew);

        return dataChart;
    }

    private void addDataQuotes(DataChart dataChart, HashMap<BigDecimal, BigDecimal> quotesSource, boolean bidFlag, Boolean isNew) {
        dataChart.getQuotes()
                .entrySet()
                .stream()
                .filter(it -> !quotesSource.containsKey(it.getKey()))
                .forEach(it ->
                        addData(
                                bidFlag ? it.getValue().getBid() : it.getValue().getOffer(),
                                BigDecimal.ZERO, 0d
                        )
                );
        List<VolumeDto> quotesList;
        if (bidFlag) {
            quotesList = quotesSource.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .map(it -> new VolumeDto(it.getKey(), it.getValue()))
                    .collect(Collectors.toList());

            Optional.ofNullable(dataChartService.getCache().getLastBidQuotes())
                    .filter(it -> !it.isEmpty())
                    .ifPresent(lastQuotes -> {
                        quotesList.stream()
                                .filter(it ->
                                        it.getPrice().compareTo(lastQuotes.get(0).getPrice()) > 0 ||
                                                it.getPrice().compareTo(lastQuotes.get(0).getPrice()) == 0 &&
                                                        it.getVolume().compareTo(lastQuotes.get(0).getVolume()) > 0
                                )
                                .forEach(it -> {
                                    BigDecimal result = Optional
                                            .ofNullable(dataChart.getBidUp())
                                            .orElse(BigDecimal.ZERO)
                                            .add(it.getVolume());
                                    if (it.getPrice().compareTo(lastQuotes.get(0).getPrice()) == 0) {
                                        result = result.subtract(lastQuotes.get(0).getVolume());
                                    }
                                    dataChart.setBidUp(result);
                                });
                        lastQuotes.stream()
                                .filter(it ->
                                        quotesList.isEmpty() ||
                                                it.getPrice().compareTo(quotesList.get(0).getPrice()) > 0 ||
                                                it.getPrice().compareTo(quotesList.get(0).getPrice()) == 0 &&
                                                        it.getVolume().compareTo(quotesList.get(0).getVolume()) > 0
                                )
                                .forEach(it -> {
                                    BigDecimal result = Optional
                                            .ofNullable(dataChart.getBidDown())
                                            .orElse(BigDecimal.ZERO)
                                            .add(it.getVolume());
                                    if (!quotesList.isEmpty() &&
                                            it.getPrice().compareTo(quotesList.get(0).getPrice()) == 0)
                                    {
                                        result = result.subtract(quotesList.get(0).getVolume());
                                    }
                                    dataChart.setBidDown(result);
                                });
                    });

            dataChartService.getCache().setLastBidQuotes(quotesList);
        } else {
            quotesList = quotesSource.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(it -> new VolumeDto(it.getKey(), it.getValue()))
                    .collect(Collectors.toList());


            Optional.ofNullable(dataChartService.getCache().getLastOfferQuotes())
                    .filter(it -> !it.isEmpty())
                    .ifPresent(lastQuotes -> {
                        quotesList.stream()
                                .filter(it ->
                                        it.getPrice().compareTo(lastQuotes.get(0).getPrice()) < 0 ||
                                                it.getPrice().compareTo(lastQuotes.get(0).getPrice()) == 0 &&
                                                        it.getVolume().compareTo(lastQuotes.get(0).getVolume()) > 0
                                )
                                .forEach(it -> {
                                    BigDecimal result = Optional
                                            .ofNullable(dataChart.getOfferUp())
                                            .orElse(BigDecimal.ZERO)
                                            .add(it.getVolume());
                                    if (it.getPrice().compareTo(lastQuotes.get(0).getPrice()) == 0) {
                                        result = result.subtract(lastQuotes.get(0).getVolume());
                                    }
                                    dataChart.setOfferUp(result);
                                });
                        lastQuotes.stream()
                                .filter(it ->
                                        quotesList.isEmpty() ||
                                                it.getPrice().compareTo(quotesList.get(0).getPrice()) < 0 ||
                                                it.getPrice().compareTo(quotesList.get(0).getPrice()) == 0 &&
                                                        it.getVolume().compareTo(quotesList.get(0).getVolume()) > 0
                                )
                                .forEach(it -> {
                                    BigDecimal result = Optional
                                            .ofNullable(dataChart.getOfferDown())
                                            .orElse(BigDecimal.ZERO)
                                            .add(it.getVolume());
                                    if (!quotesList.isEmpty() &&
                                            it.getPrice().compareTo(quotesList.get(0).getPrice()) == 0)
                                    {
                                        result = result.subtract(quotesList.get(0).getVolume());
                                    }
                                    dataChart.setOfferDown(result);
                                });
                    });
            dataChartService.getCache().setLastOfferQuotes(quotesList);
        }

        quotesSource
                .forEach((price, value) -> {
                    QuoteGroup quoteGroup = dataChart.getQuotes().get(price);
                    if (!dataChart.getQuotes().containsKey(price)) {
                        quoteGroup = new QuoteGroup();
                        dataChart.getQuotes().put(price, quoteGroup);
                    }
                    if (bidFlag) {
                        if (!isNew && quoteGroup.getBid() == null) {
                            quoteGroup.setBid(addData(null, BigDecimal.ZERO, 0d));
                        }
                        quoteGroup.setBid(addData(quoteGroup.getBid(), value, 0d));
                        if (quoteGroup.getOffer() == null) {
                            quoteGroup.setOffer(addData(null, BigDecimal.ZERO, 0d));
                        }
                    } else {
                        if (!isNew && quoteGroup.getOffer() == null) {
                            quoteGroup.setOffer(addData(null, BigDecimal.ZERO, 0d));
                        }
                        quoteGroup.setOffer(addData(quoteGroup.getOffer(), value, 0d));
                        if (quoteGroup.getBid() == null) {
                            quoteGroup.setBid(addData(null, BigDecimal.ZERO, 0d));
                        }
                    }
                });
    }

    private DataChart addOrder(AllHistoryRpt history) {
        if (Optional
                .ofNullable(dataChartService.getCache().getMaxPrice())
                .map(it -> it.compareTo(history.getPrice()) < 0)
                .orElse(true)) {
            dataChartService.getCache().setMaxPrice(history.getPrice());
        }
        if (Optional
                .ofNullable(dataChartService.getCache().getMinPrice())
                .map(it -> it.compareTo(history.getPrice()) > 0)
                .orElse(true)) {
            dataChartService.getCache().setMinPrice(history.getPrice());
        }

        DataChart dataChart = getDataChart(history);
        dataChart.setData(addData(dataChart.getData(), history.getPrice(), history.getQty()));
        if (history.getBidFlag()) {
            dataChart.setDataBid(addData(dataChart.getDataBid(), history.getPrice(), history.getQty()));
        } else {
            dataChart.setDataOffer(addData(dataChart.getDataOffer(), history.getPrice(), history.getQty()));
        }
        dataChart.setMinPrice(dataChartService.getCache().getMinPrice());
        dataChart.setMaxPrice(dataChartService.getCache().getMaxPrice());

        return dataChart;
    }

    private DataGroup addData(DataGroup data, BigDecimal value, Double volume) {
        if (data == null) {
            Candlestick candle = new Candlestick();
            candle.setClose(value);
            candle.setHigh(value);
            candle.setLow(value);
            candle.setOpen(value);

            data = new DataGroup();
            data.setVolume(volume);
            data.setCandle(candle);
        } else {
            data.setVolume(data.getVolume() + volume);
            Candlestick candle = data.getCandle();
            candle.setClose(value);
            if (candle.getHigh().compareTo(value) < 0) {
                candle.setHigh(value);
            }
            if (candle.getLow().compareTo(value) > 0) {
                candle.setLow(value);
            }
        }
        return data;
    }

    public void init() {
        dataChartService.dropCache();
        testService.init();
    }

    public void cutData(int newRange) {
        dataChartService
                .getAllCache()
                .values()
                .forEach(it -> it.cutData(newRange));
    }
    private List<DataChartDto> getDataList(Date dateBegin, Date dateEnd) {
        return dtoMapper.map(
                dataChartService.getCache().getDataList()
                        .stream()
                        .filter(it -> Objects.nonNull(it.getData()) &&
                                        Optional
                                                .ofNullable(dateBegin)
                                                .map(d -> it.getDate().compareTo(d) >= 0)
                                                .orElse(true) &&
                                        Optional
                                                .ofNullable(dateEnd)
                                                .map(d -> it.getDate().compareTo(d) <= 0)
                                                .orElse(true)

                        )
                        .sorted(Comparator.comparing(DataChart::getDate))
                , DataChartDto.class);
    }

    public List<DataChartDto> getDataList(String sDateBegin, String sDateEnd) {
        dataChartService.setCurCache(null);
        DataHolder.setFirstData(dataChartService.getCache().getFirstData());


//        setTrend(0, 5);
        setTrend(60, 5);
        setTrend(120, 5);
        setTrend(240, 5);
        setTrend(480, 5);


        return getDataList(DateFormatHolder.getDateFromString(sDateBegin), DateFormatHolder.getDateFromString(sDateEnd));
    }
}
