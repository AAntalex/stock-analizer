package com.antalex.converter;

import com.antalex.dto.CandlestickDto;
import com.antalex.dto.DataGroupDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataGroupDtoConverter implements DtoConverter<DataGroup, DataGroupDto> {
    private DtoMapper dtoMapper;

    @Autowired
    DataGroupDtoConverter(DtoMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    @Override
    public DataGroupDto convert(DataGroup entity) {
        if (entity == null) return null;
        return DataGroupDto.builder()
                .candle(dtoMapper.map(entity.getCandle(), CandlestickDto.class))
                .volume(entity.getVolume())
                .build();
    }
}
