package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
    Bank findByName(String bankName);
}
