package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.dto.BulkStockSyncRequest;
import ru.dmitartur.service.BulkStockSyncService;
import ru.dmitartur.common.utils.JwtUtil;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/bulk-stock-sync")
@RequiredArgsConstructor
public class BulkStockSyncController {
    
    private final BulkStockSyncService bulkStockSyncService;
    
    /**
     * Массовая синхронизация остатков выбранных продуктов
     */
    @PostMapping("/sync")
    public ResponseEntity<String> bulkSyncStocks(@RequestBody BulkStockSyncRequest request) {
        try {
            UUID userId = JwtUtil.getRequiredOwnerId();
            
            log.info("🔄 Bulk stock sync request for {} products with type: {}", 
                    request.getProductIds().size(), request.getStockType());
            
            bulkStockSyncService.syncStocksForProducts(request.getProductIds(), request.getStockType(), userId);
            
            return ResponseEntity.ok("Bulk stock sync request sent successfully for " + 
                    request.getProductIds().size() + " products");
                    
        } catch (Exception e) {
            log.error("❌ Error in bulk stock sync: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error in bulk stock sync: " + e.getMessage());
        }
    }
    
    /**
     * Получить статистику синхронизации для выбранных продуктов
     */
    @PostMapping("/stats")
    public ResponseEntity<Object> getSyncStats(@RequestBody BulkStockSyncRequest request) {
        try {
            UUID userId = JwtUtil.getRequiredOwnerId();
            
            log.info("📊 Getting sync stats for {} products with type: {}", 
                    request.getProductIds().size(), request.getStockType());
            
            Object stats = bulkStockSyncService.getSyncStats(request.getProductIds(), request.getStockType(), userId);
            
            return ResponseEntity.ok(stats);
                    
        } catch (Exception e) {
            log.error("❌ Error getting sync stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error getting sync stats: " + e.getMessage());
        }
    }
}

