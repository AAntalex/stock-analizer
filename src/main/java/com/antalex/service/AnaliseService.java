package com.antalex.service;

import com.antalex.model.AnaliseResultTable;
import com.antalex.model.CorrelationValue;

import java.io.IOException;
import java.util.List;

public interface AnaliseService {
    void save(AnaliseResultTable table, String filePath) throws IOException;
    void saveCorrelations(AnaliseResultTable table, String filePath, Integer steps) throws IOException;
    List<List<CorrelationValue>> getCorrelations(AnaliseResultTable table, Integer steps);

    default void saveCorrelations(AnaliseResultTable table, String filePath) throws IOException {
        saveCorrelations(table, filePath, 1);
    }

    default List<List<CorrelationValue>> getCorrelations(AnaliseResultTable table) {
        return getCorrelations(table, 1);
    }
}
