package ru.dmitartur.ozon.retry;

/**
 * Исключение для ошибок OZON API
 */
public class OzonApiException extends RuntimeException {
    
    private final String errorCode;
    private final String errorMessage;
    
    public OzonApiException(String errorCode, String errorMessage) {
        super("OZON API Error: " + errorCode + " - " + errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public OzonApiException(String errorCode, String errorMessage, Throwable cause) {
        super("OZON API Error: " + errorCode + " - " + errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
