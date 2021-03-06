package com.antalex.controllers;

import com.antalex.dto.DataChartDto;
import com.antalex.dto.TradeClassesDto;
import com.antalex.mapper.DtoMapper;
import com.antalex.persistence.entity.Quotes;
import com.antalex.persistence.repository.TradeClassesRepository;
import com.antalex.service.ChartService;
import com.antalex.persistence.entity.AllTrades;
import com.antalex.service.impl.AllTradesService;
import com.antalex.service.impl.QuotesService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class StockController {
    private AllTradesService allTradesService;
    private QuotesService quotesService;
    private ChartService chartService;
    private TradeClassesRepository tradeClassesRepository;
    private DtoMapper dtoMapper;

    @RequestMapping(method = RequestMethod.GET, path = "/quotes")
    public List<Quotes> getQuotes (
            @RequestParam(value = "secClass") String secClass
            ,   @RequestParam(value = "sDateBegin") String sDateBegin
            ,   @RequestParam(value = "sDateEnd") String sDateEnd
            ,   @RequestParam(value = "stockClass", defaultValue = "TQBR") String stockClass
    ) {
        return quotesService.query(secClass, sDateBegin, sDateEnd, stockClass);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/allTrades")
    public List<AllTrades> getAllTrades (
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
        return dtoMapper.map(tradeClassesRepository.findAll(), TradeClassesDto.class);
    }
}
