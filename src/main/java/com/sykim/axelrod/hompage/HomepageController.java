package com.sykim.axelrod.hompage;

import com.google.gson.Gson;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.UserService;
import com.sykim.axelrod.config.RedisConfig;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.model.Stock;
import com.sykim.axelrod.model.Transaction;
import com.sykim.axelrod.model.TransactionOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.resps.Tuple;

import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/homepage")
public class HomepageController {

    @Autowired
    HomepageService homepageService;
    @Autowired
    StockTradeService stockTradeService;
    @Autowired
    UserService userService;
    @Autowired
    MatchingService matchingService;

    @Autowired
    JedisPool jedisPool;

    private Player user = new Player();

    @GetMapping("")
    public String mainPage(Model model) {
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
        List<Player> playerList = homepageService.getAllPlayer();
        model.addAttribute("players", playerList);
        model.addAttribute("user", user);
        System.out.println(user.getUsername());

        List<TransactionOrder> buyOrderList = new ArrayList<>();
        List<TransactionOrder> sellOrderList = new ArrayList<>();
        for (Stock stock: stockList) {
            Gson gson = new Gson();
            try (Jedis jedis = jedisPool.getResource()) {
                List<Tuple> buyOrderTupleList = jedis.zrangeWithScores("orderbook:buy:" + stock.getTicker(), 0, -1);
                for (Tuple order: buyOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    buyOrderList.add(new TransactionOrder(null, user.getUsername(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.BUY));
                }
                List<Tuple> sellOrderTupleList = jedis.zrangeWithScores("orderbook:sell:" + stock.getTicker(), 0, -1);
                for (Tuple order: sellOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    sellOrderList.add(new TransactionOrder(null, user.getUsername(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.SELL));
                }
            }
        }

        model.addAttribute("buyOrderList", buyOrderList);
        model.addAttribute("sellOrderList", sellOrderList);
        return "homePage";
    }

    @GetMapping("/create")
    public String createStockForm(Model model) {
        model.addAttribute("stock", new Stock());
        return "createStock";
    }

    @PostMapping("/create")
    public String createStockResult(@ModelAttribute Stock.StockCreate stock, Model model) {
        stockTradeService.createStock(stock);
        return "redirect:/homepage";
    }

    @GetMapping("/player/create")
    public String createPlayerForm(Model model) {
        model.addAttribute("player", new Player());
        return "createPlayer";
    }

    @PostMapping("/player/create")
    public String createPlayerResult(@ModelAttribute Player.PlayerCreate player, Model model) {
        Player newPlayer = Player.builder()
                .username(player.username())
                .email(player.email())
                .name(player.name())
                .password(player.password())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        homepageService.createPlayer(newPlayer);
        return "redirect:/homepage";
    }

    @PostMapping("/login")
    public String loginStockResult(@ModelAttribute Player.PlayerLogin login, Model model) {
        try {
            user = userService.loginPlayer(login);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/homepage";
    }

    @GetMapping("/logout")
    public String logoutStockResult(Model model) {
        user = new Player();
        return "redirect:/homepage";
    }

    @GetMapping("/buy")
    public String buyStockForm(Model model) {
        model.addAttribute("order", new Transaction());
        model.addAttribute("type", "Buy");
        return "orderStock";
    }

    @PostMapping("/Buy")
    public String buyStockResult(@ModelAttribute TransactionOrder.WebOrderRequest order, Model model) throws SQLDataException {
        TransactionOrder.OrderRequest request = new TransactionOrder.OrderRequest(user.getUsername(), order.ticker(), order.quantity(), order.price());
        stockTradeService.createTransactionOrder(request, TransactionOrder.Type.BUY);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(request, TransactionOrder.Type.BUY);
        matchingService.bookStockOrder(newTransactionOrder.getId(), user.getUsername(), order.ticker(), TransactionOrder.Type.BUY, order.price(), order.quantity());
        return "redirect:/homepage";
    }

    @GetMapping("/sell")
    public String sellStockForm(Model model) {
        model.addAttribute("order", new Transaction());
        model.addAttribute("type", "Sell");
        return "orderStock";
    }

    @PostMapping("/Sell")
    public String sellStockResult(@ModelAttribute TransactionOrder.WebOrderRequest order, Model model) throws SQLDataException {
        TransactionOrder.OrderRequest request = new TransactionOrder.OrderRequest(user.getUsername(), order.ticker(), order.quantity(), order.price());
        stockTradeService.createTransactionOrder(request, TransactionOrder.Type.SELL);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(request, TransactionOrder.Type.SELL);
        matchingService.bookStockOrder(newTransactionOrder.getId(), user.getUsername(), order.ticker(), TransactionOrder.Type.SELL, order.price(), order.quantity());
        return "redirect:/homepage";
    }

}
