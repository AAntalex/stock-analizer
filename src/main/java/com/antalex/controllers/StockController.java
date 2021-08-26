package com.antalex.controllers;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.TradeClassesDto;
import com.antalex.mapper.DtoMapper;
import com.antalex.persistence.entity.QuotesRpt;
import com.antalex.service.ChartService;
import com.antalex.persistence.entity.AllTradesRpt;
import com.antalex.service.TradeClassesService;
import com.antalex.service.impl.AllTradesService;
import com.antalex.service.impl.QuotesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping(path = "api/v1/stock")
public class StockController {
    private AllTradesService allTradesService;
    private QuotesService quotesService;
    private ChartService chartService;
    private TradeClassesService tradeClassesService;
    private DtoMapper dtoMapper;

    @RequestMapping(method = RequestMethod.GET, path = "/quotes")
    public List<QuotesRpt> getQuotes (
            @RequestParam(value = "secClass") String secClass
            ,   @RequestParam(value = "sDateBegin") String sDateBegin
            ,   @RequestParam(value = "sDateEnd") String sDateEnd
            ,   @RequestParam(value = "stockClass", defaultValue = "TQBR") String stockClass
    ) {
        return quotesService.query(secClass, sDateBegin, sDateEnd, stockClass);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/allTrades")
    public List<AllTradesRpt> getAllTrades (
                @RequestParam(value = "secClass") String secClass
            ,   @RequestParam(value = "sDateBegin") String sDateBegin
            ,   @RequestParam(value = "sDateEnd") String sDateEnd
            ,   @RequestParam(value = "stockClass", defaultValue = "TQBR") String stockClass
    ) {
        return allTradesService.query(secClass, sDateBegin, sDateEnd, stockClass);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/charts")
    public List<DataChartDto> getDataChart (
            @RequestParam(value = "secClass") String secClass
            ,   @RequestParam(value = "sDateBegin") String sDateBegin
            ,   @RequestParam(value = "sDateEnd") String sDateEnd
            ,   @RequestParam(value = "stockClass", defaultValue = "TQBR") String stockClass
            ,   @RequestParam(value = "approximation", defaultValue = "0") int interval
    ) {
        return chartService.query(secClass, sDateBegin, sDateEnd, stockClass, interval);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/init")
    public void init () {
        chartService.init();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/classes")
    public List<TradeClassesDto> getClasses () {
        return dtoMapper.map(tradeClassesService.findAll(), TradeClassesDto.class);
    }
}
