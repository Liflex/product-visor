package ru.dmitartur.library.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Базовый класс для свойств маркетплейсов
 * Содержит общие настройки, которые могут быть расширены для конкретных маркетплейсов
 */
@ConfigurationProperties(prefix = "marketplace")
public abstract class BaseMarketplaceProperties {
    
    private String baseUrl;
    private String clientId;
    private String apiKey;
    private String defaultWarehouseId;
    private int maxRetryAttempts = 5;
    private long retryDelayMs = 1000;
    private double retryMultiplier = 2.0;
    private int syncIntervalMinutes = 60;
    private boolean autoSyncEnabled = true;
    
    public String getBaseUrl() { 
        return baseUrl; 
    }
    
    public void setBaseUrl(String baseUrl) { 
        this.baseUrl = baseUrl; 
    }
    
    public String getClientId() { 
        return clientId; 
    }
    
    public void setClientId(String clientId) { 
        this.clientId = clientId; 
    }
    
    public String getApiKey() { 
        return apiKey; 
    }
    
    public void setApiKey(String apiKey) { 
        this.apiKey = apiKey; 
    }
    
    public String getDefaultWarehouseId() { 
        return defaultWarehouseId; 
    }
    
    public void setDefaultWarehouseId(String defaultWarehouseId) { 
        this.defaultWarehouseId = defaultWarehouseId; 
    }
    
    public int getMaxRetryAttempts() { 
        return maxRetryAttempts; 
    }
    
    public void setMaxRetryAttempts(int maxRetryAttempts) { 
        this.maxRetryAttempts = maxRetryAttempts; 
    }
    
    public long getRetryDelayMs() { 
        return retryDelayMs; 
    }
    
    public void setRetryDelayMs(long retryDelayMs) { 
        this.retryDelayMs = retryDelayMs; 
    }
    
    public double getRetryMultiplier() { 
        return retryMultiplier; 
    }
    
    public void setRetryMultiplier(double retryMultiplier) { 
        this.retryMultiplier = retryMultiplier; 
    }
    
    public int getSyncIntervalMinutes() { 
        return syncIntervalMinutes; 
    }
    
    public void setSyncIntervalMinutes(int syncIntervalMinutes) { 
        this.syncIntervalMinutes = syncIntervalMinutes; 
    }
    
    public boolean isAutoSyncEnabled() { 
        return autoSyncEnabled; 
    }
    
    public void setAutoSyncEnabled(boolean autoSyncEnabled) { 
        this.autoSyncEnabled = autoSyncEnabled; 
    }
    
    /**
     * Получить название маркетплейса
     * Должен быть переопределен в конкретных реализациях
     */
    public abstract String getMarketplaceName();
    
    /**
     * Проверить, что все обязательные свойства настроены
     */
    public boolean isValid() {
        return baseUrl != null && !baseUrl.isEmpty() &&
               clientId != null && !clientId.isEmpty() &&
               apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Получить описание конфигурации (без секретных данных)
     */
    public String getConfigurationSummary() {
        return String.format("Marketplace: %s, BaseUrl: %s, ClientId: %s, WarehouseId: %s, " +
                           "RetryAttempts: %d, SyncInterval: %d min, AutoSync: %s",
                           getMarketplaceName(), baseUrl, clientId, defaultWarehouseId,
                           maxRetryAttempts, syncIntervalMinutes, autoSyncEnabled);
    }
}

