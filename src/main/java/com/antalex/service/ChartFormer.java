package com.antalex.service;

import com.antalex.dto.DataChartDto;
import com.antalex.holders.DataHolder;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.*;
import com.antalex.persistence.entity.AllTrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

@Component
public class ChartFormer{
    private final DtoMapper dtoMapper;
    private final IndicatorService indicatorService;

    private static final String START_TIME = "100000";
    private static final String END_TIME = "183959";

    private CacheDadaChart cacheDadaChart = new CacheDadaChart();


    @Autowired
    ChartFormer (DtoMapper dtoMapper,
                 IndicatorService indicatorService)
    {
        this.dtoMapper = dtoMapper;
        this.indicatorService = indicatorService;
    }
    public void setApproximation(int approximation) {
        this.cacheDadaChart.setApproximation(approximation);
    }

    private Date getDateFromString(String sDate) {
        if (sDate == null || sDate.isEmpty()) {
            return null;
        }
        try {
            return this.cacheDadaChart.getDateFormat().parse(sDate.substring(0, 14 - this.cacheDadaChart.getApproximation()));
        } catch (ParseException e) {
            System.out.println("Не верный формат даты " + sDate);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Не предвиденная ошибка " + sDate);
            e.printStackTrace();
        }
        return null;
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

    public void addDeal(AllTrades trade) {
        String uno = trade.getUno();
        Map<String, AllTrades> allTrades = this.cacheDadaChart.getAllTrades();
        Map<Date, DataChart> data = this.cacheDadaChart.getData();

        if (Optional
                .ofNullable(this.cacheDadaChart.getMaxPrice())
                .map(it -> it.compareTo(trade.getPrice()) < 0)
                .orElse(true))
        {
            this.cacheDadaChart.setMaxPrice(trade.getPrice());
        }
        if (Optional
                .ofNullable(this.cacheDadaChart.getMinPrice())
                .map(it -> it.compareTo(trade.getPrice()) > 0)
                .orElse(true))
        {
            this.cacheDadaChart.setMinPrice(trade.getPrice());
        }

        if (checkTime(uno) && !allTrades.containsKey(uno)) {
            allTrades.put(trade.getUno(), trade);
            Date date = getDateFromString(uno);
            DataChart dataChart = data.get(date);
            if (dataChart == null) {
                dataChart = new DataChart();
                dataChart.setDate(date);
                data.put(date, dataChart);
                if (this.cacheDadaChart.getLastData() != null) {
                    dataChart.setPrev(this.cacheDadaChart.getLastData());
                    indicatorService.calcAll(cacheDadaChart.getLastData());
                }
                this.cacheDadaChart.setLastData(dataChart);
                if (DataHolder.firstData() == null) {
                    DataHolder.setFirstData(dataChart);
                }
            }

            dataChart.setData(addData(dataChart.getData(), trade));

            if (trade.getBidFlag()) {
                dataChart.setDataBid(addData(dataChart.getDataBid(), trade));
            } else{
                dataChart.setDataOffer(addData(dataChart.getDataOffer(), trade));
            }
            dataChart.setMinPrice(this.cacheDadaChart.getMinPrice());
            dataChart.setMaxPrice(this.cacheDadaChart.getMaxPrice());
        }
    }

    private DataGroup addData(DataGroup data, AllTrades trade) {
        if (data == null) {
            Candlestick candle = new Candlestick();
            candle.setClose(trade.getPrice());
            candle.setHigh(trade.getPrice());
            candle.setLow(trade.getPrice());
            candle.setOpen(trade.getPrice());

            data = new DataGroup();
            data.setVolume(trade.getQty());
            data.setCandle(candle);
        } else {
            data.setVolume(data.getVolume() + trade.getQty());
            Candlestick candle = data.getCandle();
            candle.setClose(trade.getPrice());
            if (candle.getHigh().compareTo(trade.getPrice()) < 0) {
                candle.setHigh(trade.getPrice());
            }
            if (candle.getLow().compareTo(trade.getPrice()) > 0) {
                candle.setLow(trade.getPrice());
            }
        }
        data.getDeals().add(trade);
        return data;
    }

    public void init() {
        this.cacheDadaChart = new CacheDadaChart();
    }

    public List<DataChartDto> getDataList(Date dateBegin, Date dateEnd) {
        return dtoMapper.map(
                this.cacheDadaChart.getData().values()
                        .stream()
                        .filter(it -> (dateBegin == null || it.getDate().compareTo(dateBegin) >= 0)
                                && (dateEnd == null || it.getDate().compareTo(dateEnd) <= 0))
                        .sorted(Comparator.comparing(DataChart::getDate)), DataChartDto.class);
    }

    public List<DataChartDto> getDataList(String sDateBegin, String sDateEnd) {
        return getDataList(getDateFromString(sDateBegin), getDateFromString(sDateEnd));
    }
}
