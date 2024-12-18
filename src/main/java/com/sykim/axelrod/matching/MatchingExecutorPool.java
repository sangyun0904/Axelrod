package com.sykim.axelrod.matching;

import com.sykim.axelrod.StockTradeService;
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
                List<Tuple> buyOrder = jedis.zrangeWithScores("orderbook:buy:" + t, 0, -1);
                System.out.println(buyOrder);
            }
            break;
        }
        return true;
    }
}
