package com.sykim.axelrod;

import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class AxelrodApplication {

	private static AccountRepository accountRepository;

	@Autowired
	public AxelrodApplication(AccountRepository accountRepository) {
		AxelrodApplication.accountRepository = accountRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(AxelrodApplication.class, args);

		List<Account> accountList = accountRepository.findAll();

		if (accountList.isEmpty()) {
			Account account = Account.builder()
					.balance(1e7d)
					.build();

			Account savedAccount = accountRepository.save(account);
		}
	}
}
