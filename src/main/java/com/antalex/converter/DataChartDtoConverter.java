package com.antalex.converter;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.DataGroupDto;
import com.antalex.holders.DataHolder;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataChart;
import com.antalex.model.IndicatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        return DataChartDto.builder()
                .date(entity.getDate().getTime())
                .data(dtoMapper.map(entity.getData(), DataGroupDto.class))
                .dataBid(dtoMapper.map(entity.getDataBid(), DataGroupDto.class))
                .dataOffer(dtoMapper.map(entity.getDataOffer(), DataGroupDto.class))
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .minPercent(getPercentDelta(entity.getMinPrice(), open))
                .maxPercent(getPercentDelta(entity.getMaxPrice(), open))
                .indicators(entity.getIndicators().values()
                        .stream()
                        .filter(it -> it.getType() != IndicatorType.TECHNICAL)
                        .collect(Collectors.toList())
                )
                .build();
    }

    private BigDecimal getPercentDelta(BigDecimal value, BigDecimal valueFrom) {
        return value
                .divide(valueFrom, 4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
    }

}
