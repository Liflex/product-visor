package ru.dmitartur.library.marketplace.scheduled;

import java.util.Optional;

/**
 * Базовый интерфейс для планировщиков синхронизации маркетплейсов
 * Определяет общие методы для управления синхронизацией
 */
public interface BaseMarketplaceScheduler {
    
    /**
     * Запустить принудительную синхронизацию
     */
    void forceSync();
    
    /**
     * Получить информацию о последней синхронизации
     */
    Optional<SyncCheckpoint> getLastSyncInfo();
    
    /**
     * Запустить автоматическую синхронизацию
     */
    void startAutoSync();
    
    /**
     * Остановить автоматическую синхронизацию
     */
    void stopAutoSync();
    
    /**
     * Проверить, выполняется ли синхронизация
     */
    boolean isSyncRunning();
    
    /**
     * Получить название маркетплейса
     */
    String getMarketplaceName();
    
    /**
     * Класс для хранения информации о точке синхронизации
     */
    class SyncCheckpoint {
        private final String status;
        private final java.time.LocalDateTime lastSyncAt;
        private final int ordersProcessed;
        private final long syncDurationMs;
        private final String errorMessage;
        
        public SyncCheckpoint(String status, java.time.LocalDateTime lastSyncAt, 
                            int ordersProcessed, long syncDurationMs, String errorMessage) {
            this.status = status;
            this.lastSyncAt = lastSyncAt;
            this.ordersProcessed = ordersProcessed;
            this.syncDurationMs = syncDurationMs;
            this.errorMessage = errorMessage;
        }
        
        public String getStatus() { return status; }
        public java.time.LocalDateTime getLastSyncAt() { return lastSyncAt; }
        public int getOrdersProcessed() { return ordersProcessed; }
        public long getSyncDurationMs() { return syncDurationMs; }
        public String getErrorMessage() { return errorMessage; }
    }
}

