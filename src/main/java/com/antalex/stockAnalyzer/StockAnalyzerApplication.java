package com.antalex.stockAnalyzer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;;

import org.jsoup.nodes.Document;

import java.io.IOException;

@SpringBootApplication
@ComponentScan("com.antalex")
public class StockAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockAnalyzerApplication.class, args);

        try {
            Document doc = Jsoup.connect("https://www.dohod.ru/ik/analytics/dividend/").get();
            Element element = doc.getElementById("table-dividend");
            System.out.println("AAA html " + element.html());
        } catch (IOException e) {

        }
    }

}
