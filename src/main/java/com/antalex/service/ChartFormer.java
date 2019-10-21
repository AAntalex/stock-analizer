package com.antalex.service;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.VolumeDto;
import com.antalex.holders.DataHolder;
import com.antalex.holders.DateFormatHolder;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.*;
import com.antalex.persistence.entity.AllHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ChartFormer {
    private static final String START_TIME = "100000";
    private static final String END_TIME = "183959";
    private final DtoMapper dtoMapper;
    private final IndicatorService indicatorService;
    private final TrendService trendService;
    private CacheDadaChart cacheDadaChart = new CacheDadaChart();


    private String buyOrder;
    private BigDecimal buyPrice;
    private String sellOrder;
    private BigDecimal sellPrice;

    private BigDecimal pBid;
    private BigDecimal pOffer;
    private BigDecimal rsiBuy;
    private BigDecimal rsiSell;
    private BigDecimal rsiBuyPrev;
    private BigDecimal rsiSellPrev;
    private BigDecimal result = BigDecimal.ONE;


    @Autowired
    ChartFormer(DtoMapper dtoMapper,
                IndicatorService indicatorService,
                TrendService trendService) {
        this.dtoMapper = dtoMapper;
        this.indicatorService = indicatorService;
        this.trendService = trendService;
    }

    public void setApproximation(int approximation) {
        DateFormatHolder.setApproximation(approximation);
    }

    private Boolean checkTime(String uno) {
        try {
            String curTime = uno.substring(8, 14);
            return curTime.compareTo(START_TIME) >= 0 && curTime.compareTo(END_TIME) <= 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private DataChart getDataChart(String uno) {
        Map<Date, DataChart> data = this.cacheDadaChart.getData();
        Date date = DateFormatHolder.getDateFromString(uno);
        DataChart dataChart = data.get(date);
        if (dataChart == null) {
            dataChart = new DataChart();
            dataChart.setDate(date);
            data.put(date, dataChart);
        }
        return dataChart;
    }

    private Trend getTrend() {
        return getTrend(0);
    }

    private Trend getTrend(int period) {
        List<DataChart> dataList = this.getDataList();
        Trend trend = trendService.getTrend(dataList, period);
        IntStream.range(period > 0 ? dataList.size() - period : 0, dataList.size())
                .forEach(
                        idx -> {
                            HashMap<String, Indicator> indicators = dataList.get(idx).getIndicators();

                            String code = period > 0 ? "TREND" + period + "_HIGH" : "TREND_HIGH";
                            indicators.put(
                                    code,
                                    Indicator.builder()
                                            .period(period)
                                            .value(trend.getHigh().f(idx))
                                            .code(code)
                                            .name("TREND")
                                            .type(IndicatorType.TREND)
                                            .build()
                            );
                            code = period > 0 ? "TREND" + period + "_low" : "TREND_low";
                            indicators.put(
                                    code,
                                    Indicator.builder()
                                            .period(period)
                                            .value(trend.getLow().f(idx))
                                            .code(code)
                                            .name("TREND")
                                            .type(IndicatorType.TREND)
                                            .build()
                            );
                        });
        return trend;
    }

    private void addPoint(DataChart dataChart){
        if (Optional.ofNullable(this.cacheDadaChart.getLastData())
                .map(DataChart::getDate)
                .map(it -> it.compareTo(dataChart.getDate()) != 0)
                .orElse(true))
        {
            if (this.cacheDadaChart.getLastData() != null) {
                dataChart.setPrev(this.cacheDadaChart.getLastData());
                indicatorService.calcAll(cacheDadaChart.getLastData());
            }
            this.cacheDadaChart.setLastData(dataChart);
        }
    }

    public void add(AllHistory history) {
        String uno = history.getUno();
        Map<String, AllHistory> allHistory = this.cacheDadaChart.getAllHistory();
        if (checkTime(uno) && !allHistory.containsKey(uno)) {
            allHistory.put(uno, history);
            if (history.getQuotes() != null) {
                addQuotes(history);
            }
            if (history.getTradeNum() != null) {
                addPoint(addDeal(history));
            }


//            test(history, dataChart);












        }
    }





    private void test(AllHistory history, DataChart dataChart) {
        String uno = history.getUno();

        if (this.cacheDadaChart.getData().size() < 20) {
            return;
        }

        dataChart = this.cacheDadaChart.getLastData();
        HashMap<String, Indicator> indicators = dataChart.getPrev().getIndicators();
        HashMap<String, Indicator> prevIndicators = dataChart.getPrev().getPrev().getIndicators();
/*

            if (indicators.containsKey("RSI14") &&
                    indicators.get("RSI14").getValue().compareTo(BigDecimal.valueOf(70)) > 0 &&
                    buyOrder == null &&
                    dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(BigDecimal.ZERO) > 0 &&
                    dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(BigDecimal.ZERO) <= 0)
            {
                System.out.println("AAA "
                        + dataChart.getOfferDown().subtract(dataChart.getOfferUp())
                        + " "
                        + dataChart.getBidDown().subtract(dataChart.getBidUp())
                        + " "
                );
            }

*/

        if (buyOrder == null &&
/*
                    dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(BigDecimal.ZERO) > 0 &&
                    dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(BigDecimal.ZERO) <= 0 &&
*/
                indicators.get("P_OFFER").getValue().compareTo(BigDecimal.ZERO) > 0 &&
                dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(BigDecimal.ZERO) > 0 &&
                dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(indicators.get("P_OFFER").getValue()) > 0 &&
                dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(indicators.get("P_BID").getValue()) > 0 &&
                dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(dataChart.getBidDown().subtract(dataChart.getBidUp())) > 0 &&


/*
                    indicators.get("P_OFFER").getValue().compareTo(BigDecimal.ZERO) > 0 &&
                    indicators.get("P_BID").getValue().compareTo(BigDecimal.ZERO) == 0 &&
*/


                indicators.containsKey("RSI14") &&
                indicators.get("RSI14").getValue().compareTo(BigDecimal.valueOf(70)) > 0 &&
                prevIndicators.containsKey("RSI14") &&
                indicators.get("RSI14").getValue().compareTo(prevIndicators.get("RSI14").getValue()) > 0
                )
        {
            buyOrder = uno;

            pOffer = dataChart.getOfferDown().subtract(dataChart.getOfferUp());
            rsiBuy = indicators.get("RSI14").getValue();
            rsiBuyPrev = prevIndicators.get("RSI14").getValue();

            System.out.println("AAA !!!!!!!!!!!!!! pOffer " + pOffer + " rsiBuy " + rsiBuy + " rsiBuyPrev = " + rsiBuyPrev + " uno = " + uno);
        }

        if (sellOrder == null && buyPrice != null &&
                (
/*
                            dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(dataChart.getOfferDown().subtract(dataChart.getOfferUp())) > 0
*/
                        indicators.get("P_BID").getValue().compareTo(indicators.get("P_OFFER").getValue()) > 0
                                || indicators.get("RSI14").getValue().compareTo(BigDecimal.valueOf(70)) < 0
                )
                )
        {
            sellOrder = uno;

            pBid = indicators.get("P_BID").getValue();
            rsiBuy = indicators.get("RSI14").getValue();

            System.out.println("AAA sellOrder " + sellOrder + " pBid " + pBid + " rsiBuy " + rsiBuy);
        }

        if (buyOrder != null && buyPrice == null &&
                new BigDecimal(uno.substring(0, 14))
                        .subtract(new BigDecimal(buyOrder.substring(0, 14)))
                        .compareTo(BigDecimal.ZERO) > 0)
        {
            buyPrice = dataChart.getQuotes()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .filter(it -> it.getValue()
                            .getOffer()
                            .getCandle()
                            .getClose()
                            .compareTo(BigDecimal.ZERO) > 0)
                    .findFirst()

                    .map(it -> it.getKey())

/*
                        .map(it ->
                                it.getKey().max(
                                        Optional
                                                .ofNullable(history.getPrice())
                                                .orElse(BigDecimal.ZERO)
                                )
                        )
*/
                    .orElse(null);

            System.out.println("AAA buyPrice " + buyPrice + " uno = " + uno);
        }






        if (sellOrder == null &&
/*
                    dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(BigDecimal.ZERO) > 0 &&
                    dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(BigDecimal.ZERO) <= 0 &&
*/

                indicators.get("P_BID").getValue().compareTo(BigDecimal.ZERO) > 0 &&
                dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(BigDecimal.ZERO) > 0 &&
                dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(indicators.get("P_BID").getValue()) > 0 &&
                dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(indicators.get("P_OFFER").getValue()) > 0 &&
                dataChart.getBidDown().subtract(dataChart.getBidUp()).compareTo(dataChart.getOfferDown().subtract(dataChart.getOfferUp())) > 0 &&

/*
                    indicators.get("P_OFFER").getValue().compareTo(BigDecimal.ZERO) == 0 &&
                    indicators.get("P_BID").getValue().compareTo(BigDecimal.ZERO) > 0 &&
*/

                indicators.containsKey("RSI14") &&
                indicators.get("RSI14").getValue().compareTo(BigDecimal.valueOf(30)) < 0 &&
                prevIndicators.containsKey("RSI14") &&
                indicators.get("RSI14").getValue().compareTo(prevIndicators.get("RSI14").getValue()) < 0)
        {
            sellOrder = uno;


            pBid = dataChart.getBidDown().subtract(dataChart.getBidUp());
            rsiSell = indicators.get("RSI14").getValue();
            rsiSellPrev = prevIndicators.get("RSI14").getValue();

            System.out.println("AAA !!!!!!!!!!!!!! pBid " + pBid + " rsiSell " + rsiSell + " rsiSellPrev = " + rsiSellPrev + " uno = " + uno);

        }

        if (buyOrder == null && sellPrice != null &&
                (
/*
                            dataChart.getOfferDown().subtract(dataChart.getOfferUp()).compareTo(dataChart.getBidDown().subtract(dataChart.getBidUp())) > 0
*/
                        indicators.get("P_OFFER").getValue().compareTo(indicators.get("P_BID").getValue()) > 0
                                || indicators.get("RSI14").getValue().compareTo(BigDecimal.valueOf(30)) > 0
                )
                )
        {
            buyOrder = uno;

            pOffer = indicators.get("P_OFFER").getValue();
            rsiSell = indicators.get("RSI14").getValue();
            System.out.println("AAA buyOrder " + buyOrder + " pOffer " + pOffer + " rsiSell " + rsiSell);
        }

        if (sellOrder != null && sellPrice == null &&
                new BigDecimal(uno.substring(0, 14))
                        .subtract(new BigDecimal(sellOrder.substring(0, 14)))
                        .compareTo(BigDecimal.ZERO) > 0)
        {
            sellPrice = dataChart.getQuotes()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                    .filter(it -> it.getValue()
                            .getBid()
                            .getCandle()
                            .getClose()
                            .compareTo(BigDecimal.ZERO) > 0
                    )
                    .findFirst()

                    .map(it -> it.getKey())

                    /*                     .map(it ->
                                                 it.getKey().min(
                                                         Optional
                                                                 .ofNullable(history.getPrice())
                                                                 .orElse(it.getKey())
                                                 )
                                         )
                    */
                    .orElse(null);

            System.out.println("AAA sellPrice " + sellPrice + " uno = " + uno);
        }





        if (sellPrice != null && buyPrice != null) {
            System.out.println("AAA sellPrice " + sellPrice + " buyPrice " + buyPrice);

            BigDecimal result = (sellPrice
                    .divide(buyPrice, 4, RoundingMode.HALF_UP)
                    .subtract(BigDecimal.ONE))
                    .multiply(BigDecimal.valueOf(100));

            this.result = this.result.multiply(
                    sellPrice
                            .divide(buyPrice, 4, RoundingMode.HALF_UP)
            );

            System.out.println("AAA result " + result + " globalResult = " + this.result);

            sellPrice = null;
            buyPrice = null;
            buyOrder = null;
            sellOrder = null;
        }
    }

    private DataChart addQuotes(AllHistory history) {
        DataChart dataChart = getDataChart(history.getUno());

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
        addDataQuotes(dataChart, currentQuotesBid, true);
        addDataQuotes(dataChart, currentQuotesOffer, false);

        return dataChart;
    }

    private void addDataQuotes(DataChart dataChart, HashMap<BigDecimal, BigDecimal> quotesSource, boolean bidFlag) {
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

            Optional.ofNullable(this.cacheDadaChart.getLastBidQuotes())
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

             this.cacheDadaChart.setLastBidQuotes(quotesList);
        } else {
            quotesList = quotesSource.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(it -> new VolumeDto(it.getKey(), it.getValue()))
                    .collect(Collectors.toList());


            Optional.ofNullable(this.cacheDadaChart.getLastOfferQuotes())
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
            this.cacheDadaChart.setLastOfferQuotes(quotesList);
        }

        quotesSource
                .forEach((price, value) -> {
                    QuoteGroup quoteGroup = dataChart.getQuotes().get(price);
                    if (!dataChart.getQuotes().containsKey(price)) {
                        quoteGroup = new QuoteGroup();
                        dataChart.getQuotes().put(price, quoteGroup);
                    }
                    if (bidFlag) {
                        quoteGroup.setBid(addData(quoteGroup.getBid(), value, 0d));
                        if (quoteGroup.getOffer() == null) {
                            quoteGroup.setOffer(addData(null, BigDecimal.ZERO, 0d));
                        }
                    } else {
                        quoteGroup.setOffer(addData(quoteGroup.getOffer(), value, 0d));
                        if (quoteGroup.getBid() == null) {
                            quoteGroup.setBid(addData(null, BigDecimal.ZERO, 0d));
                        }
                    }
                });
    }

    private DataChart addDeal(AllHistory history) {
        if (Optional
                .ofNullable(this.cacheDadaChart.getMaxPrice())
                .map(it -> it.compareTo(history.getPrice()) < 0)
                .orElse(true)) {
            this.cacheDadaChart.setMaxPrice(history.getPrice());
        }
        if (Optional
                .ofNullable(this.cacheDadaChart.getMinPrice())
                .map(it -> it.compareTo(history.getPrice()) > 0)
                .orElse(true)) {
            this.cacheDadaChart.setMinPrice(history.getPrice());
        }

        DataChart dataChart = getDataChart(history.getUno());
        dataChart.setData(addData(dataChart.getData(), history.getPrice(), history.getQty()));
        if (history.getBidFlag()) {
            dataChart.setDataBid(addData(dataChart.getDataBid(), history.getPrice(), history.getQty()));
        } else {
            dataChart.setDataOffer(addData(dataChart.getDataOffer(), history.getPrice(), history.getQty()));
        }
        dataChart.setMinPrice(this.cacheDadaChart.getMinPrice());
        dataChart.setMaxPrice(this.cacheDadaChart.getMaxPrice());

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
        this.cacheDadaChart = new CacheDadaChart();

        result = BigDecimal.ONE;
    }

    private List<DataChartDto> getDataList(Date dateBegin, Date dateEnd) {
        return dtoMapper.map(
                this.cacheDadaChart.getData().values()
                        .stream()
                        .filter(it -> it.getData() != null && (dateBegin == null || it.getDate().compareTo(dateBegin) >= 0)
                                && (dateEnd == null || it.getDate().compareTo(dateEnd) <= 0))
                        .sorted(Comparator.comparing(DataChart::getDate)), DataChartDto.class);
    }

    private List<DataChart> getDataList() {
        return this.cacheDadaChart.getData().values()
                .stream()
                .filter(it -> it.getData() != null)
                .sorted(Comparator.comparing(DataChart::getDate))
                .collect(Collectors.toList());
    }

    public List<DataChartDto> getDataList(String sDateBegin, String sDateEnd) {
        DataHolder.setFirstData(this.cacheDadaChart.getFirstData());



        getTrend();
        getTrend(60);
        getTrend(100);



        return getDataList(DateFormatHolder.getDateFromString(sDateBegin), DateFormatHolder.getDateFromString(sDateEnd));
    }
}
