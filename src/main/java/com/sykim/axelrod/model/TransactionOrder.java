package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOrder {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String playerId;
    private String ticker;
    private Long quantity;
    private Double price;
    private Type type;

    public enum Type { SELL, BUY }
}
