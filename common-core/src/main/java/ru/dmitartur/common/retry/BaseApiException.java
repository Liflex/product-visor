package ru.dmitartur.common.retry;

/**
 * Базовое исключение для API
 */
public abstract class BaseApiException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    private final String serviceName;
    
    public BaseApiException(String serviceName, String errorCode, String errorMessage) {
        super(String.format("[%s] API Error: %s - %s", serviceName, errorCode, errorMessage));
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public BaseApiException(String serviceName, String errorCode, String errorMessage, Throwable cause) {
        super(String.format("[%s] API Error: %s - %s", serviceName, errorCode, errorMessage), cause);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Проверить, является ли ошибка повторяемой
     * Должен быть переопределен в конкретных реализациях
     */
    public abstract boolean isRetryable();
}

