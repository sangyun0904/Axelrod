package com.sykim.axelrod;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.matching.TransactionOrderListComponent;
import com.sykim.axelrod.model.*;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.model.Stock.StockCreate;
import com.sykim.axelrod.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private TransactionOrderListComponent transactionOrderListComponent;

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
            if (type == TransactionOrder.Type.BUY) transactionOrderListComponent.buyOrderList.add(order);
            else transactionOrderListComponent.sellOrderList.add(order);

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

    @Transactional
    public boolean isAllowedToMakeOrder(TransactionOrder.OrderRequest order, TransactionOrder.Type orderType) {

        String userId = order.playerId();

        Optional<Stock> stockOptional = stockRepository.findByTicker(order.ticker());
        if (stockOptional.isEmpty()) throw new RuntimeException("ticker : " + order.ticker() + " 의 주식이 존재하지 않습니다.");
        Optional<Player> playerOptional = playerRepository.findById(userId);
        if (playerOptional.isEmpty()) throw new RuntimeException("user id : " + userId + " 의 사용자가 존재하지 않습니다.");

        // 매도할 주식을 충분히 가지고 있는지 확인 (admin 제회)
        if (!userId.equals("admin")) {
            if (orderType == TransactionOrder.Type.SELL) {
                Optional<Portfolio> portfolioOptional = portfolioRepository.findByPlayerIdAndTicker(userId, stockOptional.get().getTicker());
                if (portfolioOptional.isPresent()) {
                    Portfolio portfolio = portfolioOptional.get();
                    if (portfolio.getQuantity() < order.quantity()) {
                        throw new RuntimeException("Not enough quantity to sell!");
                    }
                }
                else {
                    throw new RuntimeException("Not enough quantity to sell!");
                }
            }
        }

        return true;
    }

    public List<Stock> getNasdaqStockListFromCSV() throws IOException, CsvValidationException {
        List<Stock> nasdaqStockList = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                Objects.requireNonNull(classLoader.getResource("data/nasdaq_screener_1736480783742.csv")).getFile()
        );
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);

        String[] header = csvReader.readNext();
        Map<String, Integer> headerMap = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerMap.put(header[i], i);
        }

        String[] record;
        while ((record = csvReader.readNext()) != null) {
            nasdaqStockList.add(new Stock(
                    null,
                    record[headerMap.get("Symbol")],
                    record[headerMap.get("Name")],
                    "NASDAQ",
                    record[headerMap.get("Sector")],
                    record[headerMap.get("Industry")],
                    Double.parseDouble(record[headerMap.get("Last Sale")].substring(1)),
                    LocalDateTime.now()));
        }

        return nasdaqStockList;
    }
}

