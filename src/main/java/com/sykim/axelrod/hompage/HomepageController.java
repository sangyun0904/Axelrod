package com.sykim.axelrod.hompage;

import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.UserService;
import com.sykim.axelrod.model.Player;
import com.sykim.axelrod.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
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

    private Player user = new Player();

    @GetMapping("")
    public String mainPage(Model model) {
        return renderHomePage(model);
    }

    @GetMapping("/create")
    public String createStockForm(Model model) {
        model.addAttribute("stock", new Stock());
        return "createStock";
    }

    @PostMapping("/create")
    public String createStockResult(@ModelAttribute Stock.StockCreate stock, Model model) {
        stockTradeService.createStock(stock);
        return renderHomePage(model);
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
        return renderHomePage(model);
    }

    @PostMapping("/login")
    public String loginStockResult(@ModelAttribute Player.PlayerLogin login, Model model) {
        try {
            user = userService.loginPlayer(login);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return renderHomePage(model);
    }

    @GetMapping("/logout")
    public String logoutStockResult(Model model) {
        user = new Player();
        return renderHomePage(model);
    }

    private String renderHomePage(Model model) {
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
        List<Player> playerList = homepageService.getAllPlayer();
        model.addAttribute("players", playerList);
        model.addAttribute("user", user);
        System.out.println(user.getUsername());
        return "homePage";
    }
}
