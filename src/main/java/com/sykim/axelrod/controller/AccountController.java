package com.sykim.axelrod.controller;

import com.sykim.axelrod.AccountService;
import com.sykim.axelrod.exceptions.AccountDoseNotExistException;
import com.sykim.axelrod.exceptions.NotEnoughBalanceException;
import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.model.TransactionOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/check")
    public ResponseEntity<Boolean> checkOrderBalance(@RequestBody TransactionOrder.OrderRequest order) throws NotEnoughBalanceException, AccountDoseNotExistException {
        System.out.println(order);
        return ResponseEntity.ok(accountService.checkOrderPossible(order));
    }

    @PostMapping("/remain")
    public ResponseEntity<Double> getRemainBalance(@RequestBody Account.RemainBalanceCheck request) {
        System.out.println("userId : " + request.userId());
        return ResponseEntity.ok(accountService.getRemainBalance(request.userId()));
    }
}
