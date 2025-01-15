package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByPlayerId(String playerId);

    Optional<Portfolio> findByPlayerIdAndTicker(String playerId, String ticker);
}
