package com.sykim.axelrod;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.sykim.axelrod.model.Account;
import com.sykim.axelrod.repository.AccountRepository;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableJpaRepositories(basePackageClasses = {AccountRepository.class})
@EntityScan(basePackageClasses = {Account.class})
public class EmbeddedPostgresConfig {

    private static EmbeddedPostgres embeddedPostgres;

    @Bean
    public DataSource dataSource() throws IOException {
        embeddedPostgres = EmbeddedPostgres.builder()
                .setImage(DockerImageName.parse("postgres:14.1"))
                .start();

        return embeddedPostgres.getPostgresDatabase();
    }

    public static class EmbeddedPostgresExtension implements AfterAllCallback {
        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            if (embeddedPostgres == null) {
                return;
            }
            embeddedPostgres.close();
        }
    }
}
