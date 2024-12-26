package com.sykim.axelrod.controller;


import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Stock.StockCreate;
import com.sykim.axelrod.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLDataException;
import java.util.List;

@RestController
public class StockController {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;

    @PostMapping("/issue")
    public Transaction issueStocks(
            @RequestBody Transaction.TransactionOrder issuance
    ) {
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
    public void crateBuyStockOrder(
            @RequestBody Transaction.TransactionOrder transactionOrder
    ) throws SQLDataException {
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(transactionOrder, TransactionOrder.Type.BUY);
        matchingService.bookStockOrder(newTransactionOrder.getId(), transactionOrder.userId(), transactionOrder.ticker(), TransactionOrder.Type.BUY, transactionOrder.price(), transactionOrder.quantity());
    }

    @PostMapping("/order/sell")
    public void crateSellStockOrder(
            @RequestBody Transaction.TransactionOrder transactionOrder
    ) throws SQLDataException {
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(transactionOrder, TransactionOrder.Type.SELL);
        matchingService.bookStockOrder(newTransactionOrder.getId(), transactionOrder.userId(), transactionOrder.ticker(), TransactionOrder.Type.SELL, transactionOrder.price(), transactionOrder.quantity());
    }

    @GetMapping("/stocks")
    public List<Stock> getAllStocks() {
        return stockTradeService.getAllStocks();
    }

}
