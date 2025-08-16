package ru.dmitartur.ozon.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.ozon.entity.SyncCheckpoint;
import ru.dmitartur.ozon.repository.SyncCheckpointRepository;
import ru.dmitartur.ozon.service.OzonService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OzonBackfillScheduler {
    private final OzonService ozonService;
    private final SyncCheckpointRepository syncCheckpointRepository;
    
    @Value("${app.sync.checkpoint-name:FBO_ORDERS}")
    private String checkpointName;
    
    @Value("${app.sync.max-gap-hours:1}")
    private int maxGapHours; // Максимальный разрыв в часах для автоматической синхронизации
    
    @Value("${app.sync.enabled:true}")
    private boolean syncEnabled;

    @Autowired
    @Lazy
    private OzonBackfillScheduler ozonBackfillScheduler;

    /**
     * Проверить необходимость синхронизации при старте приложения
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE) // Запуск через 30 секунд после старта
    public void checkSyncOnStartup() {
        if (!syncEnabled) {
            log.info("🔄 Sync service is disabled");
            return;
        }
        
        log.info("🔄 Checking sync status on startup...");
        
        try {
            Optional<SyncCheckpoint> checkpoint = syncCheckpointRepository.findByCheckpointName(checkpointName);
            
            if (checkpoint.isEmpty()) {
                log.info("🆕 No sync checkpoint found, performing initial sync");
                ozonBackfillScheduler.performInitialSync();
            } else {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), OffsetDateTime.now());
                
                if (gap.toHours() > maxGapHours) {
                    log.warn("⚠️ Large sync gap detected: {} hours, performing catch-up sync", gap.toHours());
                    ozonBackfillScheduler.performCatchUpSync(cp);
                } else {
                    log.info("✅ Sync is up to date, last sync: {} hours ago", gap.toHours());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during startup sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Периодическая проверка синхронизации (каждый час)
     */
    @Scheduled(fixedRate = 3600000) // Каждый час
    public void periodicSyncCheck() {
        if (!syncEnabled) {
            return;
        }
        
        log.debug("🔄 Performing periodic sync check...");
        
        try {
            Optional<SyncCheckpoint> checkpoint = syncCheckpointRepository.findByCheckpointName(checkpointName);
            
            if (checkpoint.isPresent()) {
                SyncCheckpoint cp = checkpoint.get();
                Duration gap = Duration.between(cp.getLastSyncAt(), OffsetDateTime.now());
                
                if (gap.toHours() > maxGapHours) {
                    log.info("🔄 Periodic sync triggered, gap: {} hours", gap.toHours());
                    performCatchUpSync(cp);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error during periodic sync check: {}", e.getMessage(), e);
        }
    }

    /**
     * Выполнить начальную синхронизацию
     */
    @Transactional
    public void performInitialSync() {
        log.info("🚀 Starting initial sync...");
        
        long startTime = System.currentTimeMillis();
        SyncCheckpoint checkpoint = new SyncCheckpoint(checkpointName, OffsetDateTime.now());
        checkpoint.setStatus("IN_PROGRESS");
        checkpoint = syncCheckpointRepository.save(checkpoint);
        
        try {
            // Синхронизируем заказы за последние полгода для первого запуска
            OffsetDateTime from = OffsetDateTime.now().minusDays(180);
            OffsetDateTime to = OffsetDateTime.now();
            
            log.info("🚀 Initial sync: loading FBO + FBS orders for the last 6 months ({} to {})", from, to);
            
            int processedOrders = ozonService.backfillAllOrders(
                new ru.dmitartur.ozon.dto.DateRangeDto(from.toString(), to.toString()), 1000);
            
            checkpoint.setOrdersProcessed(processedOrders);
            checkpoint.setStatus("SUCCESS");
            checkpoint.setLastSyncAt(OffsetDateTime.now());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            
            syncCheckpointRepository.save(checkpoint);
            
            log.info("✅ Initial sync completed: {} FBO + FBS orders processed in {} ms", 
                    processedOrders, checkpoint.getSyncDurationMs());
            
        } catch (Exception e) {
            checkpoint.setStatus("FAILED");
            checkpoint.setErrorMessage(e.getMessage());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            syncCheckpointRepository.save(checkpoint);
            
            log.error("❌ Initial sync failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Выполнить догоняющую синхронизацию
     */
    @Transactional
    public void performCatchUpSync(SyncCheckpoint checkpoint) {
        log.info("🔄 Starting catch-up sync from {}", checkpoint.getLastSyncAt());
        
        long startTime = System.currentTimeMillis();
        checkpoint.setStatus("IN_PROGRESS");
        checkpoint = syncCheckpointRepository.save(checkpoint);
        
        try {
            OffsetDateTime from = checkpoint.getLastSyncAt();
            OffsetDateTime to = OffsetDateTime.now();
            
            int processedOrders = ozonService.backfillAllOrders(
                new ru.dmitartur.ozon.dto.DateRangeDto(from.toString(), to.toString()), 1000);
            
            checkpoint.setOrdersProcessed(processedOrders);
            checkpoint.setStatus("SUCCESS");
            checkpoint.setLastSyncAt(OffsetDateTime.now());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            
            syncCheckpointRepository.save(checkpoint);
            
            log.info("✅ Catch-up sync completed: {} FBO + FBS orders processed in {} ms", 
                    processedOrders, checkpoint.getSyncDurationMs());
            
        } catch (Exception e) {
            checkpoint.setStatus("FAILED");
            checkpoint.setErrorMessage(e.getMessage());
            checkpoint.setSyncDurationMs(System.currentTimeMillis() - startTime);
            syncCheckpointRepository.save(checkpoint);
            
            log.error("❌ Catch-up sync failed: {}", e.getMessage(), e);
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
     * Принудительно запустить синхронизацию
     */
    @Transactional
    public void forceSync() {
        log.info("🔄 Force sync requested - will sync FBO + FBS orders for the last 6 months");
        performInitialSync();
    }
}





