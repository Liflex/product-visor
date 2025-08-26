package ru.dmitartur.library.marketplace.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.retry.BaseRetryService;

/**
 * Универсальный сервис retry для маркетплейсов
 * Предоставляет общие методы для работы с retry логикой
 */
@Slf4j
@Service
public abstract class BaseMarketplaceRetryService extends BaseRetryService {
    
    protected final String marketplaceName;
    
    public BaseMarketplaceRetryService(String marketplaceName, BaseMarketplaceRetryPolicy retryPolicy) {
        super(marketplaceName, retryPolicy);
        this.marketplaceName = marketplaceName;
    }
    
    /**
     * Получить название маркетплейса
     */
    public String getMarketplaceName() {
        return marketplaceName;
    }
}

