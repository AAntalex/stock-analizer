package com.antalex.converter;

import com.antalex.dto.*;
import com.antalex.holders.DataHolder;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataChart;
import com.antalex.model.Indicator;
import com.antalex.model.enums.IndicatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DataChartDtoConverter implements DtoConverter<DataChart, DataChartDto> {
    private DtoMapper dtoMapper;

    @Autowired
    DataChartDtoConverter(DtoMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    @Override
    public DataChartDto convert(DataChart entity) {
        if (entity == null) return null;
        BigDecimal open = DataHolder.firstData().getData().getCandle().getOpen();

        QuoteGroupDto quoteGroupDto = QuoteGroupDto.builder()
                .bid(QuoteDto.builder()
                        .closeVolume(BigDecimal.ZERO)
                        .openVolume(BigDecimal.ZERO)
                        .build()
                )
                .offer(QuoteDto.builder()
                        .closeVolume(BigDecimal.ZERO)
                        .openVolume(BigDecimal.ZERO)
                        .build()
                )
                .build();

        return DataChartDto.builder()
                .date(entity.getDate().getTime())
                .data(dtoMapper.map(entity.getData(), DataGroupDto.class))
                .dataBid(dtoMapper.map(entity.getDataBid(), DataGroupDto.class))
                .dataOffer(dtoMapper.map(entity.getDataOffer(), DataGroupDto.class))
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .minPercent(getPercentDelta(entity.getMinPrice(), open))
                .maxPercent(getPercentDelta(entity.getMaxPrice(), open))
                .quotes(
                        dtoMapper.mapToList(entity.getQuotes(), QuoteGroupDto.class)
                                .stream()
                                .filter(it ->
                                        it.getOffer().getCandle().getOpen().compareTo(BigDecimal.ZERO) > 0 ||
                                                it.getOffer().getCandle().getClose().compareTo(BigDecimal.ZERO) > 0 ||
                                                it.getBid().getCandle().getOpen().compareTo(BigDecimal.ZERO) > 0 ||
                                                it.getBid().getCandle().getClose().compareTo(BigDecimal.ZERO) > 0
                                )
                                .sorted(Comparator.comparing(QuoteGroupDto::getPrice))
                                .map(it -> this.calcVolume(it, quoteGroupDto, false))
                                .sorted(Comparator.comparing(QuoteGroupDto::getPrice).reversed())
                                .map(it -> this.calcVolume(it, quoteGroupDto, true))
                                .collect(Collectors.toList())
                )
                .indicators(entity.getIndicators().values()
                        .stream()
                        .filter(it -> it.getType() != IndicatorType.TECHNICAL)
                        .sorted(Comparator.comparing(Indicator::getCode))
                        .collect(Collectors.toList())
                )
                .orderHistory(dtoMapper.mapToList(entity.getOrderHistory(), OrderHistoryDto.class))
                .bidUp(entity.getBidUp())
                .bidDown(entity.getBidDown())
                .offerUp(entity.getOfferUp())
                .offerDown(entity.getOfferDown())
                .build();
    }

    private QuoteGroupDto calcVolume(QuoteGroupDto quoteGroup, QuoteGroupDto prevQuoteGroup, boolean bidFlag) {
        QuoteDto quote = bidFlag ? quoteGroup.getBid() : quoteGroup.getOffer();
        QuoteDto prevQuote = bidFlag ? prevQuoteGroup.getBid() : prevQuoteGroup.getOffer();

        quote.setCloseVolume(
                prevQuote.getCloseVolume().add(
                        Optional.ofNullable(quote.getCandle())
                                .map(CandlestickDto::getClose)
                                .orElse(BigDecimal.ZERO)
                )
        );
        prevQuote.setCloseVolume(quote.getCloseVolume());

        quote.setOpenVolume(
                prevQuote.getOpenVolume().add(
                        Optional.ofNullable(quote.getCandle())
                                .map(CandlestickDto::getOpen)
                                .orElse(BigDecimal.ZERO)
                )
        );
        prevQuote.setOpenVolume(quote.getOpenVolume());

        return quoteGroup;
    }

    private BigDecimal getPercentDelta(BigDecimal value, BigDecimal valueFrom) {
        return value
                .divide(valueFrom, 4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

}
