package com.sykim.axelrod;

import com.sykim.axelrod.model.Portfolio;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.PortfolioRepository;
import com.sykim.axelrod.repository.StockRepository;
import org.apache.commons.lang3.SerializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLDataException;
import java.util.Optional;

@Service
public class StockTradeService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;

    public Portfolio createStock(String player, String ticker, Double price, Long quantity) {
        Optional<Stock> stock = stockRepository.findByTicker(ticker);
        if (stock.isPresent()) {
            Portfolio newPortfolio = new Portfolio(null, player, stock.get().getStock_id(), quantity, price);
            return portfolioRepository.save(newPortfolio);
        }
        else {
//            throw new SQLDataException("Stock with " + ticker + " ticker doesn't exists");
        }
        return null;
    }
}
