package com.antalex.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AnaliseResultTable {
    private List<String> headers;
    private List<AnaliseResultRow> data = new ArrayList<>();
}
