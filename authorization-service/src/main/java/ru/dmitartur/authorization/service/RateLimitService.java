package ru.dmitartur.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final int DEFAULT_MAX_REQUESTS = 100; // Максимум запросов
    private static final Duration DEFAULT_WINDOW = Duration.ofMinutes(1); // Окно времени

    /**
     * Проверяет rate limit для пользователя
     * @param username имя пользователя
     * @return true если запрос разрешен, false если превышен лимит
     */
    public boolean checkRateLimit(String username) {
        return checkRateLimit(username, DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW);
    }

    /**
     * Проверяет rate limit для пользователя с кастомными параметрами
     * @param username имя пользователя
     * @param maxRequests максимальное количество запросов
     * @param window окно времени
     * @return true если запрос разрешен, false если превышен лимит
     */
    public boolean checkRateLimit(String username, int maxRequests, Duration window) {
        String key = RATE_LIMIT_KEY_PREFIX + username;
        
        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;
        
        if (count >= maxRequests) {
            log.warn("Rate limit exceeded for user: {}", username);
            return false;
        }
        
        // Увеличиваем счетчик
        redisTemplate.opsForValue().increment(key);
        
        // Устанавливаем TTL если ключ новый
        if (currentCount == null) {
            redisTemplate.expire(key, window.toSeconds(), TimeUnit.SECONDS);
        }
        
        log.debug("Rate limit check passed for user: {}, count: {}", username, count + 1);
        return true;
    }

    /**
     * Получает текущий счетчик запросов для пользователя
     * @param username имя пользователя
     * @return количество запросов
     */
    public int getCurrentCount(String username) {
        String key = RATE_LIMIT_KEY_PREFIX + username;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    /**
     * Сбрасывает rate limit для пользователя
     * @param username имя пользователя
     */
    public void resetRateLimit(String username) {
        String key = RATE_LIMIT_KEY_PREFIX + username;
        redisTemplate.delete(key);
        log.info("Rate limit reset for user: {}", username);
    }
} 
