package ru.dmitartur.common.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Базовый сервис retry для API
 * Предоставляет общие методы для работы с retry логикой
 */
@Slf4j
@Service
public abstract class BaseRetryService {
    
    protected final String serviceName;
    protected final BaseRetryPolicy retryPolicy;
    
    public BaseRetryService(String serviceName, BaseRetryPolicy retryPolicy) {
        this.serviceName = serviceName;
        this.retryPolicy = retryPolicy;
    }
    
    /**
     * Выполнить операцию с retry логикой
     */
    @Retryable(
        value = {BaseApiException.class},
        maxAttempts = 5,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public JsonNode executeWithRetry(ApiOperation operation) {
        try {
            JsonNode result = operation.execute();
            
            // Проверяем ответ на наличие ошибок
            if (retryPolicy.shouldRetryForResponse(result)) {
                throw createApiException(result);
            }
            
            return result;
        } catch (BaseApiException e) {
            log.warn("🔄 [{}] API operation failed, will retry: {}", serviceName, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ [{}] Unexpected error during API operation: {}", serviceName, e.getMessage());
            throw new RuntimeException("Unexpected error during API operation", e);
        }
    }
    
    /**
     * Создать исключение на основе ответа API
     * Должен быть переопределен в конкретных реализациях
     */
    protected abstract BaseApiException createApiException(JsonNode response);
    
    /**
     * Логировать успешную операцию
     */
    protected void logSuccess(String operationName) {
        log.info("✅ [{}] {} completed successfully", serviceName, operationName);
    }
    
    /**
     * Логировать неудачную операцию
     */
    protected void logFailure(String operationName, String error) {
        log.error("❌ [{}] {} failed: {}", serviceName, operationName, error);
    }
    
    /**
     * Функциональный интерфейс для операций API
     */
    @FunctionalInterface
    public interface ApiOperation {
        JsonNode execute() throws Exception;
    }
    
    /**
     * Получить название сервиса
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Получить политику retry
     */
    public BaseRetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
}

