package com.antalex.converter;

import com.antalex.dto.TradeClassesDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.persistence.entity.TradeClassesEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TradeClassesDtoConverter implements DtoConverter<TradeClassesEntity, TradeClassesDto> {
    @Override
    public TradeClassesDto convert(TradeClassesEntity entity) {
        if (entity == null) return null;
        return new TradeClassesDto(entity.getCode(),
                entity.getClassSecList()
                        .stream()
                        .map(it -> String.format("%s (%s)", it.getShortName(), it.getCode()))
                        .collect(Collectors.toList())
        );
    }
}
