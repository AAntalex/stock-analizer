package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.AnaliseResultRow;
import com.antalex.model.AnaliseResultTable;
import com.antalex.service.AnaliseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class AnaliseServiceImpl implements AnaliseService {
    @Override
    public void save(AnaliseResultTable table, String filePath) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath, false);
        fileWriter.write(getHeaderString(table));
        table.getData()
                .forEach(
                        row -> {
                            try {
                                fileWriter.write(
                                        row.getUno()
                                                .concat(";")
                                                .concat(
                                                        row.getFactors().stream().
                                                                map(res -> res + ";")
                                                                .reduce("", String::concat)
                                                )
                                                .concat(row.getResult().toString())
                                                .concat("\n")
                                );
                            } catch (IOException e) {
                                log.error(e.getMessage());
                            }
                        }
                );
        fileWriter.close();
    }

    @Override
    public void saveCorrelations(AnaliseResultTable table, String filePath, Integer count, Integer step) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath, false);
        fileWriter.write(getHeaderString(table));
        if (table.getData().size() > 0) {
            for(int start = 0; count <= table.getData().size(); start+=step, count+=step) {
                fileWriter.write(
                        table.getData().get(start).getUno()
                                .concat(";")
                                .concat(
                                        getCorrelations(table, start, count)
                                                .stream()
                                                .map(it -> it + ";")
                                                .reduce("", String::concat)
                                ).concat("1\n")
                );
            }
        }
        fileWriter.close();
    }

    @Override
    public List<BigDecimal> getCorrelations(AnaliseResultTable table, Integer start, Integer count) {
        Integer size = table.getData().size();
        Integer begin = Integer.min(Optional.ofNullable(start).orElse(0), size);
        Integer end = Optional.ofNullable(count).map(it -> it + begin).orElse(size);
        if (end > size) {
            return Collections.emptyList();
        }
        List<AnaliseResultRow> data = IntStream
                .range(begin, end)
                .mapToObj(idx -> table.getData().get(idx))
                .collect(Collectors.toList());

        List<BigDecimal> resultSeries =
                data
                        .stream()
                        .map(AnaliseResultRow::getResult)
                        .collect(Collectors.toList());

        return IntStream
                .range(0, table.getHeaders().size())
                .mapToObj(idx ->
                        getCorrelation(
                                data
                                        .stream()
                                        .map(AnaliseResultRow::getFactors)
                                        .map(it -> it.get(idx))
                                        .collect(Collectors.toList()),
                                resultSeries
                        )
                ).collect(Collectors.toList());
    }

    private BigDecimal getCorrelation(List<BigDecimal> x, List<BigDecimal> y) {
        BigDecimal mX = getExpectedValue(x);
        BigDecimal mY = getExpectedValue(y);
        return getCovariance(x, y, mX, mY)
                .divide(
                        getStandardDeviation(x, mX)
                                .multiply(getStandardDeviation(y, mY)),
                        DataHolder.PRECISION,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal getCovariance(
            List<BigDecimal> x,
            List<BigDecimal> y,
            BigDecimal mX,
            BigDecimal mY
    ) {
        return IntStream
                .range(0, x.size())
                .mapToObj(idx -> x.get(idx).subtract(mX).multiply(y.get(idx).subtract(mY)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getStandardDeviation(List<BigDecimal> numberSeries, BigDecimal m) {
        return new BigDecimal(
                Math.sqrt(
                        numberSeries
                                .stream()
                                .map(m::subtract)
                                .map(it -> it.pow(2))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .doubleValue()
                )
        );
    }

    private BigDecimal getExpectedValue(List<BigDecimal> numberSeries) {
        return numberSeries
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(numberSeries.size()), DataHolder.PRECISION, RoundingMode.HALF_UP);
    }

    private String getHeaderString(AnaliseResultTable table) {
        return "UNO;"
                .concat(
                        table.getHeaders()
                                .stream()
                                .map(it -> it.concat(";"))
                                .reduce(String::concat)
                                .orElse("")
                )
                .concat("RESULT\n");
    }
}

