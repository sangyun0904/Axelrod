package com.sykim.axelrod;

import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class AxelrodApplication {

	public static void main(String[] args) {
		SpringApplication.run(AxelrodApplication.class, args);
	}
}
