package com.sykim.axelrod.matching;

import com.google.gson.Gson;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Transaction;
import com.sykim.axelrod.model.TransactionOrder;
import com.sykim.axelrod.repository.StockRepository;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.util.*;

@Component
public class TransactionOrderListComponent {

    private StockRepository stockRepository;

    private JedisPool jedisPool;

    public Map<String, SortedSet<TransactionOrder>> buyOrderMap = new HashMap<>();
    public Map<String, SortedSet<TransactionOrder>> sellOrderMap = new HashMap<>();
    public List<TransactionOrder> buyOrderList = new ArrayList<>();
    public List<TransactionOrder> sellOrderList = new ArrayList<>();

    TransactionOrderListComponent(StockRepository stockRepository, JedisPool jedisPool) {
        this.stockRepository = stockRepository;
        this.jedisPool = jedisPool;

        reloadOrderData(null);
    }

    public void reloadOrderData(String playerId) {

        List<Stock> stockList = stockRepository.findAll();

        for (Stock stock : stockList) {
            Gson gson = new Gson();
            try (Jedis jedis = jedisPool.getResource()) {
                SortedSet<TransactionOrder> stockBuyOrderList = new TreeSet<>();
                SortedSet<TransactionOrder> stockSellOrderList = new TreeSet<>();
                List<Tuple> buyOrderTupleList = jedis.zrangeWithScores("orderbook:buy:" + stock.getTicker(), 0, -1);
                for (Tuple order : buyOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    if (playerId != null && !playerId.equals(redisOrderElement.userId())) continue;
                    stockBuyOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.BUY));
                }
                List<Tuple> sellOrderTupleList = jedis.zrangeWithScores("orderbook:sell:" + stock.getTicker(), 0, -1);
                for (Tuple order : sellOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    if (playerId != null && !playerId.equals(redisOrderElement.userId())) continue;
                    stockSellOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.SELL));
                }
                buyOrderMap.put(stock.getTicker(), stockBuyOrderList);
                sellOrderMap.put(stock.getTicker(), stockSellOrderList);
            }
        }

        buyOrderMap.values().forEach(set -> buyOrderList.addAll(set));
        sellOrderMap.values().forEach(set -> sellOrderList.addAll(set));
    }
}
