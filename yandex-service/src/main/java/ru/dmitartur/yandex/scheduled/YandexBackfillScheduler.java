package ru.dmitartur.yandex.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
import ru.dmitartur.yandex.service.YandexService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class YandexBackfillScheduler {
    
    private final YandexService yandexService;
    
    @Value("${app.sync.checkpoint-name:YANDEX_ORDERS}")
    private String checkpointName;
    
    @Value("${app.sync.max-gap-minutes:60}")
    private int maxGapMinutes;
    
    @Value("${app.sync.enabled:true}")
    private boolean syncEnabled;

    private LocalDateTime lastSyncAt;
    private String lastSyncStatus = "NEVER_SYNCED";
    private int lastOrdersProcessed = 0;
    private long lastSyncDurationMs = 0;
    private String lastErrorMessage = null;

    /**
     * Проверить необходимость синхронизации при старте приложения
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void checkSyncOnStartup() {
        if (!syncEnabled) {
            log.info("🔄 Yandex sync service is disabled");
            return;
        }
        
        log.info("🔄 Checking Yandex sync status on startup...");
        
        try {
            if (lastSyncAt == null) {
                log.info("🆕 No sync checkpoint found, performing initial sync");
                performInitialSync();
            } else {
                long gapHours = java.time.Duration.between(lastSyncAt, LocalDateTime.now()).toHours();
                
                if (gapHours > maxGapMinutes / 60) {
                    log.warn("⚠️ Large sync gap detected: {} hours, performing catch-up sync", gapHours);
                    performCatchUpSync();
                } else {
                    log.info("✅ Yandex sync is up to date, last sync: {} hours ago", gapHours);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during startup sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Периодическая проверка синхронизации
     */
    @Scheduled(fixedRate = 120000) // Каждые 2 минуты
    public void periodicSyncCheck() {
        if (!syncEnabled) {
            return;
        }
        
        log.debug("🔄 Performing periodic Yandex sync check...");
        
        try {
            if (lastSyncAt != null) {
                long gapMinutes = java.time.Duration.between(lastSyncAt, LocalDateTime.now()).toMinutes();
                
                if (gapMinutes > maxGapMinutes) {
                    log.info("🔄 Periodic sync triggered, gap: {} hours", gapMinutes / 60);
                    performCatchUpSync();
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during periodic sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Выполнить начальную синхронизацию
     */
    public void performInitialSync() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = now.minusDays(7); // Синхронизируем за последние 7 дней
        
        performSync(from, now, "INITIAL_SYNC");
    }

    /**
     * Выполнить синхронизацию для восполнения пропусков
     */
    public void performCatchUpSync() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from = lastSyncAt != null ? lastSyncAt : now.minusDays(1);
        
        performSync(from, now, "CATCH_UP_SYNC");
    }

    /**
     * Выполнить синхронизацию
     */
    private void performSync(LocalDateTime from, LocalDateTime to, String syncType) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("🔄 Starting {} sync from {} to {}", syncType, from, to);
            
            DateRangeDto range = new DateRangeDto(
                from.format(DateTimeFormatter.ISO_DATE_TIME),
                to.format(DateTimeFormatter.ISO_DATE_TIME)
            );
            
            int processed = yandexService.backfillAllOrders(range, 100);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Обновляем информацию о синхронизации
            this.lastSyncAt = to;
            this.lastSyncStatus = "SUCCESS";
            this.lastOrdersProcessed = processed;
            this.lastSyncDurationMs = duration;
            this.lastErrorMessage = null;
            
            log.info("✅ {} sync completed: {} orders processed in {} ms", 
                    syncType, processed, duration);
                    
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            this.lastSyncStatus = "FAILED";
            this.lastOrdersProcessed = 0;
            this.lastSyncDurationMs = duration;
            this.lastErrorMessage = e.getMessage();
            
            log.error("❌ {} sync failed: {}", syncType, e.getMessage(), e);
        }
    }

    /**
     * Получить информацию о последней синхронизации
     */
    public Optional<SyncCheckpoint> getLastSyncInfo() {
        if (lastSyncAt == null) {
            return Optional.empty();
        }
        
        return Optional.of(new SyncCheckpoint(
            lastSyncStatus,
            lastSyncAt,
            lastOrdersProcessed,
            lastSyncDurationMs,
            lastErrorMessage
        ));
    }

    /**
     * Принудительно запустить синхронизацию для всех компаний
     */
    public void forceSyncAllCompanies() {
        log.info("🔄 Force sync for all companies requested");
        performInitialSync();
    }

    /**
     * Класс для хранения информации о точке синхронизации
     */
    public static class SyncCheckpoint {
        private final String status;
        private final LocalDateTime lastSyncAt;
        private final int ordersProcessed;
        private final long syncDurationMs;
        private final String errorMessage;
        
        public SyncCheckpoint(String status, LocalDateTime lastSyncAt, 
                            int ordersProcessed, long syncDurationMs, String errorMessage) {
            this.status = status;
            this.lastSyncAt = lastSyncAt;
            this.ordersProcessed = ordersProcessed;
            this.syncDurationMs = syncDurationMs;
            this.errorMessage = errorMessage;
        }
        
        public String getStatus() { return status; }
        public LocalDateTime getLastSyncAt() { return lastSyncAt; }
        public int getOrdersProcessed() { return ordersProcessed; }
        public long getSyncDurationMs() { return syncDurationMs; }
        public String getErrorMessage() { return errorMessage; }
    }
}
