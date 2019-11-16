package com.antalex.stockAnalyzer;

import com.antalex.holders.DataHolder;
import com.antalex.persistence.entity.RateEntity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;;import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;

@SpringBootApplication
@ComponentScan("com.antalex")
public class StockAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockAnalyzerApplication.class, args);

/*
        try {
            Document doc = Jsoup.connect("https://www.dohod.ru/ik/analytics/dividend/").get();
            Element element = doc.getElementById("table-dividend");
            System.out.println("AAA html " + element.html());
        } catch (IOException e) {

        }
*/
    }

}
