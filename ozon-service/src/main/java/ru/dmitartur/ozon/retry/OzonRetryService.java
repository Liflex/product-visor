package ru.dmitartur.ozon.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Сервис для работы с retry используя Spring Retry
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OzonRetryService {
    
    private final OzonRetryPolicy retryPolicy;
    
    /**
     * Выполнить API вызов с retry для updateStocks
     * @param apiCall функция API вызова
     * @param offerId ID товара для логирования
     * @return результат API вызова
     */
    @Retryable(
        value = {OzonApiException.class, RuntimeException.class},
        maxAttempts = 30,
        backoff = @Backoff(delay = 2000, multiplier = 2, maxDelay = 60000)
    )
    public JsonNode executeUpdateStocksWithRetry(Supplier<JsonNode> apiCall, String offerId) {
        log.debug("🔄 Executing updateStocks for offerId: {}", offerId);
        
        JsonNode response = apiCall.get();
        
        // Проверяем ответ на наличие ошибок
        if (retryPolicy.shouldRetryForResponse(response)) {
            // Извлекаем информацию об ошибке
            String errorCode = extractErrorCode(response);
            String errorMessage = extractErrorMessage(response);
            
            log.warn("⚠️ OZON API error detected: code={}, message={} for offerId={}", 
                errorCode, errorMessage, offerId);
            
            throw new OzonApiException(errorCode, errorMessage);
        }
        
        log.debug("✅ updateStocks successful for offerId: {}", offerId);
        return response;
    }
    
    /**
     * Выполнить API вызов с retry для общих операций
     * @param apiCall функция API вызова
     * @param operationName название операции для логирования
     * @return результат API вызова
     */
    @Retryable(
        value = {OzonApiException.class, RuntimeException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000)
    )
    public JsonNode executeWithRetry(Supplier<JsonNode> apiCall, String operationName) {
        log.debug("🔄 Executing operation: {}", operationName);
        
        JsonNode response = apiCall.get();
        
        // Проверяем ответ на наличие ошибок
        if (retryPolicy.shouldRetryForResponse(response)) {
            String errorCode = extractErrorCode(response);
            String errorMessage = extractErrorMessage(response);
            
            log.warn("⚠️ OZON API error detected: code={}, message={} for operation={}", 
                errorCode, errorMessage, operationName);
            
            throw new OzonApiException(errorCode, errorMessage);
        }
        
        log.debug("✅ Operation successful: {}", operationName);
        return response;
    }
    
    private String extractErrorCode(JsonNode response) {
        // Проверяем на ошибки в result массиве (для updateStocks)
        if (response.has("result") && response.get("result").isArray()) {
            JsonNode result = response.get("result");
            for (JsonNode item : result) {
                if (item.has("errors") && item.get("errors").isArray()) {
                    JsonNode errors = item.get("errors");
                    if (errors.size() > 0) {
                        return errors.get(0).path("code").asText();
                    }
                }
            }
        }
        
        // Проверяем на общие ошибки API
        if (response.has("error")) {
            return response.get("error").path("code").asText();
        }
        
        return "UNKNOWN_ERROR";
    }
    
    private String extractErrorMessage(JsonNode response) {
        // Проверяем на ошибки в result массиве (для updateStocks)
        if (response.has("result") && response.get("result").isArray()) {
            JsonNode result = response.get("result");
            for (JsonNode item : result) {
                if (item.has("errors") && item.get("errors").isArray()) {
                    JsonNode errors = item.get("errors");
                    if (errors.size() > 0) {
                        return errors.get(0).path("message").asText();
                    }
                }
            }
        }
        
        // Проверяем на общие ошибки API
        if (response.has("error")) {
            return response.get("error").path("message").asText();
        }
        
        return "Unknown error";
    }
}
