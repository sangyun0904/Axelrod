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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Component
public class TransactionOrderListComponent {

    private StockRepository stockRepository;

    private JedisPool jedisPool;

    public SortedSet<TransactionOrder> buyOrderList = new TreeSet<>();
    public SortedSet<TransactionOrder> sellOrderList = new TreeSet<>();

    TransactionOrderListComponent(StockRepository stockRepository, JedisPool jedisPool) {
        this.stockRepository = stockRepository;
        this.jedisPool = jedisPool;

        reloadOrderData();
    }

    public void reloadOrderData() {
        SortedSet<TransactionOrder> newBuyOrderList = new TreeSet<>();
        SortedSet<TransactionOrder> newSellOrderList = new TreeSet<>();
        List<Stock> stockList = stockRepository.findAll();

        for (Stock stock : stockList) {
            Gson gson = new Gson();
            try (Jedis jedis = jedisPool.getResource()) {
                List<Tuple> buyOrderTupleList = jedis.zrangeWithScores("orderbook:buy:" + stock.getTicker(), 0, -1);
                for (Tuple order : buyOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    newBuyOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.BUY));
                }
                List<Tuple> sellOrderTupleList = jedis.zrangeWithScores("orderbook:sell:" + stock.getTicker(), 0, -1);
                for (Tuple order : sellOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    newSellOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.SELL));
                }
            }
        }

        buyOrderList = newBuyOrderList;
        sellOrderList = newSellOrderList;
    }
}
