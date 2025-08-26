package ru.dmitartur.yandex.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yandex")
public class YandexProperties {
    
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String defaultWarehouseId;
    private int maxRetryAttempts = 5;
    private long retryDelayMs = 1000;
    private double retryMultiplier = 2.0;
    private int syncIntervalMinutes = 60;
    private boolean autoSyncEnabled = true;
}
