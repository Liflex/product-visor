package ru.dmitartur.library.marketplace.retry;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import ru.dmitartur.common.retry.BaseRetryPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * Универсальная политика retry для маркетплейсов
 * Может быть настроена для разных маркетплейсов через конфигурацию
 */
@Slf4j
public abstract class BaseMarketplaceRetryPolicy extends BaseRetryPolicy {
    
    protected final String marketplaceName;
    
    public BaseMarketplaceRetryPolicy(String marketplaceName, int maxAttempts) {
        super(marketplaceName, maxAttempts);
        this.marketplaceName = marketplaceName;
    }
    
    /**
     * Получить название маркетплейса
     */
    public String getMarketplaceName() {
        return marketplaceName;
    }
}

