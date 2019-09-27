package com.antalex.converter;

import com.antalex.dto.CandlestickDto;
import com.antalex.dto.QuoteDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class QuoteDtoConverter implements DtoConverter<DataGroup, QuoteDto> {
    private DtoMapper dtoMapper;

    @Autowired
    QuoteDtoConverter(DtoMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    @Override
    public QuoteDto convert(DataGroup entity) {
        if (entity == null) return null;
        return QuoteDto.builder()
                .candle(dtoMapper.map(entity.getCandle(), CandlestickDto.class))
                .closeVolume(BigDecimal.ZERO)
                .openVolume(BigDecimal.ZERO)
                .build();
    }
}
