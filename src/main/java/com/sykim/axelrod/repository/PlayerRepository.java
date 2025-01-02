package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, String> {
    Optional<Player> getByUsernameAndPassword(String username, String password);
}
