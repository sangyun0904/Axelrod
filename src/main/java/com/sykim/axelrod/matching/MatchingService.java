package com.sykim.axelrod.matching;

import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Transaction;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.UUID;

@Service
public class MatchingService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PlayerRepository playerRepository;

    public void bookStockOrder(String userId, String ticker, String orderType, Double price, int quantity) {
        // Redis 연결 설정
        String redisHost = "localhost"; // Redis 호스트 주소
        int redisPort = 6379;          // Redis 포트 번호

        Optional<Stock> stockOptional = stockRepository.findByTicker(ticker);
        if (stockOptional.isEmpty()) throw new RuntimeException("ticker : " + ticker + " 의 주식이 존재하지 않습니다.");
        Optional<Player> playerOptional = playerRepository.findById(userId);
        if (playerOptional.isEmpty()) throw new RuntimeException("user id : " + userId + " 의 사용자가 존재하지 않습니다.");

        String type;
        if (Transaction.Type.valueOf(orderType.toUpperCase())==Transaction.Type.BUY) type = "buy";
        else if (Transaction.Type.valueOf(orderType.toUpperCase())==Transaction.Type.SELL) type = "sell";
        else throw new RuntimeException("order type 이 올바르지 않습니다.");

        try (Jedis jedis = new Jedis(redisHost, redisPort)) {
            System.out.println("Connected to Redis!");

            // Redis 키: orderbook:type:<ticker>
            String orderKey = "orderbook:" + type + ":" + ticker;

            UUID orderId = UUID.randomUUID();

            // ZADD 명령으로 매수/매도 주문 추가
            jedis.zadd(orderKey, price, "{\"orderId\":\"" + orderId + "\",\"quantity\":" + quantity + ",\"userId\":\"" + userId + "\"}");

            System.out.println("order book: " + jedis.zrangeWithScores(orderKey, 0, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

