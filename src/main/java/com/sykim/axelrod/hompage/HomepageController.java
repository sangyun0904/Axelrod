package com.sykim.axelrod.hompage;

import com.sykim.axelrod.StockTradeService;
import com.sykim.axelrod.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/homepage")
public class HomepageController {

    @Autowired
    HomepageService homepageService;
    @Autowired
    StockTradeService stockTradeService;

    @GetMapping("")
    public String mainPage(Model model) {
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
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
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
        return "homePage";
    }
}
