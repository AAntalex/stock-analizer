package com.antalex.converter;

import com.antalex.dto.CandlestickDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.model.Candlestick;
import org.springframework.stereotype.Component;

@Component
public class CandlesticDtoConverter implements DtoConverter<Candlestick, CandlestickDto> {
    @Override
    public CandlestickDto convert(Candlestick entity) {
        if (entity == null) return null;
        return CandlestickDto.builder()
                .close(entity.getClose())
                .high(entity.getHigh())
                .low(entity.getLow())
                .open(entity.getOpen())
                .build();
    }
}
