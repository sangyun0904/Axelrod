package com.sykim.axelrod.hompage;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.AccountService;
import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.UserService;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    AccountService accountService;

    @Autowired
    JedisPool jedisPool;

    @GetMapping("")
    public String mainPage(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            Model model) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(15);
        System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());

        Page<Stock> stockPage = homepageService.findStockPaginated(PageRequest.of(currentPage - 1, pageSize));
        model.addAttribute("stocks", stockPage);

        int totalPages = stockPage.getTotalPages();
        System.out.println("total pages : " + totalPages);
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(currentPage, currentPage + 10)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        List<Stock> stockList = homepageService.getAllStocks();
        List<Player> playerList = homepageService.getAllPlayer();
        model.addAttribute("players", playerList);

        List<Portfolio> userPFList = stockTradeService.getPlayerPortfolio(userId);
        model.addAttribute("userId", userId);
        model.addAttribute("user", new Player());
        model.addAttribute("portfolios", userPFList);

        if (userId != null) model.addAttribute("accounts", accountService.getAccountByUsername(userId));
        else model.addAttribute("accounts", accountService.getAllAccounts());

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

        model.addAttribute("buyOrderList", buyOrderList.subList(0, Math.min(buyOrderList.size(), 15)));
        model.addAttribute("sellOrderList", sellOrderList.subList(0, Math.min(buyOrderList.size(), 15)));

        System.out.println(Thread.currentThread().getName() + " : " + LocalDateTime.now());
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
    public String buyStockForm(@RequestParam(value = "userId") String userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("order", new TransactionOrder());
        model.addAttribute("type", "Buy");
        return "orderStock";
    }

    @PostMapping("/Buy")
    public String buyStockResult(@ModelAttribute TransactionOrder.OrderRequest order, Model model) throws NotAvailableTickerException {
        stockTradeService.isAllowedToMakeOrder(order, TransactionOrder.Type.BUY);
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
    public String sellStockResult(@ModelAttribute TransactionOrder.OrderRequest order, Model model) throws NotAvailableTickerException {
        stockTradeService.isAllowedToMakeOrder(order, TransactionOrder.Type.SELL);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(order, TransactionOrder.Type.SELL);
        matchingService.bookStockOrder(newTransactionOrder.getId(), order.playerId(), order.ticker(), TransactionOrder.Type.SELL, order.price(), order.quantity());
        return "redirect:/homepage?userId=" + order.playerId();
    }

    @GetMapping("/stock")
    public String stockDetail(@RequestParam("ticker") Optional<String> ticker, Model model) throws IOException, CsvValidationException {
        List<Stock.History> dataList = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(
                Objects.requireNonNull(classLoader.getResource("data/aapl-2.csv").getFile())
        );
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);

        csvReader.readNext();
        String[] record;
        while ((record = csvReader.readNext()) != null) {
            dataList.add(new Stock.History(record[0]
                    , Double.parseDouble(record[1])
                    , Double.parseDouble(record[2])
                    , Double.parseDouble(record[3])
                    , Double.parseDouble(record[4])
                    , Double.parseDouble(record[5])
                    , Long.parseLong(record[6])));
        }
        model.addAttribute("chartData", dataList);
        return "stockPage";
    }

    @GetMapping("/create/account")
    public String createAccount(@RequestParam("userId") String userId, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("Banks", accountService.getAllBanksList());

        return "createAccount";
    }

}
