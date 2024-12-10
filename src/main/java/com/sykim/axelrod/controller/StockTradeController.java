package com.sykim.axelrod.controller;


import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.model.Transaction;
import com.sykim.axelrod.model.Transaction.StockIssuance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StockTradeController {

    @Autowired
    private StockTradeService stockTradeService;

    @PostMapping("/issue")
    public Transaction issueStocks(
            @RequestBody StockIssuance issuance
    ) {
        return stockTradeService.createStock(issuance);
    }
}
