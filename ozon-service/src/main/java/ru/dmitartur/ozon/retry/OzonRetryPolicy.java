package ru.dmitartur.ozon.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Кастомная политика retry для OZON API
 */
@Slf4j
@Component
public class OzonRetryPolicy extends SimpleRetryPolicy {
    
    public OzonRetryPolicy() {
        super(5, createRetryableExceptions(), true); // 5 попыток, с экспоненциальной задержкой
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
        if (lastThrowable instanceof OzonApiException) {
            OzonApiException apiException = (OzonApiException) lastThrowable;
            return isRetryableError(apiException.getErrorCode(), apiException.getErrorMessage());
        }
        
        return super.canRetry(context);
    }
    
    private boolean isRetryableError(String errorCode, String errorMessage) {
        if (errorCode == null) {
            return false;
        }
        
        switch (errorCode) {
            case "TOO_MANY_REQUESTS":
                // Специальная обработка для "Stock is updated too frequently"
                if (errorMessage != null && errorMessage.contains("too frequently")) {
                    log.info("📊 TOO_MANY_REQUESTS: Stock is updated too frequently - will retry");
                    return true;
                }
                return true;
                
            case "RATE_LIMIT_EXCEEDED":
            case "SERVICE_UNAVAILABLE":
            case "INTERNAL_SERVER_ERROR":
            case "GATEWAY_TIMEOUT":
            case "BAD_GATEWAY":
                log.info("🔄 Retryable error detected: code={}", errorCode);
                return true;
                
            default:
                log.debug("❌ Non-retryable error: code={}", errorCode);
                return false;
        }
    }
    
    /**
     * Проверить ответ от API и определить, нужно ли повторить запрос
     */
    public boolean shouldRetryForResponse(JsonNode response) {
        if (response == null) {
            return true;
        }
        
        // Проверяем на ошибки в result массиве (для updateStocks)
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
}
