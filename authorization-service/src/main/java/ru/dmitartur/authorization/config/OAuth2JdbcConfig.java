package ru.dmitartur.authorization.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import javax.sql.DataSource;

@Configuration
/**
 * JDBC-хранилище компонентов Spring Authorization Server:
 * - RegisteredClientRepository: хранение OAuth2 клиентов в БД (таблица oauth2_registered_client)
 * - OAuth2AuthorizationService: хранение авторизаций/токенов (таблица oauth2_authorization)
 * - OAuth2AuthorizationConsentService: хранение consents (таблица oauth2_authorization_consent)
 */
public class OAuth2JdbcConfig {
//TODO
//    @Bean
//    @Primary
//    /**
//     * Репозиторий клиентов (primary), чтобы переопределить in-memory и сделать стойкое хранение в БД.
//     */
//    public RegisteredClientRepository registeredClientRepository(DataSource dataSource) {
//        return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
//    }

    @Bean
    @Primary
    /**
     * Сервис авторизаций/токенов (primary),
     * обеспечивает сохранение access/refresh/authorization-code и пр. в таблице oauth2_authorization.
     */
    public OAuth2AuthorizationService oAuth2AuthorizationService(DataSource dataSource, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), registeredClientRepository);
    }

    @Bean
    /**
     * Сервис consent’ов (optional), нужен для authorization_code и согласий на scopes.
     */
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(DataSource dataSource, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), registeredClientRepository);
    }
}


