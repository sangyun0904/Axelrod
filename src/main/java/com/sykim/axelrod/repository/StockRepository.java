package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
