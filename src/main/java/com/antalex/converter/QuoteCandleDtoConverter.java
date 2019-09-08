package com.antalex.converter;

import com.antalex.dto.QuoteCandleDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.model.Candlestick;
import org.springframework.stereotype.Component;

@Component
public class QuoteCandleDtoConverter implements DtoConverter<Candlestick, QuoteCandleDto> {
    @Override
    public QuoteCandleDto convert(Candlestick entity) {
        if (entity == null) return null;
        return QuoteCandleDto.builder()
                .close(entity.getClose().doubleValue())
                .high(entity.getHigh().doubleValue())
                .low(entity.getLow().doubleValue())
                .open(entity.getOpen().doubleValue())
                .build();
    }
}
