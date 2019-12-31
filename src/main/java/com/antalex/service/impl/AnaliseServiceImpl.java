package com.antalex.service.impl;

import com.antalex.holders.DataHolder;
import com.antalex.model.AnaliseResultRow;
import com.antalex.model.AnaliseResultTable;
import com.antalex.model.CorrelationValue;
import com.antalex.service.AnaliseService;
import javafx.util.Pair;
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
                .stream()
                .sorted(Comparator.comparing(AnaliseResultRow::getUno))
                .forEach(
                        row -> {
                            try {
                                fileWriter.write(
                                        row.getUno()
                                                .concat(";")
                                                .concat(
                                                        row.getFactors()
                                                                .stream()
                                                                .map(res -> res + ";")
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
    public void saveCorrelations(AnaliseResultTable table, String filePath, Integer steps) throws IOException {
        FileWriter fileWriter = new FileWriter(filePath, false);
        fileWriter.write(table.getHeaders()
                .stream()
                .map(it -> it.concat(";MIN;MAX;RESULT;SIZE;"))
                .reduce(String::concat)
                .orElse("")
                .concat("\n")
        );

        if (table.getData().size() > 0) {
            getCorrelations(table, steps)
                    .forEach(
                            row -> {
                                try {
                                    fileWriter.write(
                                            row
                                                    .stream()
                                                    .map(
                                                            it ->
                                                                    it.getValue() + ";" +
                                                                            it.getMin() + ";" +
                                                                            it.getMax() + ";" +
                                                                            it.getResult() + ";" +
                                                                            it.getSize() + ";"
                                                    )
                                                    .reduce("", String::concat)
                                                    .concat("\n")
                                    );
                                } catch (IOException e) {
                                    log.error(e.getMessage());
                                }
                            }
                    );
        }
        fileWriter.close();
    }

    private CorrelationValue getCorrelationByIdx(List<AnaliseResultRow> data, List<BigDecimal> resultSeries, Integer idx) {
        CorrelationValue resultCorr = new CorrelationValue();
        List<BigDecimal> dataSeries = new ArrayList<>();
        Boolean setResultSeries = resultSeries.isEmpty();
        data.forEach(it -> {
            BigDecimal value = it.getFactors().get(idx);
            if (setResultSeries) {
                resultSeries.add(it.getResult());
            }
            dataSeries.add(it.getFactors().get(idx));
            resultCorr.setMax(Optional.ofNullable(resultCorr.getMax()).orElse(value).max(value));
            resultCorr.setMin(Optional.ofNullable(resultCorr.getMin()).orElse(value).min(value));
            resultCorr.setResult(Optional.ofNullable(resultCorr.getResult()).orElse(BigDecimal.ZERO).add(it.getResult()));
        });

        resultCorr.setValue(
                getCorrelation(
                        dataSeries,
                        resultSeries
                )
        );
        resultCorr.setSize(data.size());

        return resultCorr;
    }

    @Override
    public List<List<CorrelationValue>> getCorrelations(AnaliseResultTable table, Integer steps) {
        if (table.getData().isEmpty()) {
            return Collections.emptyList();
        }

        List<List<CorrelationValue>> result = new ArrayList<>();
        List<BigDecimal> resultSeries = new ArrayList<>();
        List<CorrelationValue> mainRow = IntStream
                .range(0, table.getHeaders().size())
                .mapToObj(idx -> getCorrelationByIdx(table.getData(), resultSeries, idx))
                .collect(Collectors.toList());
        result.add(mainRow);

        if (steps == 1) {
            return result;
        }

        result.addAll(
                IntStream
                        .range(0, steps)
                        .mapToObj(step ->
                                IntStream
                                        .range(0, table.getHeaders().size())
                                        .mapToObj(idx -> {
                                            BigDecimal min =
                                                    step == 0 ?
                                                            mainRow.get(idx).getMin() :
                                                            (mainRow.get(idx).getMax()
                                                                    .subtract(mainRow.get(idx).getMin()))
                                                                    .multiply(BigDecimal.valueOf(step))
                                                                    .divide(
                                                                            BigDecimal.valueOf(steps),
                                                                            DataHolder.PRECISION,
                                                                            RoundingMode.HALF_UP
                                                                    )
                                                                    .add(mainRow.get(idx).getMin());
                                            BigDecimal max =
                                                    step + 1 == step ?
                                                            mainRow.get(idx).getMax() :
                                                            (mainRow.get(idx).getMax()
                                                                    .subtract(mainRow.get(idx).getMin()))
                                                                    .divide(
                                                                            BigDecimal.valueOf(steps),
                                                                            DataHolder.PRECISION,
                                                                            RoundingMode.HALF_UP
                                                                    )
                                                                    .add(min);
                                            return getCorrelationByIdx(
                                                    table.getData()
                                                            .stream()
                                                            .filter(
                                                                    it ->
                                                                            it.getFactors().get(idx).compareTo(min) >= 0 &&
                                                                                    it.getFactors().get(idx).compareTo(max) <= 0
                                                            ).collect(Collectors.toList()),
                                                    new ArrayList<>(),
                                                    idx
                                            );
                                        })
                                        .collect(Collectors.toList())
                        )
                        .collect(Collectors.toList())
        );

        return result;
    }

    private BigDecimal getCorrelation(List<BigDecimal> x, List<BigDecimal> y) {
        BigDecimal mX = getExpectedValue(x);
        BigDecimal mY = getExpectedValue(y);
        BigDecimal divider = getStandardDeviation(x, mX).multiply(getStandardDeviation(y, mY));

        return divider.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : getCovariance(x, y, mX, mY).divide(divider, DataHolder.PRECISION, RoundingMode.HALF_UP);
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
        if (numberSeries.isEmpty()) {
            return BigDecimal.ZERO;
        }
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

