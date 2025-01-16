package com.sykim.axelrod;

import com.sykim.axelrod.exceptions.NotAvailableTickerException;
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
    public Transaction issueStock(TransactionOrder.OrderRequest issuance) throws NotAvailableTickerException {
        Optional<Stock> stockOrNull = stockRepository.findByTicker(issuance.ticker());

        if (stockOrNull.isPresent()) {
            Stock stock = stockOrNull.get();
            Transaction transaction = new Transaction(null, issuance.playerId(), issuance.ticker(), issuance.quantity(), issuance.price(), Transaction.Type.ISSUE, LocalDate.now());

            // 플레이어 포트폴리오 업데이트
            Optional<Portfolio> portfolioOrNull = portfolioRepository.findByPlayerIdAndTicker(issuance.playerId(), stock.getTicker());
            Portfolio newPortfolio;
            if (portfolioOrNull.isPresent()) {
                Portfolio oldPortfolio = portfolioOrNull.get();
                Double newAvgPrice = (oldPortfolio.getAveragePrice() * oldPortfolio.getQuantity() + issuance.price() * issuance.quantity()) / oldPortfolio.getQuantity() + issuance.quantity();
                newPortfolio = new Portfolio(issuance.playerId(), stock.getTicker(), issuance.quantity() + oldPortfolio.getQuantity(), newAvgPrice);
            } else {
                newPortfolio = new Portfolio(issuance.playerId(), stock.getTicker(), issuance.quantity(), issuance.price());
            }
            portfolioRepository.save(newPortfolio);
            return transactionRepository.save(transaction);
        }
        else {
            throw new NotAvailableTickerException("Stock with " + issuance.ticker() + " ticker doesn't exists");
        }
    }

    @Transactional
    public Stock createStock(StockCreate stock) {
        Stock newStock = new Stock(null, stock.ticker(), stock.name(), stock.market(), stock.sector(), null, stock.price(), LocalDateTime.now());
        return stockRepository.save(newStock);
    }

    @Transactional
    public TransactionOrder createTransactionOrder(TransactionOrder.OrderRequest transactionOrder, TransactionOrder.Type type) throws NotAvailableTickerException {
        Optional<Stock> stockOrNull = stockRepository.findByTicker(transactionOrder.ticker());

        if (stockOrNull.isPresent()) {
            Stock stock = stockOrNull.get();
            TransactionOrder order = new TransactionOrder(null, transactionOrder.playerId(), transactionOrder.ticker(), transactionOrder.quantity(), transactionOrder.price(), type);
            return orderRepository.save(order);
        } else {
            throw new NotAvailableTickerException("Stock with " + transactionOrder.ticker() + " ticker doesn't exists");
        }
    }

    @Transactional
    public synchronized void createTransaction(String ticker, Double price, Long quantity, String sellPlayer, String buyPlayer) {
        Stock stock = stockRepository.findByTicker(ticker).get();

        Transaction transaction = new Transaction(null, sellPlayer, ticker, quantity, price, Transaction.Type.SELL, LocalDate.now());
        transactionRepository.save(transaction);
        transaction = new Transaction(null, buyPlayer, ticker, quantity, price, Transaction.Type.BUY, LocalDate.now());
        transactionRepository.save(transaction);

        Portfolio buyerPF = getPortfolioByUserAndStock(buyPlayer, stock.getTicker());
        Double newPFAvg = (buyerPF.getAveragePrice() * buyerPF.getQuantity() + quantity * price) / (buyerPF.getQuantity() + quantity);
        Portfolio newPF = new Portfolio(buyPlayer, stock.getTicker(), buyerPF.getQuantity() + quantity, newPFAvg);
        portfolioRepository.save(newPF);
        Portfolio sellerPF = getPortfolioByUserAndStock(sellPlayer, stock.getTicker());
        if (sellerPF.getQuantity() > quantity) {
            newPFAvg = (sellerPF.getAveragePrice() * sellerPF.getQuantity() + quantity * price) / (sellerPF.getQuantity() + quantity);
        } else {
            newPFAvg = 0d;
        }
        newPF = new Portfolio(sellPlayer, stock.getTicker(), sellerPF.getQuantity() - quantity, newPFAvg);
        portfolioRepository.save(newPF);
    }

    @Transactional
    private Portfolio getPortfolioByUserAndStock(String playerId, String ticker) {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findByPlayerIdAndTicker(playerId, ticker);
        return portfolioOptional.orElseGet(() -> portfolioRepository.save(new Portfolio(playerId, ticker, 0L, 0d)));
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

    public List<Portfolio> getPlayerPortfolio(String userId) {
        return portfolioRepository.findByPlayerId(userId);
    }

    public List<Stock> createStockByStockList(List<Stock> nasdaqStockList) {
        return stockRepository.saveAll(nasdaqStockList);
    }
}

