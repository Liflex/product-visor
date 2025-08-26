package ru.dmitartur.library.marketplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_credentials")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCredentials extends BaseMarketplaceCredentials {
    
    @Column(name = "access_token")
    private String accessToken;
    
    @Column(name = "refresh_token")
    private String refreshToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    

    
    @Column(name = "marketplace_type")
    private String marketplaceType; // "OZON", "YANDEX", etc.
    
    @Override
    public String getMarketplaceName() {
        return marketplaceType;
    }
    
    /**
     * Проверить, действителен ли токен (для Ozon всегда true, так как токены не используются)
     */
    public boolean isTokenValid() {
        return true; // Ozon не использует токены
    }
    
    /**
     * Проверить, нужен ли refresh токена (для Ozon всегда false)
     */
    public boolean needsTokenRefresh() {
        return false; // Ozon не использует токены
    }
    
    /**
     * Проверить, что учетные данные валидны
     */
    @Override
    public boolean isValid() {
        return super.isValid(); // Для Ozon достаточно clientId и apiKey
    }
}
