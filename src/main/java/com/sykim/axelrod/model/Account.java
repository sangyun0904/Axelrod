package com.sykim.axelrod.model;

import com.sykim.axelrod.exceptions.NotEnoughBalanceException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Double balance;
    private String username;
    private String accountNum;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public record CreateAccount(String playerId, String bankName) {}

    public record ChangeBalance(String accountNum, Integer type, Double amount) {}

    public Double changeBalance(Double change) throws NotEnoughBalanceException {
        if (this.balance + change < 0)
            throw new NotEnoughBalanceException("Not Enough Money in the account to withdraw " + change * (-1) + " $");

        this.balance = this.balance + change;
        return this.balance;
    }
}
