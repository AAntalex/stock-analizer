package com.antalex.controllers;

import com.antalex.dto.DataChartDto;
import com.antalex.holders.DataChartHolder;
import com.antalex.service.ChartService;
import com.antalex.service.TestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/v1/test")
@Slf4j
public class TestController {
    private ChartService chartService;
    private TestService testService;

    @RequestMapping(method = RequestMethod.GET)
    public List<DataChartDto> getTestData (
            @RequestParam(value = "secClass") String secClass
            ,   @RequestParam(value = "sDateBegin") String sDateBegin
            ,   @RequestParam(value = "sDateEnd") String sDateEnd
            ,   @RequestParam(value = "stockClass", defaultValue = "TQBR") String stockClass
            ,   @RequestParam(value = "approximation", defaultValue = "0") int interval
    ) {
        DataChartHolder.setTest(true);
        chartService.init();
        List<DataChartDto>  result = chartService.query(secClass, sDateBegin, sDateEnd, stockClass, interval);
        log.info("AAA TEST is Done");
        return result;
/*

        DataChartHolder.setCalcCorr(true);
        chartService.init();
        List<DataChartDto>  result = chartService.query(secClass, sDateBegin, sDateEnd, stockClass, interval);

        try {
            testService.saveResult();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        
        return result;
*/
    }
}
