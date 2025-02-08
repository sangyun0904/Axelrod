package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

}
