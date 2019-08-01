package com.antalex.converter;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.DataGroupDto;
import com.antalex.dto.IndicatorDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        return DataChartDto.builder()
                .date(entity.getDate().getTime())
                .data(dtoMapper.map(entity.getData(), DataGroupDto.class))
                .dataBid(dtoMapper.map(entity.getDataBid(), DataGroupDto.class))
                .dataOffer(dtoMapper.map(entity.getDataOffer(), DataGroupDto.class))
                .minPrice(entity.getMinPrice())
                .maxPrice(entity.getMaxPrice())
                .indicators(
                        dtoMapper.mapToList(
                                entity.getIndicators()
                                        .entrySet()
                                        .stream()
                                        .filter(it -> it.getValue().getIsPublic() ),
                                IndicatorDto.class
                        ))
                .build();
    }
}
