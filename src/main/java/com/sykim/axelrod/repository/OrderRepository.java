package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.TransactionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<TransactionOrder, Long> {
}
