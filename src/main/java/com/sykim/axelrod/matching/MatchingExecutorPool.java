package com.sykim.axelrod.matching;

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

    public List<String> tickerList = List.of("KSY");

    @Bean
    public boolean findMatch() {
        JedisPool jedisPool = new JedisPool(REDIS_HOST, REDIS_PORT);
        for (String t: tickerList) {
            try (Jedis jedis = jedisPool.getResource()) {
                Tuple buyOrder = jedis.zrangeWithScores("orderbook:buy:" + t, 0, 0).getFirst();
                System.out.println(buyOrder);
                List<Tuple> sellOrdersByScore = jedis.zrangeByScoreWithScores("orderbook:sell:" + t, buyOrder.getScore(), buyOrder.getScore());
                Transaction transaction = matchTransaction(buyOrder, sellOrdersByScore);
            }
            break;
        }
        return true;
    }

    private Transaction matchTransaction(Tuple buyOrder, List<Tuple> SellOrderList) {
        // TODO : - 주식 판매 수량이 구매 수량 보다 적으면 null 리턴
        //        - 판매 수량이 같거나 더 많으면 Transaction 생성 및 Player Portfolio 수정 후 Transaction 리턴

        return null;
    }
}
