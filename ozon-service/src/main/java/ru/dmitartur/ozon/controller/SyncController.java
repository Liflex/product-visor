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
@RequestMapping("/api/ozon/sync")
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

}
