package com.sykim.axelrod.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    // stock symbol
    @Column(unique=true)
    private String ticker;
    private String name;
    // e.g. NASDAQ
    private String market;
    // industry sector e.g. Technology Services
    private String sector;
    private String industry;
    private Double price;
    private LocalDateTime timeStamp;

    public void setCurrentPrice(Double transactionPrice) {
        this.price = transactionPrice;
    }

    public record StockCreate(String ticker, String name, String market, String sector, Double price) {}

    public record History(String date, Double open, Double high, Double low, Double close, Double adjClose, Long volume) {}

    public record StockPageData(List<Stock> stockPage, List<Integer> pageNumbers, int currentPage) {}
}
