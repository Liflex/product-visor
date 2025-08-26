package ru.dmitartur.library.marketplace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;
import ru.dmitartur.library.marketplace.repository.CompanyCredentialsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CompanyCredentialsService {
    
    private final CompanyCredentialsRepository repository;
    
    /**
     * Найти учетные данные по компании и маркетплейсу
     */
    public Optional<CompanyCredentials> findByCompanyIdAndMarketplace(UUID companyId, String marketplace) {
        log.debug("🔍 Finding credentials for company: {}, marketplace: {}", companyId, marketplace);
        return repository.findByCompanyIdAndMarketplaceType(companyId, marketplace);
    }
    
    /**
     * Найти все учетные данные компании
     */
    public List<CompanyCredentials> findByCompanyId(UUID companyId) {
        log.debug("🔍 Finding all credentials for company: {}", companyId);
        return repository.findByCompanyId(companyId);
    }
    
    /**
     * Найти все активные учетные данные для маркетплейса
     */
    public List<CompanyCredentials> findByMarketplace(String marketplace) {
        log.debug("🔍 Finding all active credentials for marketplace: {}", marketplace);
        return repository.findByMarketplaceTypeAndIsActiveTrue(marketplace);
    }
    
    /**
     * Сохранить или обновить учетные данные
     */
    public CompanyCredentials save(CompanyCredentials credentials) {
        log.info("💾 Saving credentials for company: {}, marketplace: {}", 
                credentials.getCompanyId(), credentials.getMarketplaceName());
        return repository.save(credentials);
    }
    
    /**
     * Получить все активные учетные данные
     */
    public List<CompanyCredentials> findAllActive() {
        log.debug("🔍 Finding all active credentials");
        return repository.findByIsActiveTrue();
    }
    
    /**
     * Получить все учетные данные
     */
    public List<CompanyCredentials> findAll() {
        log.debug("🔍 Finding all credentials");
        return repository.findAll();
    }
    
    /**
     * Проверить существование учетных данных
     */
    public boolean existsByCompanyIdAndMarketplace(UUID companyId, String marketplace) {
        return repository.existsByCompanyIdAndMarketplaceType(companyId, marketplace);
    }
    
    /**
     * Деактивировать учетные данные
     */
    public void deactivate(UUID companyId, String marketplace) {
        Optional<CompanyCredentials> credentials = findByCompanyIdAndMarketplace(companyId, marketplace);
        if (credentials.isPresent()) {
            CompanyCredentials cred = credentials.get();
            cred.setActive(false);
            save(cred);
            log.info("🔒 Deactivated credentials for company: {}, marketplace: {}", companyId, marketplace);
        }
    }
    
    /**
     * Обновить токены
     */
    public CompanyCredentials updateTokens(UUID companyId, String marketplace, 
                                         String accessToken, String refreshToken, 
                                         LocalDateTime expiresAt) {
        Optional<CompanyCredentials> credentialsOpt = findByCompanyIdAndMarketplace(companyId, marketplace);
        if (credentialsOpt.isPresent()) {
            CompanyCredentials credentials = credentialsOpt.get();
            credentials.setAccessToken(accessToken);
            credentials.setRefreshToken(refreshToken);
            credentials.setTokenExpiresAt(expiresAt);
            
            CompanyCredentials saved = save(credentials);
            log.info("🔄 Updated tokens for company: {}, marketplace: {}", companyId, marketplace);
            return saved;
        } else {
            throw new RuntimeException("Credentials not found for company: " + companyId + ", marketplace: " + marketplace);
        }
    }
    
    /**
     * Найти учетные данные, которым нужно обновить токен
     */
    public List<CompanyCredentials> findCredentialsNeedingTokenRefresh() {
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        return repository.findCredentialsNeedingTokenRefresh(expiresAt);
    }
    
    /**
     * Получить действительный токен для компании и маркетплейса
     */
    public Optional<String> getValidAccessToken(UUID companyId, String marketplace) {
        Optional<CompanyCredentials> credentialsOpt = findByCompanyIdAndMarketplace(companyId, marketplace);
        if (credentialsOpt.isPresent()) {
            CompanyCredentials credentials = credentialsOpt.get();
            if (credentials.isTokenValid()) {
                return Optional.of(credentials.getAccessToken());
            } else {
                log.warn("⚠️ Token expired or invalid for company: {}, marketplace: {}", companyId, marketplace);
            }
        } else {
            log.warn("⚠️ No credentials found for company: {}, marketplace: {}", companyId, marketplace);
        }
        return Optional.empty();
    }
}