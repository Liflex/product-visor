package ru.dmitartur.ozon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dmitartur.ozon.entity.SyncCheckpoint;
import ru.dmitartur.ozon.scheduled.OzonBackfillScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Контроллер для управления синхронизацией с Ozon API
 */
@Slf4j
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final OzonBackfillScheduler ozonBackfillScheduler;

    /**
     * Получить информацию о последней синхронизации
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Optional<SyncCheckpoint> checkpoint = ozonBackfillScheduler.getLastSyncInfo();
        
        Map<String, Object> response = new HashMap<>();
        
        if (checkpoint.isPresent()) {
            SyncCheckpoint cp = checkpoint.get();
            response.put("checkpointName", cp.getCheckpointName());
            response.put("lastSyncAt", cp.getLastSyncAt());
            response.put("status", cp.getStatus());
            response.put("ordersProcessed", cp.getOrdersProcessed());
            response.put("syncDurationMs", cp.getSyncDurationMs());
            response.put("errorMessage", cp.getErrorMessage());
            response.put("updatedAt", cp.getUpdatedAt());
        } else {
            response.put("message", "No sync checkpoint found");
            response.put("status", "NOT_INITIALIZED");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Принудительно запустить синхронизацию
     */
    @PostMapping("/force")
    public ResponseEntity<Map<String, Object>> forceSync() {
        log.info("🔄 Force sync requested via API");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ozonBackfillScheduler.forceSync();
            response.put("message", "Sync started successfully");
            response.put("status", "STARTED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Force sync failed: {}", e.getMessage(), e);
            response.put("message", "Sync failed: " + e.getMessage());
            response.put("status", "FAILED");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Проверить необходимость синхронизации
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSyncNeeded() {
        Optional<SyncCheckpoint> checkpoint = ozonBackfillScheduler.getLastSyncInfo();
        
        Map<String, Object> response = new HashMap<>();
        
        if (checkpoint.isEmpty()) {
            response.put("syncNeeded", true);
            response.put("reason", "No sync checkpoint found");
            response.put("recommendation", "Perform initial sync");
        } else {
            SyncCheckpoint cp = checkpoint.get();
            long hoursSinceLastSync = java.time.Duration.between(cp.getLastSyncAt(), java.time.OffsetDateTime.now()).toHours();
            
            response.put("lastSyncAt", cp.getLastSyncAt());
            response.put("hoursSinceLastSync", hoursSinceLastSync);
            response.put("syncNeeded", hoursSinceLastSync > 24); // 24 часа - максимальный разрыв
            response.put("reason", hoursSinceLastSync > 24 ? "Large sync gap detected" : "Sync is up to date");
            response.put("recommendation", hoursSinceLastSync > 24 ? "Perform catch-up sync" : "No action needed");
        }
        
        return ResponseEntity.ok(response);
    }
}
