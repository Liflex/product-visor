package ru.dmitartur.ozon.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.common.security.CompanyContextHolder;
import ru.dmitartur.library.marketplace.entity.CompanyCredentials;
import ru.dmitartur.ozon.entity.SyncCheckpoint;
import ru.dmitartur.ozon.repository.SyncCheckpointRepository;
import ru.dmitartur.library.marketplace.service.CompanyCredentialsService;
import ru.dmitartur.ozon.service.OzonService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OzonBackfillScheduler {
    private final OzonService ozonService;
    private final SyncCheckpointRepository syncCheckpointRepository;
    private final CompanyCredentialsService companyCredentialsService;
    
    @Value("${app.sync.checkpoint-name:FBO_ORDERS}")
    private String checkpointName;
    
    @Value("${app.sync.max-gap-minutes:1}")
    private int maxGapMinutes; // Максимальный разрыв в минутах для автоматической синхронизации
    
    @Value("${app.sync.enabled:true}")
    private boolean syncEnabled;

    @Autowired
    @Lazy
    private OzonBackfillScheduler ozonBackfillScheduler;

    /**
     * Проверить необходимость синхронизации при старте приложения
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    @Transactional// Запуск через 30 секунд после старта
    public void checkSyncOnStartup() {
        if (!syncEnabled) {
            log.info("🔄 Sync service is disabled");
            return;
        }
        
        log.info("🔄 Checking sync status on startup...");
        
        try {
            // Получаем все компании с учетными данными
            List<CompanyCredentials> companies = companyCredentialsService.findAll();
            
            if (companies.isEmpty()) {
                log.warn("⚠️ No companies found with credentials, using default configuration");
            } else {
                log.info("🏢 Found {} companies, performing sync for each", companies.size());
                for (CompanyCredentials company : companies) {
                    performSyncForCompany(company);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during startup sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Периодическая проверка синхронизации
     */
    @Scheduled(fixedDelay = 120000) // Каждые 2 минуты
    @Transactional
    public void periodicSyncCheck() {
        if (!syncEnabled) {
            return;
        }
        
        log.debug("🔄 Performing periodic sync check...");
        
        try {
            // Получаем все компании с учетными данными
            List<CompanyCredentials> companies = companyCredentialsService.findAll();
            
            if (companies.isEmpty()) {
                log.warn("⚠️ No companies found with credentials, using default configuration");
            } else {
                log.info("🏢 Found {} companies, performing periodic sync for each", companies.size());
                for (CompanyCredentials company : companies) {
                    performPeriodicSyncForCompany(company);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during periodic sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Выполнить синхронизацию для конкретной компании
     */
    private void performSyncForCompany(CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        log.info("🏢 Starting sync for company: {}", companyId);
        
        try {
            Optional<SyncCheckpoint> checkpoint = syncCheckpointRepository.findByCheckpointName(checkpointName + "_" + companyId);
            
            if (checkpoint.isEmpty()) {
                log.info("🆕 No sync checkpoint found for company {}, performing initial sync", companyId);
                ozonBackfillScheduler.performInitialSyncForCompany(company);
            } else {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), LocalDateTime.now());
                
                if (gap.toHours() > maxGapMinutes) {
                    log.warn("⚠️ Large sync gap detected for company {}: {} hours, performing catch-up sync", companyId, gap.toHours());
                    ozonBackfillScheduler.performCatchUpSyncForCompany(cp, company);
                } else {
                    log.info("✅ Sync is up to date for company {}, last sync: {} hours ago", companyId, gap.toHours());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during sync for company {}: {}", companyId, e.getMessage(), e);
        }
    }

    /**
     * Выполнить периодическую синхронизацию для конкретной компании
     */
    private void performPeriodicSyncForCompany(CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        
        try {
            Optional<SyncCheckpoint> checkpoint = syncCheckpointRepository.findByCheckpointName(checkpointName + "_" + companyId);
            
            if (checkpoint.isPresent()) {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), LocalDateTime.now());
                
                if (gap.toMinutes() > maxGapMinutes) {
                    log.info("🔄 Periodic sync triggered for company {}, gap: {} hours", companyId, gap.toHours());
                    performCatchUpSyncForCompany(cp, company);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during periodic sync for company {}: {}", companyId, e.getMessage(), e);
        }
    }

    /**
     * Выполнить начальную синхронизацию для конкретной компании
     */
    @Transactional
    public void performInitialSyncForCompany(CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        log.info("🚀 Starting initial sync for company: {}", companyId);
        
        long startTime = System.currentTimeMillis();
        SyncCheckpoint checkpoint = new SyncCheckpoint(checkpointName + "_" + companyId, LocalDateTime.now());
        checkpoint.setStatus("IN_PROGRESS");
        checkpoint = syncCheckpointRepository.save(checkpoint);
        
        try {
            // Синхронизируем заказы за последние полгода для первого запуска
            LocalDateTime from = LocalDateTime.now().minusDays(180);
            LocalDateTime to = LocalDateTime.now();
            
            log.info("🚀 Initial sync for company {}: loading FBO + FBS orders for the last 6 months ({} to {})", 
                    companyId, from, to);
            
            int processedOrders = ozonService.backfillAllOrders(
                new ru.dmitartur.common.dto.marketplace.DateRangeDto(from.toString(), to.toString()), 1000);
            
            checkpoint.setOrdersProcessed(processedOrders);
            checkpoint.setStatus("SUCCESS");
            checkpoint.setLastSyncAt(LocalDateTime.now());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            
            syncCheckpointRepository.save(checkpoint);
            
            log.info("✅ Initial sync completed for company {}: {} FBO + FBS orders processed in {} ms", 
                    companyId, processedOrders, checkpoint.getSyncDurationMs());
            
        } catch (Exception e) {
            checkpoint.setStatus("FAILED");
            checkpoint.setErrorMessage(e.getMessage());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            syncCheckpointRepository.save(checkpoint);
            
            log.error("❌ Initial sync failed for company {}: {}", companyId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Выполнить догоняющую синхронизацию для конкретной компании
     */
    @Transactional
    public void performCatchUpSyncForCompany(SyncCheckpoint checkpoint, CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        log.info("🔄 Starting catch-up sync for company {} from {}", companyId, checkpoint.getLastSyncAt());
        
        long startTime = System.currentTimeMillis();
        checkpoint.setStatus("IN_PROGRESS");
        checkpoint = syncCheckpointRepository.save(checkpoint);
        
        try {
            LocalDateTime from = checkpoint.getLastSyncAt();
            LocalDateTime to = LocalDateTime.now();
            
            int processedOrders = ozonService.backfillAllOrders(
                new ru.dmitartur.common.dto.marketplace.DateRangeDto(from.toString(), to.toString()), 1000);
            
            checkpoint.setOrdersProcessed(processedOrders);
            checkpoint.setStatus("SUCCESS");
            checkpoint.setLastSyncAt(LocalDateTime.now());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            
            syncCheckpointRepository.save(checkpoint);
            
            log.info("✅ Catch-up sync completed for company {}: {} FBO + FBS orders processed in {} ms", 
                    companyId, processedOrders, checkpoint.getSyncDurationMs());
            
        } catch (Exception e) {
            checkpoint.setStatus("FAILED");
            checkpoint.setErrorMessage(e.getMessage());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            syncCheckpointRepository.save(checkpoint);
            
            log.error("❌ Catch-up sync failed for company {}: {}", companyId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Получить информацию о последней синхронизации
     */
    public Optional<SyncCheckpoint> getLastSyncInfo() {
        return syncCheckpointRepository.findByCheckpointName(checkpointName);
    }

    /**
     * Получить информацию о последней синхронизации для конкретной компании
     */
    public Optional<SyncCheckpoint> getLastSyncInfoForCompany(UUID companyId) {
        return syncCheckpointRepository.findByCheckpointName(checkpointName + "_" + companyId);
    }


    /**
     * Принудительно запустить синхронизацию для всех компаний
     */
    @Transactional
    public void forceSyncAllCompanies() {
        log.info("🔄 Force sync requested for all companies");
        
        List<CompanyCredentials> companies = companyCredentialsService.findAll();
        
        if (companies.isEmpty()) {
            log.warn("⚠️ No companies found with credentials, using default configuration");
        } else {
            log.info("🏢 Force syncing {} companies", companies.size());
            for (CompanyCredentials company : companies) {
                try {
                    performInitialSyncForCompany(company);
                } catch (Exception e) {
                    log.error("❌ Force sync failed for company {}: {}", company.getCompanyId(), e.getMessage(), e);
                }
            }
        }
    }
}






