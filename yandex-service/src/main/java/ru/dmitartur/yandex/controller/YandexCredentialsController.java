package ru.dmitartur.yandex.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.common.security.SecurityUtils;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;
import ru.dmitartur.library.marketplace.service.CompanyCredentialsService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/yandex/credentials")
@RequiredArgsConstructor
public class YandexCredentialsController {
    
    private final CompanyCredentialsService credentialsService;
    
    /**
     * Получить учетные данные Yandex для компании
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyCredentials> getCredentials(@PathVariable UUID companyId) {
        log.info("🔍 Getting Yandex credentials for company: {}", companyId);
        
        try {
            SecurityUtils.requireAccessToCompany(companyId);
        } catch (SecurityException e) {
            log.warn("❌ {}", e.getMessage());
            return ResponseEntity.status(403).build();
        }
        
        Optional<CompanyCredentials> credentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "YANDEX");
        if (credentials.isPresent()) {
            // Скрываем чувствительные данные
            CompanyCredentials result = credentials.get();
            result.setClientSecret("***");
            result.setAccessToken("***");
            result.setRefreshToken("***");
            
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Сохранить или обновить учетные данные Yandex
     */
    @PostMapping("/{companyId}")
    public ResponseEntity<CompanyCredentials> saveCredentials(
            @PathVariable UUID companyId, 
            @RequestBody CompanyCredentials credentials) {
        
        log.info("💾 Saving Yandex credentials for company: {}", companyId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to save credentials for company: {} for user: {}", companyId, JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        credentials.setCompanyId(companyId);
        credentials.setMarketplace("YANDEX");
        credentials.setActive(true);
        
        CompanyCredentials saved = credentialsService.save(credentials);
        
        // Скрываем чувствительные данные в ответе
        saved.setClientSecret("***");
        saved.setAccessToken("***");
        saved.setRefreshToken("***");
        
        return ResponseEntity.ok(saved);
    }
    
    /**
     * Удалить учетные данные Yandex
     */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable UUID companyId) {
        log.info("🗑️ Deactivating Yandex credentials for company: {}", companyId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to delete credentials for company: {} for user: {}", companyId, JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        credentialsService.deactivate(companyId, "YANDEX");
        return ResponseEntity.ok().build();
    }
    
    /**
     * Проверить статус учетных данных
     */
    @GetMapping("/{companyId}/status")
    public ResponseEntity<Object> getCredentialsStatus(@PathVariable UUID companyId) {
        log.info("📊 Checking Yandex credentials status for company: {}", companyId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to check status for company: {} for user: {}", companyId, JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        Optional<CompanyCredentials> credentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "YANDEX");
        if (credentials.isPresent()) {
            CompanyCredentials cred = credentials.get();
            return ResponseEntity.ok(java.util.Map.of(
                    "exists", true,
                    "isActive", cred.isActive(),
                    "hasValidToken", cred.isTokenValid(),
                    "needsTokenRefresh", cred.needsTokenRefresh(),
                    "tokenExpiresAt", cred.getTokenExpiresAt()
            ));
        } else {
            return ResponseEntity.ok(java.util.Map.of("exists", false));
        }
    }
    
    /**
     * Получить все активные учетные данные Yandex (только для администраторов)
     */
    @GetMapping("/all")
    public ResponseEntity<List<CompanyCredentials>> getAllActiveCredentials() {
        log.info("🔍 Getting all active Yandex credentials");
        
        // Проверяем, что пользователь имеет административные права
        if (!SecurityUtils.isAdmin()) {
            log.warn("❌ Access denied to get all credentials for user: {}", JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        List<CompanyCredentials> credentials = credentialsService.findByMarketplace("YANDEX");
        
        // Скрываем чувствительные данные
        credentials.forEach(cred -> {
            cred.setClientSecret("***");
            cred.setAccessToken("***");
            cred.setRefreshToken("***");
        });
        
        return ResponseEntity.ok(credentials);
    }
    

}
