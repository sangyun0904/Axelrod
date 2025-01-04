package com.sykim.axelrod;

import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PlayerRepository playerRepository;

    public Player loginPlayer(Player.PlayerLogin login) {
        Optional<Player> player = playerRepository.getByUsernameAndPassword(login.username(), login.password());

        if (player.isPresent()) return player.get();
        else throw new NoSuchElementException("Check your username and password");
    }

}
