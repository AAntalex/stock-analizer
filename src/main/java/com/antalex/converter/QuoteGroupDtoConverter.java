package com.antalex.converter;

import com.antalex.dto.QuoteDto;
import com.antalex.dto.QuoteGroupDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.mapper.DtoMapper;
import com.antalex.model.DataGroup;
import com.antalex.model.QuoteGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class QuoteGroupDtoConverter implements DtoConverter<QuoteGroup, QuoteGroupDto> {
    private DtoMapper dtoMapper;

    @Autowired
    QuoteGroupDtoConverter(DtoMapper dtoMapper) {
        this.dtoMapper = dtoMapper;
    }

    @Override
    public QuoteGroupDto convert(QuoteGroup entity) {
        if (entity == null) return null;
        return QuoteGroupDto.builder()
                .bid(dtoMapper.map(
                        Optional.ofNullable(entity.getBid())
                                .orElse(new DataGroup()),
                        QuoteDto.class))
                .offer(dtoMapper.map(
                        Optional.ofNullable(entity.getOffer())
                                .orElse(new DataGroup()),
                        QuoteDto.class))
                .build();
    }

    @Override
    public QuoteGroupDto convert(QuoteGroup entity, Object price) {
        if (entity == null) return null;
        QuoteGroupDto dto = convert(entity);
        dto.setPrice((BigDecimal) price);
        return dto;
    }
}
