package com.antalex.model;

import lombok.Data;

@Data
public class QuoteGroup {
    private DataGroup bid;
    private DataGroup offer;
}
