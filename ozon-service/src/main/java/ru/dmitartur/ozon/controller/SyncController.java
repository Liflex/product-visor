package ru.dmitartur.ozon.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.ozon.entity.SyncCheckpoint;
import ru.dmitartur.ozon.scheduled.OzonBackfillScheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/sync")
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
            // Конвертируем время в московский timezone для отображения

            response.put("checkpointName", cp.getCheckpointName());
            response.put("lastSyncAt", cp.getLastSyncAt()); // В московском времени
            response.put("status", cp.getStatus());
            response.put("ordersProcessed", cp.getOrdersProcessed());
            response.put("syncDurationMs", cp.getSyncDurationMs());
            response.put("errorMessage", cp.getErrorMessage());
            response.put("updatedAt", cp.getUpdatedAt()); // В московском времени
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
        Map<String, Object> response = new HashMap<>();
        
        try {
            ozonBackfillScheduler.forceSync();
            response.put("message", "Force sync started successfully");
            response.put("status", "STARTED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to start force sync: " + e.getMessage());
            response.put("status", "ERROR");
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
            long hoursSinceLastSync = java.time.Duration.between(cp.getLastSyncAt(), java.time.LocalDateTime.now()).toHours();
            
            response.put("lastSyncAt", cp.getLastSyncAt());
            response.put("hoursSinceLastSync", hoursSinceLastSync);
            response.put("syncNeeded", hoursSinceLastSync > 24); // 24 часа - максимальный разрыв
            response.put("reason", hoursSinceLastSync > 24 ? "Large sync gap detected" : "Sync is up to date");
            response.put("recommendation", hoursSinceLastSync > 24 ? "Perform catch-up sync" : "No action needed");
        }
        
        return ResponseEntity.ok(response);
    }
}
