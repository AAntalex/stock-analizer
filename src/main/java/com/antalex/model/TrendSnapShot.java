package com.antalex.model;

import lombok.Data;

@Data
public class TrendSnapShot {
    private Integer start;
    private Trend trend;

    public TrendSnapShot() {
        this.start = 0;
    }
}
