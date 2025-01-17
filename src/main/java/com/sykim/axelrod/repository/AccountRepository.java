package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUsername(String username);

    Optional<Account> findByAccountNum(String accountNum);
}
