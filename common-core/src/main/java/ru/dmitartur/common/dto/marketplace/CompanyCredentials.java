package ru.dmitartur.common.dto.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCredentials {
    
    private UUID id;
    private UUID companyId;
    private String marketplace; // "OZON", "YANDEX", etc.
    private String clientId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private String warehouseId; // Опционально
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Проверить, действителен ли токен
     */
    public boolean isTokenValid() {
        return accessToken != null && 
               !accessToken.isEmpty() && 
               tokenExpiresAt != null && 
               tokenExpiresAt.isAfter(LocalDateTime.now());
    }
    
    /**
     * Проверить, нужен ли refresh токена
     */
    public boolean needsTokenRefresh() {
        return tokenExpiresAt != null && 
               tokenExpiresAt.minusMinutes(5).isBefore(LocalDateTime.now());
    }
}

