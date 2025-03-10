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

    public Map<String, Map<String,SortedSet<TransactionOrder>>> buyOrderMapByUserId = new HashMap<>();
    public Map<String, Map<String, SortedSet<TransactionOrder>>> sellOrderMapByUserId = new HashMap<>();

    TransactionOrderListComponent(StockRepository stockRepository, JedisPool jedisPool) {
        this.stockRepository = stockRepository;
        this.jedisPool = jedisPool;

        reloadOrderData();
    }

    public void reloadOrderData() {

        List<Stock> stockList = stockRepository.findAll();

        for (Stock stock : stockList) {
            Gson gson = new Gson();
            try (Jedis jedis = jedisPool.getResource()) {
                List<Tuple> buyOrderTupleList = jedis.zrangeWithScores("orderbook:buy:" + stock.getTicker(), 0, -1);
                for (Tuple order : buyOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    TransactionOrder buyOrder = new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.BUY);
                    if (!buyOrderMapByUserId.containsKey(buyOrder.getPlayerId())) buyOrderMapByUserId.put(buyOrder.getPlayerId(), new HashMap<>());
                    if (!buyOrderMapByUserId.get(buyOrder.getPlayerId()).containsKey(stock.getTicker())) buyOrderMapByUserId.get(buyOrder.getPlayerId()).put(stock.getTicker(), new TreeSet<>());
                    buyOrderMapByUserId.get(buyOrder.getPlayerId()).get(stock.getTicker()).add(buyOrder);
                }
                List<Tuple> sellOrderTupleList = jedis.zrangeWithScores("orderbook:sell:" + stock.getTicker(), 0, -1);
                for (Tuple order : sellOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    TransactionOrder sellOrder = new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.SELL);
                    if (!sellOrderMapByUserId.containsKey(sellOrder.getPlayerId())) sellOrderMapByUserId.put(sellOrder.getPlayerId(), new HashMap<>());
                    if (!sellOrderMapByUserId.get(sellOrder.getPlayerId()).containsKey(stock.getTicker())) sellOrderMapByUserId.get(sellOrder.getPlayerId()).put(stock.getTicker(), new TreeSet<>());
                    sellOrderMapByUserId.get(sellOrder.getPlayerId()).get(stock.getTicker()).add(sellOrder);
                }
            }
        }
    }

    public void updateTransaction(String ticker, Double transactionPrice, Long quantity, String sellerId, String buyerId) {
        TransactionOrder buyOrder = buyOrderMapByUserId.get(buyerId).get(ticker).removeLast();
        if (buyOrder.getQuantity() > quantity) {
            buyOrderMapByUserId.get(buyerId).get(ticker).add(new TransactionOrder(null, buyerId, ticker, buyOrder.getQuantity() - quantity, buyOrder.getPrice(), TransactionOrder.Type.BUY));
        }
        TransactionOrder sellOrder = sellOrderMapByUserId.get(sellerId).get(ticker).removeFirst();
        if (sellOrder.getQuantity() > quantity) {
            sellOrderMapByUserId.get(sellerId).get(ticker).add(new TransactionOrder(null, sellerId, ticker, sellOrder.getQuantity() - quantity, sellOrder.getPrice(), TransactionOrder.Type.SELL));
        }
    }
}
