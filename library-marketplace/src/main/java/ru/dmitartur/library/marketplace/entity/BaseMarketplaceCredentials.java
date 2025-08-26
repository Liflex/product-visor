package ru.dmitartur.library.marketplace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.dmitartur.common.security.CryptoStringConverter;

/**
 * Базовая Entity для учетных данных маркетплейсов
 * Содержит общие поля для хранения API ключей и настроек
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BaseMarketplaceCredentials extends BaseMarketplaceEntity {
    
    @Column(name = "client_id", nullable = false)
    private String clientId;
    
    @Column(name = "api_key", nullable = false)
    @Convert(converter = ru.dmitartur.common.security.CryptoStringConverter.class)
    private String apiKey;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "last_sync_at")
    private java.time.LocalDateTime lastSyncAt;
    
    @Column(name = "sync_status")
    private String syncStatus = "NEVER_SYNCED";
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * Получить название маркетплейса
     */
    @Override
    public abstract String getMarketplaceName();
    
    /**
     * Проверить, что учетные данные валидны
     */
    public boolean isValid() {
        return clientId != null && !clientId.isEmpty() &&
               apiKey != null && !apiKey.isEmpty() &&
               isActive;
    }
    
    /**
     * Получить маскированный API ключ для логирования
     * Для зашифрованных данных возвращает "***ENCRYPTED***"
     */
    public String getMaskedApiKey() {
        if (apiKey == null) {
            return "********";
        }
        // Если API ключ зашифрован (начинается с Base64), показываем это
        if (apiKey.length() > 20 && apiKey.matches("^[A-Za-z0-9+/]+={0,2}$")) {
            return "***ENCRYPTED***";
        }
        // Для незашифрованных данных (в процессе миграции)
        if (apiKey.length() < 8) {
            return "********";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}

