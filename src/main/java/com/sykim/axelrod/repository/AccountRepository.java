package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<Account, Long> {
}
