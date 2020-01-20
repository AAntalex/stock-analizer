package com.antalex.dto;

import com.antalex.model.enums.EventType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DealHistoryDto {
    private BigDecimal price;
    private EventType type;
}
