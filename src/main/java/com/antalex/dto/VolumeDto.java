package com.antalex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class VolumeDto {
    private BigDecimal price;
    private BigDecimal volume;
}
