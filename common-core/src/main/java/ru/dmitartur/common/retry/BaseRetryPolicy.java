package ru.dmitartur.common.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовая политика retry для API
 * Может быть настроена для разных сервисов через конфигурацию
 */
@Slf4j
public abstract class BaseRetryPolicy extends SimpleRetryPolicy {
    
    protected final String serviceName;
    protected final int maxAttempts;
    
    public BaseRetryPolicy(String serviceName, int maxAttempts) {
        super(maxAttempts, createRetryableExceptions(), true);
        this.serviceName = serviceName;
        this.maxAttempts = maxAttempts;
    }
    
    private static Map<Class<? extends Throwable>, Boolean> createRetryableExceptions() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(RuntimeException.class, true);
        retryableExceptions.put(Exception.class, true);
        return retryableExceptions;
    }
    
    @Override
    public boolean canRetry(RetryContext context) {
        // Проверяем, есть ли в контексте ответ от API с ошибкой
        Object lastThrowable = context.getLastThrowable();
        if (lastThrowable instanceof BaseApiException) {
            BaseApiException apiException = (BaseApiException) lastThrowable;
            return isRetryableError(apiException.getErrorCode(), apiException.getErrorMessage());
        }
        
        return super.canRetry(context);
    }
    
    /**
     * Определить, является ли ошибка повторяемой
     * Должен быть переопределен в конкретных реализациях
     */
    protected abstract boolean isRetryableError(String errorCode, String errorMessage);
    
    /**
     * Проверить ответ от API и определить, нужно ли повторить запрос
     */
    public boolean shouldRetryForResponse(JsonNode response) {
        if (response == null) {
            return true;
        }
        
        // Проверяем на ошибки в result массиве (для batch операций)
        if (response.has("result") && response.get("result").isArray()) {
            JsonNode result = response.get("result");
            for (JsonNode item : result) {
                if (item.has("errors") && item.get("errors").isArray()) {
                    JsonNode errors = item.get("errors");
                    for (JsonNode error : errors) {
                        String errorCode = error.path("code").asText();
                        String errorMessage = error.path("message").asText();
                        
                        if (isRetryableError(errorCode, errorMessage)) {
                            return true;
                        }
                    }
                }
            }
        }
        
        // Проверяем на общие ошибки API
        if (response.has("error")) {
            JsonNode error = response.get("error");
            String errorCode = error.path("code").asText();
            return isRetryableError(errorCode, "");
        }
        
        return false;
    }
    
    /**
     * Получить название сервиса
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Получить максимальное количество попыток
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }
    
    /**
     * Логировать информацию о retry
     */
    protected void logRetryAttempt(RetryContext context, String errorCode, String errorMessage) {
        log.info("🔄 [{}] Retry attempt {}/{} for error: code={}, message={}", 
                serviceName, context.getRetryCount(), maxAttempts, errorCode, errorMessage);
    }
}

