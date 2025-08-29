package ru.dmitartur.ozon.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.library.marketplace.scheduled.BaseMarketplaceScheduler;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OzonScheduler implements BaseMarketplaceScheduler {
    
    private final OzonBackfillSchedulerV2 ozonBackfillScheduler;

    @Override
    public void forceSync() {
        log.info("🔄 Force sync requested for Ozon");
        ozonBackfillScheduler.forceSyncAllCompanies();
    }
    
    @Override
    public Optional<SyncCheckpoint> getLastSyncInfo() {
        try {
            var checkpoint = ozonBackfillScheduler.getLastSyncInfo();
            if (checkpoint.isPresent()) {
                var cp = checkpoint.get();
                return Optional.of(new SyncCheckpoint(
                    cp.getStatus(),
                    cp.getLastSyncAt(),
                    cp.getOrdersProcessed(),
                    cp.getSyncDurationMs(),
                    cp.getErrorMessage()
                ));
            }
        } catch (Exception e) {
            log.error("❌ Error getting last sync info: {}", e.getMessage());
        }
        return Optional.empty();
    }
    
    @Override
    public void startAutoSync() {
        log.info("🔄 Auto sync started for Ozon");
        // OzonBackfillScheduler уже имеет автоматическую синхронизацию
    }
    
    @Override
    public void stopAutoSync() {
        log.info("🔄 Auto sync stopped for Ozon");
        // Можно добавить флаг для остановки автоматической синхронизации
    }
    
    @Override
    public boolean isSyncRunning() {
        // Можно добавить флаг для отслеживания состояния синхронизации
        return false;
    }
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    /**
     * Принудительно запустить синхронизацию для всех компаний
     */
    public void forceSyncAllCompanies() {
        log.info("🔄 Force sync requested for all companies");
        ozonBackfillScheduler.forceSyncAllCompanies();
    }
}

