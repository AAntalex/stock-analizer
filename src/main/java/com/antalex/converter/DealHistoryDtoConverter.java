package com.antalex.converter;

import com.antalex.dto.DealHistoryDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.persistence.entity.DealHistoryRpt;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class DealHistoryDtoConverter implements DtoConverter<DealHistoryRpt, DealHistoryDto> {
    @Override
    public DealHistoryDto convert(DealHistoryRpt entity) {
        return Optional.ofNullable(entity)
                .map(it ->
                        DealHistoryDto.builder()
                                .price(it.getPrice())
                                .type(it.getType())
                                .build()
                ).orElse(null);
    }
}
