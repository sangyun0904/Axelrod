package com.sykim.axelrod.matching;

import com.google.gson.Gson;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Transaction;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

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
    JedisPool jedisPool;

    private boolean systemRunning = true;
    @PreDestroy
    public void onDestroy() {
        systemRunning = false;
    }

    public List<String> tickerList = new ArrayList<>();

    @Scheduled(fixedRate = 5000)
    public void findMatch() {
        List<Stock> stockList = stockTradeService.getAllStocks();
        tickerList = stockList.stream().map(Stock::getTicker).toList();

        for (String t : tickerList) {
            System.out.println(t);
            try (Jedis jedis = jedisPool.getResource()) {
                Tuple maxPriceBuyOrder = jedis.zrangeWithScores("orderbook:buy:" + t, -1, -1).getLast();
                Tuple leastPriceSellOrder = jedis.zrangeWithScores("orderbook:sell:" + t, 0, 0).getFirst();

                matchTransaction(t, maxPriceBuyOrder, leastPriceSellOrder);
            } catch (Exception e) {
//                System.out.println(e.getMessage());
            }
        }
    }

    private Transaction matchTransaction(String ticker, Tuple buyOrderTuple, Tuple sellOrderTuple) {
        // TODO : - 주식 판매 금액이 구매 수량 금액보다 높으면 null 리턴
        //        - 판매 수량이 같거나 더 많으면 Transaction 생성 및 Player Portfolio 수정
        //        - 남은 주식 redis 에 다시 ZADD


        // 구매 주문 가격이 판매 주문 가격보다 높거나 같으면 거래 채결
        if (buyOrderTuple.getScore() >= sellOrderTuple.getScore()) {
            Gson gson = new Gson();
            System.out.println(buyOrderTuple.getScore() >= sellOrderTuple.getScore());
            Transaction.RedisOrder buyOrder = gson.fromJson(buyOrderTuple.getElement(), Transaction.RedisOrder.class);
            System.out.println(buyOrder.orderId());
            Transaction.RedisOrder sellOrder = gson.fromJson(sellOrderTuple.getElement(), Transaction.RedisOrder.class);
            System.out.println(sellOrder.orderId());

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

            stockTradeService.updateStockPrice(ticker, transactionPrice);
        }
        return null;
    }
}
