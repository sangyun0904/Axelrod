package com.sykim.axelrod.hompage;

import com.google.gson.Gson;
import com.sykim.axelrod.model.*;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.PortfolioRepository;
import com.sykim.axelrod.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.resps.Tuple;

import java.util.List;

@Service
public class HomepageService {

    @Autowired
    StockRepository stockRepository;
    @Autowired
    PlayerRepository playerRepository;
    @Autowired
    PortfolioRepository portfolioRepository;

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }
    public List<Player> getAllPlayer() {
        return playerRepository.findAll();
    }
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    public List<Portfolio> getPortfolioByUser(String userId) {
        return portfolioRepository.findByUserId(userId);
    }
}
