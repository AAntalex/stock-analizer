package com.antalex.converter;

import com.antalex.dto.IndicatorDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.model.Indicator;
import org.springframework.stereotype.Component;

@Component
public class IndicatorDtoConverter implements DtoConverter<Indicator, IndicatorDto> {
    @Override
    public IndicatorDto convert(Indicator entity) {
        return convert(entity, "");
    }

    @Override
    public <K> IndicatorDto convert(Indicator entity, K key) {
        if (entity == null) return null;
        return IndicatorDto.builder()
                .code((String) key)
                .name(entity.getName())
                .value(entity.getValue())
                .build();
    }
}
