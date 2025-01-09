package com.sykim.axelrod.hompage;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.UserService;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLDataException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @GetMapping("")
    public String mainPage(@RequestParam(name = "userId", required = false) String userId, Model model) {
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
        List<Player> playerList = homepageService.getAllPlayer();
        model.addAttribute("players", playerList);

        List<Portfolio> userPFList = stockTradeService.getPlayerPortfolio(userId);
        model.addAttribute("userId", userId);
        model.addAttribute("user", new Player());
        model.addAttribute("portfolios", userPFList);

        List<TransactionOrder> buyOrderList = new ArrayList<>();
        List<TransactionOrder> sellOrderList = new ArrayList<>();
        for (Stock stock: stockList) {
            Gson gson = new Gson();
            try (Jedis jedis = jedisPool.getResource()) {
                List<Tuple> buyOrderTupleList = jedis.zrangeWithScores("orderbook:buy:" + stock.getTicker(), 0, -1);
                for (Tuple order: buyOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    buyOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.BUY));
                }
                List<Tuple> sellOrderTupleList = jedis.zrangeWithScores("orderbook:sell:" + stock.getTicker(), 0, -1);
                for (Tuple order: sellOrderTupleList) {
                    Transaction.RedisOrder redisOrderElement = gson.fromJson(order.getElement(), Transaction.RedisOrder.class);
                    sellOrderList.add(new TransactionOrder(null, redisOrderElement.userId(), stock.getTicker(), redisOrderElement.quantity(), order.getScore(), TransactionOrder.Type.SELL));
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
        String userId = "";

        try {
            userService.loginPlayer(login);
            return "redirect:/homepage?userId=" + login.username();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "redirect:/homepage";
    }

    @GetMapping("/logout")
    public String logoutStockResult(Model model) {
        return "redirect:/homepage";
    }

    @GetMapping("/buy")
    public String buyStockForm(@RequestParam(value = "userId") String userId, Model model) throws IOException, CsvValidationException {
        model.addAttribute("userId", userId);
        model.addAttribute("order", new TransactionOrder());
        model.addAttribute("type", "Buy");

        List<Stock.Diamond> dataList = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                Objects.requireNonNull(classLoader.getResource("data/diamonds.csv").getFile())
        );
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);

        csvReader.readNext();
        String[] record;
        while ((record = csvReader.readNext()) != null) {
            dataList.add(new Stock.Diamond(Double.parseDouble(record[0]), Integer.parseInt(record[1])));
        }

        model.addAttribute("chartData", dataList);


        return "orderStock";
    }

    @PostMapping("/Buy")
    public String buyStockResult(@ModelAttribute TransactionOrder.OrderRequest order, Model model) throws SQLDataException {
        stockTradeService.createTransactionOrder(order, TransactionOrder.Type.BUY);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(order, TransactionOrder.Type.BUY);
        matchingService.bookStockOrder(newTransactionOrder.getId(), order.playerId(), order.ticker(), TransactionOrder.Type.BUY, order.price(), order.quantity());
        return "redirect:/homepage?userId=" + order.playerId();
    }

    @GetMapping("/sell")
    public String sellStockForm(@RequestParam(value = "userId") String userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("order", new TransactionOrder());
        model.addAttribute("type", "Sell");
        return "orderStock";
    }

    @PostMapping("/Sell")
    public String sellStockResult(@ModelAttribute TransactionOrder.OrderRequest order, Model model) throws SQLDataException {
        stockTradeService.createTransactionOrder(order, TransactionOrder.Type.SELL);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(order, TransactionOrder.Type.SELL);
        matchingService.bookStockOrder(newTransactionOrder.getId(), order.playerId(), order.ticker(), TransactionOrder.Type.SELL, order.price(), order.quantity());
        return "redirect:/homepage?userId=" + order.playerId();
    }

}
