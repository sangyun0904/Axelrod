package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Newsletter {
    private Long id;
    private String title;
    private String content;
    private LocalDate postedAt;
}
