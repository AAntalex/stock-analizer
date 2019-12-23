package com.antalex.service;

import com.antalex.model.AnaliseResultTable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface AnaliseService {
    void save(AnaliseResultTable table, String filePath) throws IOException;
    void saveCorrelations(AnaliseResultTable table, String filePath, Integer count, Integer step) throws IOException;
    List<BigDecimal> getCorrelations(AnaliseResultTable table, Integer start, Integer count);

    default void saveCorrelations(AnaliseResultTable table, String filePath) throws IOException {
        saveCorrelations(table, filePath, table.getData().size(), 1);
    }

    default List<BigDecimal> getCorrelations(AnaliseResultTable table) {
        return getCorrelations(table, null, null);
    }
}
