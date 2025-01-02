package com.sykim.axelrod;

import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class AxelrodApplication {

	public static void main(String[] args) {
		SpringApplication.run(AxelrodApplication.class, args);
	}
}
