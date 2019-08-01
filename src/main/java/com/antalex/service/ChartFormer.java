package com.antalex.service;

import com.antalex.dto.DataChartDto;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.*;
import com.antalex.persistence.entity.AllTrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
            }
            dataChart.setData(addData(dataChart.getData(), trade));


            if (trade.getBidFlag()) {
                dataChart.setDataBid(addData(dataChart.getDataBid(), trade));
            } else{
                dataChart.setDataOffer(addData(dataChart.getDataOffer(), trade));
            }
            dataChart.setMinPrice(this.cacheDadaChart.getMinPrice());
            dataChart.setMaxPrice(this.cacheDadaChart.getMaxPrice());

            this.cacheDadaChart.setLastData(dataChart);

            calcIndicators(dataChart);
        }
    }

    private void calcIndicators(DataChart dataChart) {
        BigDecimal allBidVolume = new BigDecimal(0);
        BigDecimal allOfferVolume = new BigDecimal(0);
        BigDecimal allVolume = new BigDecimal(0);
        DataChart lastData = this.cacheDadaChart.getLastData();
        if (!lastData.getIndicators().isEmpty()) {
            allBidVolume = lastData.getIndicators().get("allBidVolume").getValue();
            allOfferVolume = lastData.getIndicators().get("allOfferVolume").getValue();
            allVolume = lastData.getIndicators().get("allVolume").getValue();
        }
        allVolume = allVolume.add(BigDecimal.valueOf(dataChart.getData().getVolume()));
        if (dataChart.getDataBid() != null) {
            allBidVolume = allBidVolume.add(BigDecimal.valueOf(dataChart.getDataBid().getVolume()));
        }
        if (dataChart.getDataOffer() != null) {
            allOfferVolume = allOfferVolume.add(BigDecimal.valueOf(dataChart.getDataOffer().getVolume()));
        }


        dataChart.getIndicators().put("allVolume", Indicator.builder()
                .value(allVolume)
                .isPublic(false)
                .name("Объем сделок")
                .build());
        dataChart.getIndicators().put("allBidVolume", Indicator.builder()
                .value(allBidVolume)
                .isPublic(false)
                .name("Объем спроса")
                .build());
        dataChart.getIndicators().put("allOfferVolume", Indicator.builder()
                .value(allOfferVolume)
                .isPublic(false)
                .name("Объем предложения")
                .build());
        dataChart.getIndicators().put("OVB", Indicator.builder()
                .value(allBidVolume.divide(allVolume, 5, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)))
                .isPublic(true)
                .name("Индекс перекупленности")
                .build());



        BigDecimal ind = indicatorService.calc(cacheDadaChart.getLastData(), "ALLVOL", 100);
        System.out.println("AAA " + ind);
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
