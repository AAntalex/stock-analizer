package com.antalex.model;

import com.antalex.persistence.entity.AllTrades;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataGroup {
    private Candlestick candle;
    private Double volume;
    private List<AllTrades> deals = new ArrayList<>();
}
