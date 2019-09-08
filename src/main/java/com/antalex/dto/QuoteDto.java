package com.antalex.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuoteDto {
    private QuoteCandleDto candle;
    private BigDecimal price;
}
