package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class PortfolioId implements Serializable {
    private String playerId;
    private String ticker;
}
