package com.sykim.axelrod.repository;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentTooLongException;
import com.sykim.axelrod.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, ContentTooLongException> {
}
