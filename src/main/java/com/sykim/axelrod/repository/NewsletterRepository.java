package com.sykim.axelrod.repository;

import com.sykim.axelrod.model.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    List<Newsletter> findNewsletterByPostedAt(LocalDate now);
}
