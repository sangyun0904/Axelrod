package com.sykim.axelrod.controller;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.matching.MatchingService;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class StockController {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;

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

        List<Stock> nasdaqStockList = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                Objects.requireNonNull(classLoader.getResource("data/nasdaq_screener_1736480783742.csv").getFile())
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

        stockTradeService.createStockByStockList(nasdaqStockList);

        return "Stock generated";
    }

}
