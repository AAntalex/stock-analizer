package com.antalex;

import com.antalex.mapper.DtoConverter;
import com.antalex.dto.DataChartDto;
import com.antalex.model.DataChart;
import com.antalex.stockAnalyzer.StockAnalyzerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.GenericTypeResolver;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StockAnalyzerApplication.class)
public class ApplicationTests {
	private List<DtoConverter> dtoConverters = new ArrayList<>();

	@Autowired
	public void setDtoConverters(List<DtoConverter> dtoConverters) {
		this.dtoConverters = dtoConverters;
	}

	@Test
	public void contextLoads() {
		for (DtoConverter<?, ?> dtoConverter : dtoConverters) {
			Class<?>[] classes = GenericTypeResolver.resolveTypeArguments(dtoConverter.getClass(), DtoConverter.class);

			if (classes[1] == DataChartDto.class && classes[0] == DataChart.class) {
				System.out.println("AAA " + dtoConverter);
			}
		}
	}

}
