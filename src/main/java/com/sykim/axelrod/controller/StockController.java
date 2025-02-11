package com.sykim.axelrod.controller;


import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.AccountService;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.exceptions.AccountDoseNotExistException;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.exceptions.NotEnoughBalanceException;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.*;
import com.sykim.axelrod.model.Stock.StockCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class StockController {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;
    @Autowired
    private AccountService accountService;

    private final int PAGE_SIZE=15;

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
    ) throws NotAvailableTickerException, NotEnoughBalanceException, AccountDoseNotExistException {
        //TODO: Account Balance 확인
        accountService.checkAccountBalance(transactionOrder);
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

    @GetMapping("/searchStock")
    public ResponseEntity<Stock.StockPageData> searchStockByKeyword(@RequestParam("keyword") String keyword, @RequestParam("pageNum") int pageNum) {
        Page<Stock> stockPage = stockTradeService.searchStockByKeyword(keyword, PageRequest.of(pageNum - 1, PAGE_SIZE));
        int lastPage = stockPage.getTotalPages();
        List<Integer> pageNumbers = List.of(1);
        if (lastPage > 0) {
            pageNumbers = getPageNumbers(pageNum, lastPage);
        }

        return ResponseEntity.ok(new Stock.StockPageData(stockPage.stream().toList(), pageNumbers, pageNum));
    }

    private List<Integer> getPageNumbers(int currentPage, int lastPage) {
        List<Integer> pageNumbers;

        if (currentPage < 5) {
            pageNumbers = IntStream.rangeClosed(1, Math.min(10, lastPage))
                    .boxed()
                    .collect(Collectors.toList());
        }
        else {
            pageNumbers = IntStream.rangeClosed(currentPage-4, Math.min(currentPage + 5, lastPage))
                    .boxed()
                    .collect(Collectors.toList());
        }

        return pageNumbers;
    }

}
