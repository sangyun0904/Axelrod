package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserIdAndStockId(String s, Long id);
    List<Portfolio> findByUserId(String userId);
}
