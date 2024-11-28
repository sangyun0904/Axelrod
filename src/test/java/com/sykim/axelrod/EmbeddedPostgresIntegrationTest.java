package com.sykim.axelrod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ExtendWith(EmbeddedPostgresConfig.EmbeddedPostgresExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {EmbeddedPostgresConfig.class})
public class EmbeddedPostgresIntegrationTest {
    @Autowired
    private AccountRepository accountRepository;

    @Test
    void TestInitAccount() {
        Account account = Account.builder()
                .balance(1000d)
                .build();

        Account savedAccount = accountRepository.save(account);
        assertNotNull(savedAccount);
        assertThat(account.getBalance()).isEqualTo(savedAccount.getBalance());
    }

}
