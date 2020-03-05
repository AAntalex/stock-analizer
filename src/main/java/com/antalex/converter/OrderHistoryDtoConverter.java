package com.antalex.converter;

import com.antalex.dto.OrderHistoryDto;
import com.antalex.mapper.DtoConverter;
import com.antalex.persistence.entity.OrderHistoryRpt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OrderHistoryDtoConverter implements DtoConverter<OrderHistoryRpt, OrderHistoryDto> {
    @Override
    public OrderHistoryDto convert(OrderHistoryRpt entity) {
        return Optional.ofNullable(entity)
                .map(it ->
                        OrderHistoryDto.builder()
                                .price(it.getPrice())
                                .type(it.getType())
                                .build()
                ).orElse(null);
    }
}
