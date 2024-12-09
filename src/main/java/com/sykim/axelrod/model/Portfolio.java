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
public class Portfolio {
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long portfolio_id;
    private String user_id;
    private Long stock_id;
    private Long quantity;
    private Double average_price;
}
