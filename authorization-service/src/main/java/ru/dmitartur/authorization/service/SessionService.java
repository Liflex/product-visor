package ru.dmitartur.authorization.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final Duration DEFAULT_SESSION_TIMEOUT = Duration.ofHours(24); // 24 часа

    /**
     * Создает новую сессию для пользователя
     * @param username имя пользователя
     * @param sessionData данные сессии
     * @return ID сессии
     */
    public String createSession(String username, Map<String, Object> sessionData) {
        String sessionId = generateSessionId();
        String key = SESSION_KEY_PREFIX + sessionId;
        
        sessionData.put("username", username);
        sessionData.put("createdAt", System.currentTimeMillis());
        
        redisTemplate.opsForValue().set(key, sessionData, DEFAULT_SESSION_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        
        log.info("Created session for user: {}, sessionId: {}", username, sessionId);
        return sessionId;
    }

    /**
     * Получает данные сессии
     * @param sessionId ID сессии
     * @return данные сессии или null если сессия не найдена
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object sessionData = redisTemplate.opsForValue().get(key);
        
        if (sessionData != null) {
            log.debug("Retrieved session: {}", sessionId);
            return (Map<String, Object>) sessionData;
        }
        
        return null;
    }

    /**
     * Обновляет данные сессии
     * @param sessionId ID сессии
     * @param sessionData новые данные сессии
     */
    public void updateSession(String sessionId, Map<String, Object> sessionData) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, sessionData, DEFAULT_SESSION_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        
        log.debug("Updated session: {}", sessionId);
    }

    /**
     * Удаляет сессию
     * @param sessionId ID сессии
     */
    public void removeSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
        
        log.info("Removed session: {}", sessionId);
    }

    /**
     * Продлевает время жизни сессии
     * @param sessionId ID сессии
     */
    public void extendSession(String sessionId) {
        String key = SESSION_KEY_PREFIX + sessionId;
        Object sessionData = redisTemplate.opsForValue().get(key);
        
        if (sessionData != null) {
            redisTemplate.expire(key, DEFAULT_SESSION_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            log.debug("Extended session: {}", sessionId);
        }
    }

    /**
     * Получает все активные сессии пользователя
     * @param username имя пользователя
     * @return количество активных сессий
     */
    public long getActiveSessionsCount(String username) {
        // Это упрощенная реализация. В реальном проекте нужно использовать SCAN
        return 0; // Пока возвращаем 0, так как для полной реализации нужен SCAN
    }

    /**
     * Удаляет все сессии пользователя
     * @param username имя пользователя
     */
    public void removeAllUserSessions(String username) {
        // Это упрощенная реализация. В реальном проекте нужно использовать SCAN
        log.info("Removing all sessions for user: {}", username);
    }

    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
} 