package com.antalex.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuoteGroupDto {
    private QuoteDto bid;
    private QuoteDto offer;
    private BigDecimal price;
}
