package com.sykim.axelrod.hompage;

import com.sykim.axelrod.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomepageController {

    @Autowired
    HomepageService homepageService;

    @GetMapping("/")
    public String mainPage(Model model) {
        List<Stock> stockList = homepageService.getAllStocks();
        model.addAttribute("stocks", stockList);
        return "homePage";
    }
}
