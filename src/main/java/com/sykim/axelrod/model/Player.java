package com.sykim.axelrod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    private String username;
    private String name;
    private String email;
    private String password;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
