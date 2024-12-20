package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
