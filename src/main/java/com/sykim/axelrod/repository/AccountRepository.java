package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUsername(String username);

    Optional<Account> findByAccountNum(String accountNum);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNum = :accountNum")
    Optional<Account> findByAccountNumForUpdate(String accountNum);
}
