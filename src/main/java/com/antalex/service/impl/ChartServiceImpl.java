package com.antalex.service.impl;

import com.antalex.dto.DataChartDto;
import com.antalex.service.ChartFormer;
import com.antalex.service.ChartService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ChartServiceImpl implements ChartService {
    private ChartFormer chartFormer;
    private AllTradesService allTradesService;

    @Override
    public void init() {
        chartFormer.init();
    }

    @Override
    public List<DataChartDto> query(String secClass, String sDateBegin, String sDateEnd, String stockClass, int approximation) {
        chartFormer.setApproximation(approximation);
        allTradesService.query(secClass, sDateBegin, sDateEnd, stockClass).forEach(chartFormer::addDeal);
        return chartFormer.getDataList(sDateBegin, sDateEnd);
    }
}

