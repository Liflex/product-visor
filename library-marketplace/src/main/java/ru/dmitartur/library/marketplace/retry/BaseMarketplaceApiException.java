package ru.dmitartur.library.marketplace.retry;

import ru.dmitartur.common.retry.BaseApiException;

/**
 * Базовое исключение для API маркетплейсов
 */
public abstract class BaseMarketplaceApiException extends BaseApiException {
    
    private final String marketplaceName;
    
    public BaseMarketplaceApiException(String marketplaceName, String errorCode, String errorMessage) {
        super(marketplaceName, errorCode, errorMessage);
        this.marketplaceName = marketplaceName;
    }
    
    public BaseMarketplaceApiException(String marketplaceName, String errorCode, String errorMessage, Throwable cause) {
        super(marketplaceName, errorCode, errorMessage, cause);
        this.marketplaceName = marketplaceName;
    }
    
    public String getMarketplaceName() {
        return marketplaceName;
    }
}

