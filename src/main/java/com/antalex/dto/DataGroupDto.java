package com.antalex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataGroupDto {
    private CandlestickDto candle;
    private Double volume;
}
