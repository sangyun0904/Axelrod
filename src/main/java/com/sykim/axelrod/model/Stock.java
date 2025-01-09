package com.sykim.axelrod.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    // industry sector e.g. Manufacture
    private String sector;
    private Double price;
    private LocalDateTime timeStamp;

    public void setCurrentPrice(Double transactionPrice) {
        this.price = transactionPrice;
    }

    public record StockCreate(String ticker, String name, String market, String sector, Double price) {}

    public record Diamond(Double carat, int price) {}
}
