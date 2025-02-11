package com.sykim.axelrod.hompage;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sykim.axelrod.*;
import com.sykim.axelrod.exceptions.AccountDoseNotExistException;
import com.sykim.axelrod.exceptions.NotAvailableTickerException;
import com.sykim.axelrod.exceptions.NotEnoughBalanceException;
import com.sykim.axelrod.matching.MatchingService;
import com.sykim.axelrod.matching.TransactionOrderListComponent;
import com.sykim.axelrod.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/homepage")
public class HomepageController {

    @Autowired
    private HomepageService homepageService;
    @Autowired
    private StockTradeService stockTradeService;
    @Autowired
    private UserService userService;
    @Autowired
    private MatchingService matchingService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionOrderListComponent transactionOrderListComponent;
    @Autowired
    private AlphaVantageService alphaVantageService;
    @Autowired
    private NewsletterService newsletterService;

    @Value("${alphavantage.key}")
    private String ALPHA_VANTAGE_API_KEY;
    private final int PAGE_SIZE=15;

    @GetMapping("")
    public String mainPage(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam("stockPage") Optional<Integer> stockPageNum,
            @RequestParam("pfPage") Optional<Integer> pfPageNum,
            Model model) throws IOException {

        int stockListCurrentPage = stockPageNum.orElse(1);
        Page<Stock> stockPage = homepageService.getStockPaginated(PageRequest.of(stockListCurrentPage - 1, PAGE_SIZE));
        model.addAttribute("stocks", stockPage);
        int lastPage = stockPage.getTotalPages();
        if (lastPage > 0) {
            List<Integer> pageNumbers = getPageNumbers(stockListCurrentPage, lastPage);
            model.addAttribute("stockPageNumbers", pageNumbers);
        }
        model.addAttribute("stockCurrentPage", stockListCurrentPage);

        int portfolioCurrentPage = pfPageNum.orElse(1);
        Page<Portfolio.PortfolioReport> portfolioPage = homepageService.getPortfolioReportPaginated(PageRequest.of(portfolioCurrentPage - 1, PAGE_SIZE), userId);
        model.addAttribute("portfolios", portfolioPage);
        lastPage = portfolioPage.getTotalPages();
        if (lastPage > 0) {
            List<Integer> pageNumbers = getPageNumbers(portfolioCurrentPage, lastPage);
            model.addAttribute("portfolioPageNumbers", pageNumbers);
        }
        model.addAttribute("pfCurrentPage", portfolioCurrentPage);

        List<Player> playerList = homepageService.getAllPlayer();
        model.addAttribute("players", playerList);

        model.addAttribute("userId", userId);
        model.addAttribute("user", new Player());

        if (userId != null) model.addAttribute("accounts", accountService.getAccountByUsername(userId));
        else model.addAttribute("accounts", accountService.getAllAccounts());

        // TODO : NASDAQ 주식 전부 돌면서 orderbook:buy, orderbook:sell 주문 리스트 가져오는 시간 줄이기 현재 1.5초 ~ 2초
        // Mac에선 원래 0.7초
        // => order list를 가지고 있는 bean 객체를 따로 생성하여 매번 화면이 랜더링 될때마다 거래 주문 리스트를 전부 긁어오는 시간을 줄임
        // 바뀐후 시간 : Mac 에서 0.1초
        List<TransactionOrder> buyOrderList = transactionOrderListComponent.buyOrderList;
        List<TransactionOrder> sellOrderList = transactionOrderListComponent.sellOrderList;
        model.addAttribute("buyOrderList", buyOrderList.subList(0, Math.min(buyOrderList.size(), 15)));
        model.addAttribute("sellOrderList", sellOrderList.subList(0, Math.min(sellOrderList.size(), 15)));

        List<Newsletter> newsList = newsletterService.getNewYorkTimesLetters();
        model.addAttribute("newslist", newsList);

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
    public String buyStockForm(@RequestParam(value = "ticker") String ticker, @RequestParam(value = "userId") String userId, Model model) {
        model.addAttribute("ticker", ticker);
        model.addAttribute("userId", userId);
        model.addAttribute("order", new TransactionOrder());
        model.addAttribute("type", "Buy");
        return "orderStock";
    }

    @PostMapping("/Buy")
    public String buyStockResult(@ModelAttribute TransactionOrder.OrderRequest order, Model model) throws NotAvailableTickerException, NotEnoughBalanceException {
        //TODO: Account Balance 확인
        accountService.checkAccountBalance(order);
        stockTradeService.isAllowedToMakeOrder(order, TransactionOrder.Type.BUY);
        TransactionOrder newTransactionOrder = stockTradeService.createTransactionOrder(order, TransactionOrder.Type.BUY);
        matchingService.bookStockOrder(newTransactionOrder.getId(), order.playerId(), order.ticker(), TransactionOrder.Type.BUY, order.price(), order.quantity());
        return "redirect:/homepage?userId=" + order.playerId();
    }

    @GetMapping("/sell")
    public String sellStockForm(@RequestParam(value = "ticker") String ticker, @RequestParam(value = "userId") String userId, Model model) {
        model.addAttribute("ticker", ticker);
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
    public String stockDetail(@RequestParam("ticker") String ticker,@RequestParam("userId") Optional<String> userId, Model model) throws IOException, CsvValidationException {
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
        dataList = alphaVantageService.getStockData(ticker);
        model.addAttribute("chartData", dataList);
        if (userId.isPresent()) {
            System.out.println("userId : " + userId.get());
            model.addAttribute("userId", userId.get());
        }
        model.addAttribute("ticker", ticker);

        return "stockPage";
    }

    @GetMapping("/create/account")
    public String createAccount(@RequestParam("userId") String userId, Model model) {
        model.addAttribute("userId", userId);
        List<Bank> bankList = accountService.getAllBanksList();
        model.addAttribute("banks", bankList.subList(0, 100));
        model.addAttribute("createAccount", new Account.CreateAccount("", ""));
        return "createAccount";
    }

    @PostMapping("/create/account")
    public String createAccountResult(@ModelAttribute Account.CreateAccount createAccount, Model model) {
        Account account = accountService.createAccount(createAccount);
        return "redirect:/homepage?userId=" + createAccount.playerId();
    }

    @GetMapping("/deposit")
    public String depositMoney(@RequestParam("userId") String userId, @RequestParam("accountNum") String accountNum, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("accountNum", accountNum);
        model.addAttribute("changeBalance", new Account.ChangeBalance("", "", 1, 0d));
        model.addAttribute("type", "deposit");
        return "changeBalance";
    }

    @PostMapping("/deposit")
    public String depositMoneyResult(@ModelAttribute Account.ChangeBalance changeBalance, Model model) throws NotEnoughBalanceException, AccountDoseNotExistException {
        System.out.println(changeBalance);
        accountService.changeAccountBalance(changeBalance.accountNum(), changeBalance.amount() * changeBalance.type());
        return "redirect:/homepage?userId=" + changeBalance.userId();
    }

    @GetMapping("/withdrawal")
    public String withdrawMoney(@RequestParam("userId") String userId, @RequestParam("accountNum") String accountNum, Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("accountNum", accountNum);
        model.addAttribute("changeBalance", new Account.ChangeBalance("", "", -1, 0d));
        model.addAttribute("type", "withdrawal");
        return "changeBalance";
    }

    @PostMapping("/withdrawal")
    public String withdrawMoneyResult(@ModelAttribute Account.ChangeBalance changeBalance, Model model) throws NotEnoughBalanceException, AccountDoseNotExistException {
        System.out.println(changeBalance);
        accountService.changeAccountBalance(changeBalance.accountNum(), changeBalance.amount() * changeBalance.type());
        return "redirect:/homepage?userId=" + changeBalance.userId();
    }

    private List<Integer> getPageNumbers(int currentPage, int lastPage) {
        List<Integer> pageNumbers;

        if (currentPage < 5) {
            pageNumbers = IntStream.rangeClosed(1, Math.min(10, lastPage))
                    .boxed()
                    .collect(Collectors.toList());
        }
        else {
            pageNumbers = IntStream.rangeClosed(currentPage-4, Math.min(currentPage + 5, lastPage))
                    .boxed()
                    .collect(Collectors.toList());
        }

        return pageNumbers;
    }

}
