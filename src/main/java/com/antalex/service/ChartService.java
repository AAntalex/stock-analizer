package com.antalex.service;

import com.antalex.dto.DataChartDto;

import java.util.List;

public interface ChartService {
    void init();
    void getData(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation);
    List<DataChartDto> query(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation);
}
