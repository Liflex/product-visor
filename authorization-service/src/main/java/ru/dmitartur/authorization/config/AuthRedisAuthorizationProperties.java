package ru.dmitartur.authorization.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth.redis-authorization")
/**
 * Настройки Redis-кэша авторизаций:
 * - enabled: включение кэширования поверх JDBC
 * - accessTtlSeconds / refreshTtlSeconds / idTtlSeconds: TTL ключей индексов и самих авторизаций
 */
public class AuthRedisAuthorizationProperties {
    private boolean enabled = false;
    private long accessTtlSeconds = 3600;
    private long refreshTtlSeconds = 2592000; // 30d
    private long idTtlSeconds = 3600;
}


