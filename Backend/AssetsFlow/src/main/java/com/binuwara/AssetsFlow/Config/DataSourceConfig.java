package com.binuwara.AssetsFlow.Config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Configuration(proxyBeanMethods = false)
public class DataSourceConfig {

    private static final String DEFAULT_DATABASE_URL = "jdbc:postgresql://localhost:5432/AssetsFlow";
    private static final String DEFAULT_DATABASE_USERNAME = "postgres";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    @Bean
    public DataSource dataSource(Environment environment) {
        Properties localEnvironment = loadLocalEnvironment();
        String password = firstAvailable(
                localEnvironment.getProperty("DB_PASSWORD"),
                environment.getProperty("DB_PASSWORD")
        );

        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "DB_PASSWORD must be provided through the environment or .env file."
            );
        }

        return DataSourceBuilder.create()
                .driverClassName(environment.getProperty(
                        "spring.datasource.driver-class-name",
                        POSTGRES_DRIVER
                ))
                .url(firstAvailable(
                        localEnvironment.getProperty("DB_URL"),
                        environment.getProperty("spring.datasource.url"),
                        DEFAULT_DATABASE_URL
                ))
                .username(firstAvailable(
                        localEnvironment.getProperty("DB_USERNAME"),
                        environment.getProperty("spring.datasource.username"),
                        DEFAULT_DATABASE_USERNAME
                ))
                .password(removeOptionalWrappingQuotes(password))
                .build();
    }

    private Properties loadLocalEnvironment() {
        Path environmentFile = Path.of(".env");
        Properties properties = new Properties();

        if (!Files.isRegularFile(environmentFile)) {
            return properties;
        }

        try (Reader reader = Files.newBufferedReader(environmentFile, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the .env file.", exception);
        }
    }

    private String firstAvailable(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String removeOptionalWrappingQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        boolean singleQuoted = first == '\'' && last == '\'';
        boolean doubleQuoted = first == '"' && last == '"';

        return singleQuoted || doubleQuoted
                ? value.substring(1, value.length() - 1)
                : value;
    }
}
