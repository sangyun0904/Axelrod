package com.sykim.axelrod.matching;

import com.google.gson.Gson;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.util.List;
import java.util.Objects;

@Configuration
public class MatchingExecutorPool {

    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private MatchingService matchingService;

    @Value("${spring.redis.host}")
    private String REDIS_HOST; // Redis 호스트 주소
    @Value("${spring.redis.port}")
    private int REDIS_PORT;    // Redis 포트 번호

    public List<String> tickerList = List.of("005930");

    @Bean
    public boolean findMatch() {
        JedisPool jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        for (String t: tickerList) {
            try (Jedis jedis = jedisPool.getResource()) {
                Tuple maxPriceBuyOrder = jedis.zrangeWithScores("orderbook:buy:" + t, -1, -1).getLast();
                System.out.println(maxPriceBuyOrder);
                Tuple leastPriceSellOrder = jedis.zrangeWithScores("orderbook:sell:" + t, 0, 0).getFirst();
                System.out.println(leastPriceSellOrder);
                matchTransaction(t, maxPriceBuyOrder, leastPriceSellOrder);
            }
            break;
        }
        return true;
    }

    private Transaction matchTransaction(String ticker, Tuple buyOrderTuple, Tuple sellOrderTuple) {
        // TODO : - 주식 판매 금액이 구매 수량 금액보다 높으면 null 리턴
        //        - 판매 수량이 같거나 더 많으면 Transaction 생성 및 Player Portfolio 수정
        //        - 남은 주식 redis 에 다시 ZADD
        JedisPool jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);

        // 구매 주문 가격이 판매 주문 가격보다 높거나 같으면 거래 채결
        if (buyOrderTuple.getScore() >= sellOrderTuple.getScore()) {
            Gson gson = new Gson();
            Transaction.RedisOrder buyOrder = gson.fromJson(buyOrderTuple.getElement(), Transaction.RedisOrder.class);
            Transaction.RedisOrder sellOrder = gson.fromJson(sellOrderTuple.getElement(), Transaction.RedisOrder.class);

            // 중간 가격으로 거래 진행
            Double transactionPrice = buyOrderTuple.getScore() + sellOrderTuple.getScore() / 2;

            Long quantity = Math.min(buyOrder.quantity(), sellOrder.quantity());
            stockTradeService.createTransaction(ticker, transactionPrice, quantity, sellOrder.userId(), buyOrder.userId());
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zrem("orderbook:buy:" + ticker, buyOrderTuple.getElement());
                jedis.zrem("orderbook:sell:" + ticker, sellOrderTuple.getElement());
                if (buyOrder.quantity().equals(quantity)) {
                    jedis.zadd("orderbook:sell:" + ticker , sellOrderTuple.getScore(), "{\"orderId\":\"" + sellOrder.orderId() + "\",\"quantity\":" + (sellOrder.quantity() - quantity) + ",\"userId\":\"" + sellOrder.userId() + "\"}");
                } else {
                    jedis.zadd("orderbook:sell:" + ticker , buyOrderTuple.getScore(), "{\"orderId\":\"" + buyOrder.orderId() + "\",\"quantity\":" + (buyOrder.quantity() - quantity) + ",\"userId\":\"" + buyOrder.userId() + "\"}");
                }
            }
        }
        return null;
    }
}
