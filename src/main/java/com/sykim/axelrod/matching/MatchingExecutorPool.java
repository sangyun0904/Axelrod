package com.sykim.axelrod.matching;

import com.google.gson.Gson;
import com.sykim.axelrod.AccountService;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.exceptions.AccountDoseNotExistException;
import com.sykim.axelrod.exceptions.NotEnoughBalanceException;
import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Transaction;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MatchingExecutorPool {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;
    @Autowired
    private AccountService accountService;

    private final int LOOP_NUM=300;
    private int stockMatchingLoopIndex = 0;

    @Autowired
    JedisPool jedisPool;
    @Autowired
    TransactionOrderListComponent transactionOrderListComponent;

    private boolean systemRunning = true;
    @PreDestroy
    public void onDestroy() {
        systemRunning = false;
    }

    public List<String> tickerList = new ArrayList<>();

    @Scheduled(fixedRate = 100)
    public void findMatch() {
        System.out.println(Thread.currentThread().getName() + " - 실행됨");
//        System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());
        List<Stock> stockList = stockTradeService.getAllStocks();
        tickerList = stockList.stream().map(Stock::getTicker).toList();

//        System.out.println(tickerList.size() / LOOP_NUM);

        for (int i=0; i < LOOP_NUM; i++) {
            int idx = stockMatchingLoopIndex * LOOP_NUM + i;
            if (tickerList.size() <= idx) break;
            String t = tickerList.get(idx);

//            System.out.println(t);
            try (Jedis jedis = jedisPool.getResource()) {
                Tuple maxPriceBuyOrder = jedis.zrangeWithScores("orderbook:buy:" + t, -1, -1).getLast();
                Tuple leastPriceSellOrder = jedis.zrangeWithScores("orderbook:sell:" + t, 0, 0).getFirst();

                matchTransaction(t, maxPriceBuyOrder, leastPriceSellOrder);
            } catch (Exception e) {
//                System.out.println(e.getMessage());
            }
        }
        stockMatchingLoopIndex = (stockMatchingLoopIndex + 1) % (tickerList.size() / LOOP_NUM + 1);
//        System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());
    }

    @Transactional
    private Transaction matchTransaction(String ticker, Tuple buyOrderTuple, Tuple sellOrderTuple) throws NotEnoughBalanceException, AccountDoseNotExistException {
        // TODO : - 주식 판매 금액이 구매 수량 금액보다 높으면 null 리턴
        //        - 판매 수량이 같거나 더 많으면 Transaction 생성 및 Player Portfolio 수정
        //        - 남은 주식 redis 에 다시 ZADD


        // 구매 주문 가격이 판매 주문 가격보다 높거나 같으면 거래 채결
        if (buyOrderTuple.getScore() >= sellOrderTuple.getScore()) {
            Gson gson = new Gson();
//            System.out.println(buyOrderTuple.getScore() >= sellOrderTuple.getScore());
            Transaction.RedisOrder buyOrder = gson.fromJson(buyOrderTuple.getElement(), Transaction.RedisOrder.class);
//            System.out.println(buyOrder.orderId());
            Transaction.RedisOrder sellOrder = gson.fromJson(sellOrderTuple.getElement(), Transaction.RedisOrder.class);
//            System.out.println(sellOrder.orderId());

            // 중간 가격으로 거래 진행
            Double transactionPrice = (buyOrderTuple.getScore() + sellOrderTuple.getScore()) / 2;

            System.out.println("transaction (price: " + transactionPrice + ", ticker: " + ticker + ")");

            Long quantity = Math.min(buyOrder.quantity(), sellOrder.quantity());
            stockTradeService.createTransaction(ticker, transactionPrice, quantity, sellOrder.userId(), buyOrder.userId());
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zrem("orderbook:buy:" + ticker, buyOrderTuple.getElement());
                jedis.zrem("orderbook:sell:" + ticker, sellOrderTuple.getElement());
                if (buyOrder.quantity().equals(quantity)) {
                    jedis.zadd("orderbook:sell:" + ticker , sellOrderTuple.getScore(), "{\"orderId\":\"" + sellOrder.orderId() + "\",\"quantity\":" + (sellOrder.quantity() - quantity) + ",\"userId\":\"" + sellOrder.userId() + "\"}");
                } else {
                    jedis.zadd("orderbook:buy:" + ticker , buyOrderTuple.getScore(), "{\"orderId\":\"" + buyOrder.orderId() + "\",\"quantity\":" + (buyOrder.quantity() - quantity) + ",\"userId\":\"" + buyOrder.userId() + "\"}");
                }
            }

            Account buyAccount = accountService.getAccountByUsername(buyOrder.userId()).get(0);
            Account sellAccount = accountService.getAccountByUsername(sellOrder.userId()).get(0);
            accountService.changeAccountBalance(buyAccount.getAccountNum(), 0 - transactionPrice * quantity);
            accountService.changeAccountBalance(sellAccount.getAccountNum(), transactionPrice * quantity);

            stockTradeService.updateStockPrice(ticker, transactionPrice);
            System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());
//            transactionOrderListComponent.reloadOrderData();
            System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());
        }
        return null;
    }
}
