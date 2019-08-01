package com.antalex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TradeClassesDto {
    private String group;
    private List<String> names;
}
