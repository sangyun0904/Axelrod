package com.sykim.axelrod.matching;

import com.sykim.axelrod.model.Portfolio;
import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.repository.PlayerRepository;
import com.sykim.axelrod.repository.PortfolioRepository;
import com.sykim.axelrod.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.Optional;

@Service
public class MatchingService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PortfolioRepository portfolioRepository;

    @Value("${spring.redis.host}")
    private String REDIS_HOST; // Redis 호스트 주소
    @Value("${spring.redis.port}")
    private int REDIS_PORT;           // Redis 포트 번호

    public void bookStockOrder(Long orderId, String userId, String ticker, TransactionOrder.Type orderType, Double price, Long quantity) {

        Optional<Stock> stockOptional = stockRepository.findByTicker(ticker);
        if (stockOptional.isEmpty()) throw new RuntimeException("ticker : " + ticker + " 의 주식이 존재하지 않습니다.");
        Optional<Player> playerOptional = playerRepository.findById(userId);
        if (playerOptional.isEmpty()) throw new RuntimeException("user id : " + userId + " 의 사용자가 존재하지 않습니다.");

        // 매도할 주식을 충분히 가지고 있는지 확인 (admin 제회)
        if (!userId.equals("admin")) {
            if (orderType == TransactionOrder.Type.SELL) {
                Optional<Portfolio> portfolioOptional = portfolioRepository.findByPlayerIdAndTicker(userId, stockOptional.get().getTicker());
                if (portfolioOptional.isPresent()) {
                    Portfolio portfolio = portfolioOptional.get();
                    if (portfolio.getQuantity() < quantity) {
                        throw new RuntimeException("Not enough quantity to sell!");
                    }
                }
                else {
                    throw new RuntimeException("Not enough quantity to sell!");
                }
            }
        }

        String type;
        if (orderType== TransactionOrder.Type.BUY) type = "buy";
        else if (orderType== TransactionOrder.Type.SELL) type = "sell";
        else throw new RuntimeException("order type 이 올바르지 않습니다.");

        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {

            // Redis 키: orderbook:type:<ticker>
            String orderKey = "orderbook:" + type + ":" + ticker;

            // ZADD 명령으로 매수/매도 주문 추가
            jedis.zadd(orderKey, price, "{\"orderId\":\"" + orderId + "\",\"quantity\":" + quantity + ",\"userId\":\"" + userId + "\"}");

            System.out.println("order book: " + jedis.zrangeWithScores(orderKey, 0, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean matching(String ticker) {
        String sellKey = "orderbook:sell:" + ticker;
        String buyKey  = "orderbook:buy:" + ticker;
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            Tuple minPriceBuyOrder = jedis.zrangeWithScores(buyKey, 0, 0).getFirst();
            System.out.println(minPriceBuyOrder);
        }

        return true;
    }

}

