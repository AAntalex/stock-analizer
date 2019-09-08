package com.antalex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuoteCandleDto {
    private Double open;
    private Double high;
    private Double low;
    private Double close;
}
