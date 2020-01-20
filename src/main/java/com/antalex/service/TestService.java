package com.antalex.service;

import com.antalex.model.DataChart;

import java.io.IOException;

public interface TestService {
    void test(DataChart data);
    void calcCorr(DataChart data);
    void saveResult() throws IOException;
    void init();
}
