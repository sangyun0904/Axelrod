package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOrder implements Comparable<TransactionOrder>{
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String playerId;
    private String ticker;
    private Long quantity;
    private Double price;
    private Type type;

    @Override
    public int compareTo(@NotNull TransactionOrder order) {
        int tickerComparison = ticker.compareTo(order.ticker);
        if (tickerComparison != 0) return tickerComparison;

        return price.compareTo(order.price);
    }

    public enum Type { SELL, BUY }

    public record OrderRequest(String playerId, String ticker, Long quantity, Double price) {}
}
