package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, String> {
}
