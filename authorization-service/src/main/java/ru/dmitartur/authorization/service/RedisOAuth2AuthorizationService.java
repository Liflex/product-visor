package ru.dmitartur.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import ru.dmitartur.authorization.config.AuthRedisAuthorizationProperties;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "auth.redis-authorization", name = "enabled", havingValue = "true")
/**
 * Опциональный кэш OAuth2Authorization в Redis.
 * Кэширует:
 * - саму авторизацию по id (oauth2:authorization:<id>)
 * - индексы access/refresh токенов -> id авторизации
 * ТТL настраиваются через AuthRedisAuthorizationProperties.
 */
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final RedisTemplate<String, OAuth2Authorization> redisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final AuthRedisAuthorizationProperties props;

    private static final String OAUTH2_AUTHORIZATION_KEY_PREFIX = "oauth2:authorization:";
    private static final String OAUTH2_AUTHORIZATION_ACCESS_TOKEN_KEY_PREFIX = "oauth2:authorization:access_token:";
    private static final String OAUTH2_AUTHORIZATION_REFRESH_TOKEN_KEY_PREFIX = "oauth2:authorization:refresh_token:";

    @Override
    public void save(OAuth2Authorization authorization) {
        String id = authorization.getId();
        String key = OAUTH2_AUTHORIZATION_KEY_PREFIX + id;
        
        redisTemplate.opsForValue().set(key, authorization, Duration.ofSeconds(props.getIdTtlSeconds()));
        
        // Сохраняем индексы для быстрого поиска
        OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
        if (accessToken != null) {
            String accessTokenKey = OAUTH2_AUTHORIZATION_ACCESS_TOKEN_KEY_PREFIX + 
                    accessToken.getTokenValue();
            stringRedisTemplate.opsForValue().set(accessTokenKey, id, Duration.ofSeconds(props.getAccessTtlSeconds()));
        }

        OAuth2RefreshToken refreshToken = authorization.getRefreshToken() != null ? authorization.getRefreshToken().getToken() : null;
        if (refreshToken != null) {
            String refreshTokenKey = OAUTH2_AUTHORIZATION_REFRESH_TOKEN_KEY_PREFIX + 
                    refreshToken.getTokenValue();
            stringRedisTemplate.opsForValue().set(refreshTokenKey, id, Duration.ofSeconds(props.getRefreshTtlSeconds()));
        }
        
        log.info("Saved OAuth2 authorization: {}", id);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        String id = authorization.getId();
        String key = OAUTH2_AUTHORIZATION_KEY_PREFIX + id;
        
        redisTemplate.delete(key);
        
        // Удаляем индексы
        OAuth2AccessToken accessToken = authorization.getAccessToken().getToken();
        if (accessToken != null) {
            String accessTokenKey = OAUTH2_AUTHORIZATION_ACCESS_TOKEN_KEY_PREFIX + 
                    accessToken.getTokenValue();
            stringRedisTemplate.delete(accessTokenKey);
        }

        OAuth2RefreshToken refreshToken = authorization.getRefreshToken() != null ? authorization.getRefreshToken().getToken() : null;
        if (refreshToken != null) {
            String refreshTokenKey = OAUTH2_AUTHORIZATION_REFRESH_TOKEN_KEY_PREFIX + 
                    refreshToken.getTokenValue();
            stringRedisTemplate.delete(refreshTokenKey);
        }
        
        log.info("Removed OAuth2 authorization: {}", id);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        String key = OAUTH2_AUTHORIZATION_KEY_PREFIX + id;
        OAuth2Authorization authorization = redisTemplate.opsForValue().get(key);
        
        if (authorization != null) {
            log.debug("Found OAuth2 authorization by id: {}", id);
        }
        
        return authorization;
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        String key;
        
        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            key = OAUTH2_AUTHORIZATION_ACCESS_TOKEN_KEY_PREFIX + token;
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            key = OAUTH2_AUTHORIZATION_REFRESH_TOKEN_KEY_PREFIX + token;
        } else {
            return null;
        }
        
        String authorizationId = stringRedisTemplate.opsForValue().get(key);
        if (authorizationId != null) {
            return findById(authorizationId);
        }
        
        return null;
    }
} 