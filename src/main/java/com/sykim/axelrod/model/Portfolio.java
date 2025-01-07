package com.sykim.axelrod.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PortfolioId.class)
public class Portfolio {
    @Id
    private String playerId;
    @Id
    private String ticker;
    private Long quantity;
    private Double averagePrice;
}
