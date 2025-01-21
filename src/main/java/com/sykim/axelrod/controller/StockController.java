package com.sykim.axelrod.controller;


import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.AccountService;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.Bank;
import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Stock.StockCreate;
import com.sykim.axelrod.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

@RestController
public class StockController {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;
    @Autowired
    private AccountService accountService;

    @PostMapping("/issue")
    public Transaction issueStocks(
            @RequestBody TransactionOrder.OrderRequest issuance
    ) throws NotAvailableTickerException {
        return stockTradeService.issueStock(issuance);
    }

    @PostMapping("/create")
    public Stock createStock(
            @RequestBody StockCreate stock
    ) {
        return stockTradeService.createStock(stock);
    }

    @GetMapping("/match")
    public boolean matchingOrder() {
        return matchingService.matching("005930");
    }

    @PostMapping("/order/buy")
    public ResponseEntity crateBuyStockOrder(
            @RequestBody TransactionOrder.OrderRequest transactionOrder
    ) throws NotAvailableTickerException {
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(transactionOrder, TransactionOrder.Type.BUY);
        matchingService.bookStockOrder(newTransactionOrder.getId(), transactionOrder.playerId(), transactionOrder.ticker(), TransactionOrder.Type.BUY, transactionOrder.price(), transactionOrder.quantity());
        return ResponseEntity.ok("success!");
    }

    @PostMapping("/order/sell")
    public ResponseEntity crateSellStockOrder(
            @RequestBody TransactionOrder.OrderRequest transactionOrder
    ) throws NotAvailableTickerException {
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(transactionOrder, TransactionOrder.Type.SELL);
        matchingService.bookStockOrder(newTransactionOrder.getId(), transactionOrder.playerId(), transactionOrder.ticker(), TransactionOrder.Type.SELL, transactionOrder.price(), transactionOrder.quantity());
        return ResponseEntity.ok("success!");
    }

    @GetMapping("/stocks")
    public List<Stock> getAllStocks() {
        return stockTradeService.getAllStocks();
    }

    @GetMapping("/generateStock")
    public String generateStockData() throws IOException, CsvValidationException {
        List<Stock> nasdaqStockList = stockTradeService.getNasdaqStockListFromCSV();
        stockTradeService.createStockByStockList(nasdaqStockList);
        return "Stock generated";
    }

    @GetMapping("/generateBank")
    public String generateBankData() throws IOException, CsvValidationException {
        List<Bank> bankList = accountService.getBankListFromCSV();
        accountService.createBankByList(bankList);
        return "Bank generated";
    }

}
