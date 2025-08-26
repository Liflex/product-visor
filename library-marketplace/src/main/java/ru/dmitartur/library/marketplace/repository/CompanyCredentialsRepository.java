package ru.dmitartur.library.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyCredentialsRepository extends JpaRepository<CompanyCredentials, Long> {
    
    /**
     * Найти учетные данные по компании и маркетплейсу
     */
    Optional<CompanyCredentials> findByCompanyIdAndMarketplaceType(UUID companyId, String marketplaceType);
    
    /**
     * Найти все учетные данные компании
     */
    List<CompanyCredentials> findByCompanyId(UUID companyId);
    
    /**
     * Найти все активные учетные данные
     */
    List<CompanyCredentials> findByIsActiveTrue();
    
    /**
     * Найти все учетные данные для конкретного маркетплейса
     */
    List<CompanyCredentials> findByMarketplaceTypeAndIsActiveTrue(String marketplaceType);
    
    /**
     * Проверить существование учетных данных
     */
    boolean existsByCompanyIdAndMarketplaceType(UUID companyId, String marketplaceType);
    
    /**
     * Найти учетные данные, которым нужно обновить токен
     */
    @Query("SELECT cc FROM CompanyCredentials cc WHERE cc.isActive = true AND cc.tokenExpiresAt IS NOT NULL AND cc.tokenExpiresAt < :expiresAt")
    List<CompanyCredentials> findCredentialsNeedingTokenRefresh(@Param("expiresAt") LocalDateTime expiresAt);
}
