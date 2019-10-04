package com.antalex.service;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.VolumeDto;
import com.antalex.holders.DataHolder;
import com.antalex.holders.DateFormatHolder;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.*;
import com.antalex.persistence.entity.AllTrades;
import com.antalex.persistence.entity.Quotes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChartFormer {
    private static final String START_TIME = "100000";
    private static final String END_TIME = "183959";
    private final DtoMapper dtoMapper;
    private final IndicatorService indicatorService;
    private CacheDadaChart cacheDadaChart = new CacheDadaChart();


    @Autowired
    ChartFormer(DtoMapper dtoMapper,
                IndicatorService indicatorService) {
        this.dtoMapper = dtoMapper;
        this.indicatorService = indicatorService;
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

    public void addQuotes(Quotes quote) {
        String uno = quote.getUno();
        Map<String, Quotes> quotesMap = this.cacheDadaChart.getQuotes();

        if (checkTime(uno) && !quotesMap.containsKey(uno)) {
            quotesMap.put(uno, quote);
            DataChart dataChart = getDataChart(uno);

            List<String> quotesList = Arrays.asList(quote.getQuotes().split(";"));
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
        }
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

    public void addDeal(AllTrades trade) {
        String uno = trade.getUno();
        Map<String, AllTrades> allTrades = this.cacheDadaChart.getAllTrades();

        if (Optional
                .ofNullable(this.cacheDadaChart.getMaxPrice())
                .map(it -> it.compareTo(trade.getPrice()) < 0)
                .orElse(true)) {
            this.cacheDadaChart.setMaxPrice(trade.getPrice());
        }
        if (Optional
                .ofNullable(this.cacheDadaChart.getMinPrice())
                .map(it -> it.compareTo(trade.getPrice()) > 0)
                .orElse(true)) {
            this.cacheDadaChart.setMinPrice(trade.getPrice());
        }

        if (checkTime(uno) && !allTrades.containsKey(uno)) {
            allTrades.put(uno, trade);
            DataChart dataChart = getDataChart(uno);

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

            dataChart.setData(addData(dataChart.getData(), trade.getPrice(), trade.getQty()));

            if (trade.getBidFlag()) {
                dataChart.setDataBid(addData(dataChart.getDataBid(), trade.getPrice(), trade.getQty()));
            } else {
                dataChart.setDataOffer(addData(dataChart.getDataOffer(), trade.getPrice(), trade.getQty()));
            }
            dataChart.setMinPrice(this.cacheDadaChart.getMinPrice());
            dataChart.setMaxPrice(this.cacheDadaChart.getMaxPrice());
        }
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
    }

    public List<DataChartDto> getDataList(Date dateBegin, Date dateEnd) {
        return dtoMapper.map(
                this.cacheDadaChart.getData().values()
                        .stream()
                        .filter(it -> it.getData() != null && (dateBegin == null || it.getDate().compareTo(dateBegin) >= 0)
                                && (dateEnd == null || it.getDate().compareTo(dateEnd) <= 0))
                        .sorted(Comparator.comparing(DataChart::getDate)), DataChartDto.class);
    }

    public List<DataChartDto> getDataList(String sDateBegin, String sDateEnd) {
        DataHolder.setFirstData(this.cacheDadaChart.getFirstData());
        return getDataList(DateFormatHolder.getDateFromString(sDateBegin), DateFormatHolder.getDateFromString(sDateEnd));
    }
}
