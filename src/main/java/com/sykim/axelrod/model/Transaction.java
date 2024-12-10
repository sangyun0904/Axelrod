package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String userId;
    private Long stockId;
    private Long quantity;
    private Double price;
    private Type type;
    private LocalDate transactionDate;

    public enum Type { SELL, BUY, ISSUE }

    public record StockIssuance(String userId, String ticker, Long quantity, Double price) {}
}
