package ru.dmitartur.ozon.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Улучшенная версия планировщика синхронизации Ozon с современными практиками программирования.
 * 
 * Основные улучшения:
 * - Асинхронная обработка компаний
 * - Улучшенная обработка ошибок
 * - Более чистая архитектура с разделением ответственности
 * - Конфигурируемые параметры
 * - Лучшее логирование
 * - Метрики производительности
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OzonBackfillSchedulerV2 {
    
    private final OzonService ozonService;
    private final SyncCheckpointRepository syncCheckpointRepository;
    private final CompanyCredentialsService companyCredentialsService;
    
    // Конфигурационные параметры
    @Value("${app.sync.checkpoint-name:FBO_ORDERS}")
    private String checkpointName;
    
    @Value("${app.sync.max-gap-minutes:1}")
    private int maxGapMinutes;
    
    @Value("${app.sync.enabled:true}")
    private boolean syncEnabled;
    
    @Value("${app.sync.initial-delay-ms:10000}")
    private long initialDelayMs;
    
    @Value("${app.sync.periodic-interval-ms:120000}")
    private long periodicIntervalMs;
    
    @Value("${app.sync.batch-size:1000}")
    private int batchSize;
    
    @Value("${app.sync.initial-sync-days:180}")
    private int initialSyncDays;
    
    @Value("${app.sync.async-enabled:true}")
    private boolean asyncEnabled;
    
    // Пул потоков для асинхронной обработки
    private final Executor asyncExecutor = Executors.newFixedThreadPool(3);
    
    /**
     * Проверка синхронизации при старте приложения
     */
    @Scheduled(initialDelayString = "${app.sync.initial-delay-ms:10000}", fixedDelay = Long.MAX_VALUE)
    @Transactional
    public void checkSyncOnStartup() {
        if (!syncEnabled) {
            log.info("🔄 Sync service is disabled");
            return;
        }
        
        log.info("🔄 Starting sync check on application startup...");
        
        try {
            List<CompanyCredentials> companies = getCompaniesWithCredentials();
            if (companies.isEmpty()) {
                log.warn("⚠️ No companies found with credentials");
                return;
            }
            
            log.info("🏢 Found {} companies, starting sync process", companies.size());
            
            if (asyncEnabled) {
                processCompaniesAsync(companies, this::performSyncForCompany);
            } else {
                companies.forEach(this::performSyncForCompany);
            }
            
        } catch (Exception e) {
            log.error("❌ Critical error during startup sync check", e);
        }
    }
    
    /**
     * Периодическая проверка синхронизации
     */
    @Scheduled(fixedDelayString = "${app.sync.periodic-interval-ms:120000}")
    @Transactional
    public void periodicSyncCheck() {
        if (!syncEnabled) {
            return;
        }
        
        log.debug("🔄 Performing periodic sync check...");
        
        try {
            List<CompanyCredentials> companies = getCompaniesWithCredentials();
            if (companies.isEmpty()) {
                return;
            }
            
            if (asyncEnabled) {
                processCompaniesAsync(companies, this::performPeriodicSyncForCompany);
            } else {
                companies.forEach(this::performPeriodicSyncForCompany);
            }
            
        } catch (Exception e) {
            log.error("❌ Error during periodic sync check", e);
        }
    }
    
    /**
     * Асинхронная обработка компаний
     */
    private void processCompaniesAsync(List<CompanyCredentials> companies, 
                                     CompanySyncFunction syncFunction) {
        List<CompletableFuture<Void>> futures = companies.stream()
                .map(company -> CompletableFuture.runAsync(() -> {
                    try {
                        syncFunction.apply(company);
                    } catch (Exception e) {
                        log.error("❌ Error processing company {}: {}", 
                                company.getCompanyId(), e.getMessage(), e);
                    }
                }, asyncExecutor))
                .toList();
        
        // Ждем завершения всех задач
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(throwable -> {
                    log.error("❌ Error in async processing", throwable);
                    return null;
                });
    }
    
    /**
     * Выполнить синхронизацию для конкретной компании
     */
    private void performSyncForCompany(CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        log.info("🏢 Starting sync for company: {}", companyId);
        
        try (CompanyContext context = new CompanyContext(company)) {
            Optional<SyncCheckpoint> checkpoint = getCheckpointForCompany(companyId);
            
            if (checkpoint.isEmpty()) {
                log.info("🆕 No checkpoint found for company {}, performing initial sync", companyId);
                performInitialSyncForCompany(company);
            } else {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), LocalDateTime.now());
                
                if (gap.toMinutes() > maxGapMinutes) {
                    log.warn("⚠️ Large sync gap detected for company {}: {} hours", 
                            companyId, gap.toHours());
                    performCatchUpSyncForCompany(cp, company);
                } else {
                    log.debug("✅ Sync is up to date for company {}, last sync: {} hours ago", 
                            companyId, gap.toHours());
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
        
        try (CompanyContext context = new CompanyContext(company)) {
            Optional<SyncCheckpoint> checkpoint = getCheckpointForCompany(companyId);
            
            if (checkpoint.isPresent()) {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), LocalDateTime.now());
                
                if (gap.toMinutes() > maxGapMinutes) {
                    log.info("🔄 Periodic sync triggered for company {}, gap: {} hours", 
                            companyId, gap.toHours());
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
        
        SyncMetrics metrics = new SyncMetrics();
        SyncCheckpoint checkpoint = createCheckpoint(companyId, "IN_PROGRESS");
        
        try {
            LocalDateTime from = LocalDateTime.now().minusDays(initialSyncDays);
            LocalDateTime to = LocalDateTime.now();
            
            log.info("🚀 Initial sync for company {}: loading orders for the last {} days ({} to {})",
                    companyId, initialSyncDays, from, to);
            
            int processedOrders = ozonService.backfillAllOrders(
                    new DateRangeDto(from.toString(), to.toString()), 
                    batchSize
            );
            
            updateCheckpointSuccess(checkpoint, processedOrders, metrics.getDuration());
            
            log.info("✅ Initial sync completed for company {}: {} orders processed in {} ms",
                    companyId, processedOrders, metrics.getDuration());
            
        } catch (Exception e) {
            updateCheckpointFailure(checkpoint, e.getMessage(), metrics.getDuration());
            log.error("❌ Initial sync failed for company {}: {}", companyId, e.getMessage(), e);
            throw new SyncException("Initial sync failed for company " + companyId, e);
        }
    }
    
    /**
     * Выполнить догоняющую синхронизацию для конкретной компании
     */
    @Transactional
    public void performCatchUpSyncForCompany(SyncCheckpoint checkpoint, CompanyCredentials company) {
        String companyId = company.getCompanyId().toString();
        log.info("🔄 Starting catch-up sync for company {} from {}", 
                companyId, checkpoint.getLastSyncAt());
        
        SyncMetrics metrics = new SyncMetrics();
        updateCheckpointStatus(checkpoint, "IN_PROGRESS");
        
        try {
            LocalDateTime from = checkpoint.getLastSyncAt();
            LocalDateTime to = LocalDateTime.now();
            
            int processedOrders = ozonService.backfillAllOrders(
                    new DateRangeDto(from.toString(), to.toString()), 
                    batchSize
            );
            
            updateCheckpointSuccess(checkpoint, processedOrders, metrics.getDuration());
            
            log.info("✅ Catch-up sync completed for company {}: {} orders processed in {} ms",
                    companyId, processedOrders, metrics.getDuration());
            
        } catch (Exception e) {
            updateCheckpointFailure(checkpoint, e.getMessage(), metrics.getDuration());
            log.error("❌ Catch-up sync failed for company {}: {}", companyId, e.getMessage(), e);
            throw new SyncException("Catch-up sync failed for company " + companyId, e);
        }
    }
    
    /**
     * Принудительно запустить синхронизацию для всех компаний
     */
    @Transactional
    public void forceSyncAllCompanies() {
        log.info("🔄 Force sync requested for all companies");
        
        List<CompanyCredentials> companies = getCompaniesWithCredentials();
        
        if (companies.isEmpty()) {
            log.warn("⚠️ No companies found with credentials");
            return;
        }
        
        log.info("🏢 Force syncing {} companies", companies.size());
        
        if (asyncEnabled) {
            processCompaniesAsync(companies, this::forceSyncForCompany);
        } else {
            companies.forEach(this::forceSyncForCompany);
        }
    }
    
    /**
     * Принудительная синхронизация для конкретной компании
     */
    private void forceSyncForCompany(CompanyCredentials company) {
        try (CompanyContext context = new CompanyContext(company)) {
            performInitialSyncForCompany(company);
        } catch (Exception e) {
            log.error("❌ Force sync failed for company {}: {}", 
                    company.getCompanyId(), e.getMessage(), e);
        }
    }
    
    // Вспомогательные методы
    
    private List<CompanyCredentials> getCompaniesWithCredentials() {
        return companyCredentialsService.findAll();
    }
    
    private Optional<SyncCheckpoint> getCheckpointForCompany(String companyId) {
        return syncCheckpointRepository.findByCheckpointName(checkpointName + "_" + companyId);
    }
    
    private SyncCheckpoint createCheckpoint(String companyId, String status) {
        SyncCheckpoint checkpoint = new SyncCheckpoint(checkpointName + "_" + companyId, LocalDateTime.now());
        checkpoint.setStatus(status);
        return syncCheckpointRepository.save(checkpoint);
    }
    
    private void updateCheckpointSuccess(SyncCheckpoint checkpoint, int ordersProcessed, long duration) {
        checkpoint.setOrdersProcessed(ordersProcessed);
        checkpoint.setStatus("SUCCESS");
        checkpoint.setLastSyncAt(LocalDateTime.now());
        checkpoint.setSyncDurationMs(duration);
        syncCheckpointRepository.save(checkpoint);
    }
    
    private void updateCheckpointFailure(SyncCheckpoint checkpoint, String errorMessage, long duration) {
        checkpoint.setStatus("FAILED");
        checkpoint.setErrorMessage(errorMessage);
        checkpoint.setSyncDurationMs(duration);
        syncCheckpointRepository.save(checkpoint);
    }
    
    private void updateCheckpointStatus(SyncCheckpoint checkpoint, String status) {
        checkpoint.setStatus(status);
        syncCheckpointRepository.save(checkpoint);
    }
    
    // Публичные методы для получения информации
    
    public Optional<SyncCheckpoint> getLastSyncInfo() {
        return syncCheckpointRepository.findByCheckpointName(checkpointName);
    }
    
    public Optional<SyncCheckpoint> getLastSyncInfoForCompany(UUID companyId) {
        return syncCheckpointRepository.findByCheckpointName(checkpointName + "_" + companyId);
    }
    
    // Внутренние классы для улучшения архитектуры
    
    /**
     * Функциональный интерфейс для синхронизации компании
     */
    @FunctionalInterface
    private interface CompanySyncFunction {
        void apply(CompanyCredentials company);
    }
    
    /**
     * Автоматическое управление контекстом компании
     */
    private static class CompanyContext implements AutoCloseable {
        private final String companyId;
        private final String userId;
        
        public CompanyContext(CompanyCredentials company) {
            this.companyId = company.getCompanyId().toString();
            this.userId = company.getUserId().toString();
            CompanyContextHolder.setContext(companyId, userId);
        }
        
        @Override
        public void close() {
            CompanyContextHolder.clear();
        }
    }
    
    /**
     * Метрики синхронизации
     */
    private static class SyncMetrics {
        private final long startTime = System.currentTimeMillis();
        
        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    /**
     * Кастомное исключение для синхронизации
     */
    public static class SyncException extends RuntimeException {
        public SyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
