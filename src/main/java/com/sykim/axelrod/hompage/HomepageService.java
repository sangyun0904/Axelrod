package com.sykim.axelrod.hompage;

import com.google.gson.Gson;
import com.sykim.axelrod.model.*;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.PortfolioRepository;
import com.sykim.axelrod.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.resps.Tuple;

import java.util.Collections;
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
        return portfolioRepository.findByPlayerId(userId);
    }

    public Page<Stock> findStockPaginated(Pageable pageable) {
        List<Stock> stocks = stockRepository.findAll();

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Stock> stockList;

        if (stocks.size() < startItem) {
            stockList = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, stocks.size());
            stockList = stocks.subList(startItem, toIndex);
        }

        Page<Stock> stockPage = new PageImpl<>(stockList, PageRequest.of(currentPage, pageSize), stocks.size());

        return stockPage;
    }
}
