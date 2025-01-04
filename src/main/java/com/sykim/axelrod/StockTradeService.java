package com.sykim.axelrod;

import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.model.Portfolio;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Stock.StockCreate;
import com.sykim.axelrod.model.Transaction;
import com.sykim.axelrod.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLDataException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StockTradeService {

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Transaction issueStock(TransactionOrder.OrderRequest issuance) {
        Optional<Stock> stockOrNull = stockRepository.findByTicker(issuance.ticker());

        if (stockOrNull.isPresent()) {
            Stock stock = stockOrNull.get();
            Transaction transaction = new Transaction(null, issuance.playerId(), issuance.ticker(), issuance.quantity(), issuance.price(), Transaction.Type.ISSUE, LocalDate.now());

            // 플레이어 포트폴리오 업데이트
            Optional<Portfolio> portfolioOrNull = portfolioRepository.findByUserIdAndStockId(issuance.playerId(), stock.getId());
            Portfolio newPortfolio;
            if (portfolioOrNull.isPresent()) {
                Portfolio oldPortfolio = portfolioOrNull.get();
                Double newAvgPrice = (oldPortfolio.getAveragePrice() * oldPortfolio.getQuantity() + issuance.price() * issuance.quantity()) / oldPortfolio.getQuantity() + issuance.quantity();
                newPortfolio = new Portfolio(oldPortfolio.getId(), issuance.playerId(), stock.getId(), issuance.quantity() + oldPortfolio.getQuantity(), newAvgPrice);
            } else {
                newPortfolio = new Portfolio(null, issuance.playerId(), stock.getId(), issuance.quantity(), issuance.price());
            }
            portfolioRepository.save(newPortfolio);
            return transactionRepository.save(transaction);
        }
        else {
//            throw new SQLDataException("Stock with " + ticker + " ticker doesn't exists");
        }
        return null;
    }

    @Transactional
    public Stock createStock(StockCreate stock) {
        Stock newStock = new Stock(null, stock.ticker(), stock.name(), stock.market(), stock.sector(), stock.price(), LocalDateTime.now());
        return stockRepository.save(newStock);
    }

    @Transactional
    public TransactionOrder createTransactionOrder(TransactionOrder.OrderRequest transactionOrder, TransactionOrder.Type type) throws SQLDataException {
        Optional<Stock> stockOrNull = stockRepository.findByTicker(transactionOrder.ticker());

        if (stockOrNull.isPresent()) {
            Stock stock = stockOrNull.get();
            TransactionOrder order = new TransactionOrder(null, transactionOrder.playerId(), transactionOrder.ticker(), transactionOrder.quantity(), transactionOrder.price(), type);
            return orderRepository.save(order);
        } else {
            throw new SQLDataException("Stock with " + transactionOrder.ticker() + " ticker doesn't exists");
        }
    }

    @Transactional
    public void createTransaction(String ticker, Double price, Long quantity, String sellPlayer, String buyPlayer) {
        Stock stock = stockRepository.findByTicker(ticker).get();

        Transaction transaction = new Transaction(null, sellPlayer, ticker, quantity, price, Transaction.Type.SELL, LocalDate.now());
        transactionRepository.save(transaction);
        transaction = new Transaction(null, buyPlayer, ticker, quantity, price, Transaction.Type.BUY, LocalDate.now());
        transactionRepository.save(transaction);

        Portfolio sellerPF = getPortfolioByUserAndStock(sellPlayer, stock.getId());
        Double newPFAvg = (sellerPF.getAveragePrice() * sellerPF.getQuantity() + quantity * price) / (sellerPF.getQuantity() + quantity);
        Portfolio newPF = new Portfolio(sellerPF.getId(), sellPlayer, stock.getId(), sellerPF.getQuantity() + quantity, newPFAvg);
        portfolioRepository.save(newPF);
        Portfolio buyerPF = getPortfolioByUserAndStock(buyPlayer, stock.getId());
        if (buyerPF.getQuantity() > quantity) {
            newPFAvg = (buyerPF.getAveragePrice() * buyerPF.getQuantity() + quantity * price) / (buyerPF.getQuantity() + quantity);
        } else {
            newPFAvg = 0d;
        }
        newPF = new Portfolio(buyerPF.getId(), sellPlayer, stock.getId(), buyerPF.getQuantity() - quantity, newPFAvg);
        portfolioRepository.save(newPF);
    }

    @Transactional
    private Portfolio getPortfolioByUserAndStock(String userId, Long stockId) {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByUserIdAndStockId(userId, stockId);
        return portfolioOptional.orElseGet(() -> portfolioRepository.save(new Portfolio(null, userId, stockId, 0L, 0d)));
    }

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public void updateStockPrice(String ticker, Double transactionPrice) {
        Optional<Stock> stockOptional = stockRepository.findByTicker(ticker);
        if (stockOptional.isPresent()) {
            Stock stock = stockOptional.get();
            stock.setCurrentPrice(transactionPrice);
            stockRepository.save(stock);
        }
    }
}
