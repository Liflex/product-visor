package ru.dmitartur.ozon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.common.security.SecurityUtils;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;
import ru.dmitartur.library.marketplace.service.CompanyCredentialsService;
import ru.dmitartur.ozon.integration.OzonApi;
import ru.dmitartur.ozon.dto.ConnectionStatusResponse;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ozon/credentials")
@RequiredArgsConstructor
public class CompanyCredentialsController {

    private final CompanyCredentialsService credentialsService;
    private final OzonApi ozonApi;

    /**
     * Получить учетные данные для текущей компании
     */
    @GetMapping
    public ResponseEntity<CompanyCredentials> getForCurrentCompany() {
        UUID companyId = JwtUtil.resolveEffectiveCompanyId().map(UUID::fromString).orElseThrow();
        log.info("🔍 Getting Ozon credentials for company: {}", companyId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to company: {} for user: {}", companyId, JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        Optional<CompanyCredentials> credentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "OZON");
        if (credentials.isPresent()) {
            // Скрываем чувствительные данные
            CompanyCredentials result = credentials.get();
            result.setAccessToken("***");
            result.setRefreshToken("***");
            
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Создать или обновить учетные данные (только один набор на компанию)
     */
    @PostMapping
    public ResponseEntity<CompanyCredentials> upsert(@RequestBody CompanyCredentials payload) {
        UUID companyId = JwtUtil.resolveEffectiveCompanyId().map(UUID::fromString).orElseThrow();
        UUID userId = JwtUtil.getRequiredOwnerId();
        log.info("💾 Saving Ozon credentials for company: {} by user: {}", companyId, userId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to save credentials for company: {} for user: {}", companyId, userId);
            return ResponseEntity.status(403).build();
        }
        
        // Валидация входных данных
        if (payload.getClientId() == null || payload.getClientId().trim().isEmpty()) {
            log.warn("❌ Invalid clientId provided for company: {}", companyId);
            return ResponseEntity.badRequest().build();
        }
        
        if (payload.getApiKey() == null || payload.getApiKey().trim().isEmpty()) {
            log.warn("❌ Invalid apiKey provided for company: {}", companyId);
            return ResponseEntity.badRequest().build();
        }
        
        // Проверяем существование учетных данных для компании
        Optional<CompanyCredentials> existingCredentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "OZON");
        
        CompanyCredentials credentialsToSave;
        if (existingCredentials.isPresent()) {
            // Обновляем существующие учетные данные
            credentialsToSave = existingCredentials.get();
            credentialsToSave.setApiKey(payload.getApiKey());
            credentialsToSave.setActive(true);
            log.info("🔄 Updating existing Ozon credentials for company: {}", companyId);
        } else {
            // Создаем новые учетные данные
            payload.setCompanyId(companyId);
            payload.setUserId(userId);
            payload.setMarketplaceType("OZON");
            payload.setActive(true);
            credentialsToSave = payload;
            log.info("➕ Creating new Ozon credentials for company: {}", companyId);
        }
        
        CompanyCredentials saved = credentialsService.save(credentialsToSave);
        
        // Скрываем чувствительные данные в ответе
        saved.setAccessToken("***");
        saved.setRefreshToken("***");
        
        log.info("✅ Ozon credentials saved successfully for company: {}", companyId);
        return ResponseEntity.ok(saved);
    }

    /**
     * Обновить учетные данные по ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyCredentials> update(@PathVariable Long id, @RequestBody CompanyCredentials payload) {
        UUID companyId = JwtUtil.resolveEffectiveCompanyId().map(UUID::fromString).orElseThrow();
        UUID userId = JwtUtil.getRequiredOwnerId();
        log.info("🔄 Updating Ozon credentials with id: {} for company: {} by user: {}", id, companyId, userId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to update credentials for company: {} for user: {}", companyId, userId);
            return ResponseEntity.status(403).build();
        }
        
        // Проверяем существование учетных данных
        Optional<CompanyCredentials> existingCredentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "OZON");
        if (existingCredentials.isEmpty()) {
            log.warn("❌ Credentials not found for company: {}", companyId);
            return ResponseEntity.notFound().build();
        }
        
        CompanyCredentials existing = existingCredentials.get();
        if (!existing.getId().equals(id)) {
            log.warn("❌ ID mismatch for credentials update. Expected: {}, got: {}", existing.getId(), id);
            return ResponseEntity.badRequest().build();
        }
        
        // Валидация входных данных
        if (payload.getClientId() == null || payload.getClientId().trim().isEmpty()) {
            log.warn("❌ Invalid clientId provided for company: {}", companyId);
            return ResponseEntity.badRequest().build();
        }
        
        if (payload.getApiKey() == null || payload.getApiKey().trim().isEmpty()) {
            log.warn("❌ Invalid apiKey provided for company: {}", companyId);
            return ResponseEntity.badRequest().build();
        }
        
        // Обновляем только разрешенные поля
        existing.setClientId(payload.getClientId());
        existing.setApiKey(payload.getApiKey());
        
        CompanyCredentials saved = credentialsService.save(existing);
        
        // Скрываем чувствительные данные в ответе
        saved.setAccessToken("***");
        saved.setRefreshToken("***");
        
        log.info("✅ Ozon credentials updated successfully for company: {}", companyId);
        return ResponseEntity.ok(saved);
    }

    /**
     * Удалить учетные данные (деактивировать)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        UUID companyId = JwtUtil.resolveEffectiveCompanyId().map(UUID::fromString).orElseThrow();
        UUID userId = JwtUtil.getRequiredOwnerId();
        log.info("🗑️ Deleting Ozon credentials with id: {} for company: {} by user: {}", id, companyId, userId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to delete credentials for company: {} for user: {}", companyId, userId);
            return ResponseEntity.status(403).build();
        }
        
        // Проверяем существование учетных данных
        Optional<CompanyCredentials> existingCredentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "OZON");
        if (existingCredentials.isEmpty()) {
            log.warn("❌ Credentials not found for company: {}", companyId);
            return ResponseEntity.notFound().build();
        }
        
        CompanyCredentials existing = existingCredentials.get();
        if (!existing.getId().equals(id)) {
            log.warn("❌ ID mismatch for credentials deletion. Expected: {}, got: {}", existing.getId(), id);
            return ResponseEntity.badRequest().build();
        }
        
        // Деактивируем учетные данные вместо физического удаления
        credentialsService.deactivate(companyId, "OZON");
        
        log.info("✅ Ozon credentials deactivated successfully for company: {}", companyId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Проверить статус учетных данных и подключения к Ozon API
     */
    @GetMapping("/status")
    public ResponseEntity<ConnectionStatusResponse> getStatus() {
        UUID companyId = JwtUtil.resolveEffectiveCompanyId().map(UUID::fromString).orElseThrow();
        log.info("🔍 Checking Ozon credentials status and API connection for company: {}", companyId);
        
        // Проверяем права доступа к компании
        if (!SecurityUtils.hasAccessToCompany(companyId)) {
            log.warn("❌ Access denied to check credentials status for company: {} for user: {}", companyId, JwtUtil.getCurrentId().orElse("unknown"));
            return ResponseEntity.status(403).build();
        }
        
        Optional<CompanyCredentials> credentials = credentialsService.findByCompanyIdAndMarketplace(companyId, "OZON");
        
        if (credentials.isPresent()) {
            CompanyCredentials cred = credentials.get();
            return ResponseEntity.ok(createStatusResponse(cred, companyId));
        } else {
            return ResponseEntity.ok(createEmptyStatusResponse());
        }
    }
    
    /**
     * Создать ответ со статусом для существующих учетных данных
     */
    private ConnectionStatusResponse createStatusResponse(CompanyCredentials cred, UUID companyId) {
        // Проверяем реальное подключение к Ozon API
        boolean connectionTestResult = false;
        String connectionErrorMsg = null;
        
        try {
            log.debug("🔍 Testing Ozon API connection for company: {}", companyId);
            connectionTestResult = ozonApi.testConnection();
            if (!connectionTestResult) {
                connectionErrorMsg = "API connection failed - check credentials or network";
            }
        } catch (Exception e) {
            log.error("❌ Error testing Ozon API connection for company: {}: {}", companyId, e.getMessage());
            connectionErrorMsg = "API connection error: " + e.getMessage();
        }
        
        return new ConnectionStatusResponse(
            true, // exists
            connectionTestResult,
            connectionErrorMsg,
            cred.getSyncStatus(),
            cred.getLastSyncAt() != null ? cred.getLastSyncAt().toString() : null,
            cred.getErrorMessage()
        );
    }
    
    /**
     * Создать ответ со статусом для отсутствующих учетных данных
     */
    private ConnectionStatusResponse createEmptyStatusResponse() {
        return new ConnectionStatusResponse(
            false, // exists
            false, // apiConnectionTest
            "No credentials configured", // connectionError
            null, // syncStatus
            null, // lastSyncAt
            null  // errorMessage
        );
    }
}




