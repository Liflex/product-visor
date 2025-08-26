package ru.dmitartur.ozon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.dto.marketplace.StockItemReqest;
import ru.dmitartur.common.dto.marketplace.StockSyncResponse;
import ru.dmitartur.library.marketplace.service.BaseStockSyncService;
import ru.dmitartur.ozon.integration.OzonApi;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonStockSyncService implements BaseStockSyncService {
    
    private final OzonApi ozonApi;
    
    /**
     * Синхронизировать один товар с Ozon API (новый метод для StockItemReqest)
     */
    public StockSyncResponse syncStock(StockItemReqest request) {
        log.info("🔄 Starting Ozon stock sync for offerId: {}, quantity: {}", 
                request.getOfferId(), request.getQuantity());
        
        try {
            // Фильтруем только Ozon склады
            List<StockItemReqest.WarehouseSyncInfo> ozonWarehouses = request.getWarehouses().stream()
                    .filter(warehouse -> "OZON".equalsIgnoreCase(warehouse.getMarketplace()))
                    .collect(Collectors.toList());
            
            if (ozonWarehouses.isEmpty()) {
                log.warn("⚠️ No Ozon warehouses found for offerId: {}", request.getOfferId());
                return StockSyncResponse.builder()
                        .marketplace("OZON")
                        .status("SKIPPED")
                        .processedAt(LocalDateTime.now())
                        .totalItems(1)
                        .successCount(0)
                        .failedCount(0)
                        .errorMessage("No Ozon warehouses found")
                        .build();
            }
            
            List<StockSyncResponse.StockSyncResult> results = new ArrayList<>();
            int successCount = 0;
            int failedCount = 0;
            
            // Обновляем остатки для каждого Ozon склада
            for (StockItemReqest.WarehouseSyncInfo warehouse : ozonWarehouses) {
                try {
                    log.debug("🔄 Updating stock for offerId: {}, quantity: {}, warehouseId: {}", 
                            request.getOfferId(), request.getQuantity(), warehouse.getWarehouseId());
                    
                    // Вызываем API для обновления остатков
                    var response = ozonApi.updateStock(
                        request.getOfferId(), 
                        request.getQuantity(), 
                        warehouse.getWarehouseId()
                    );
                    
                    // Проверяем успешность операции
                    if (response != null && !response.has("error")) {
                        results.add(StockSyncResponse.StockSyncResult.builder()
                                .offerId(request.getOfferId())
                                .sku(request.getProductId()) // Используем productId как SKU
                                .status("SUCCESS")
                                .newQuantity(request.getQuantity())
                                .build());
                        successCount++;
                        log.debug("✅ Stock updated successfully for offerId: {}, warehouseId: {}", 
                                request.getOfferId(), warehouse.getWarehouseId());
                    } else {
                        String errorMessage = response != null && response.has("error") ? 
                            response.get("error").toString() : "Unknown error";
                        
                        results.add(StockSyncResponse.StockSyncResult.builder()
                                .offerId(request.getOfferId())
                                .sku(request.getProductId())
                                .status("FAILED")
                                .errorMessage(errorMessage)
                                .newQuantity(request.getQuantity())
                                .build());
                        failedCount++;
                        log.error("❌ Failed to update stock for offerId: {}, warehouseId: {}, error: {}", 
                                request.getOfferId(), warehouse.getWarehouseId(), errorMessage);
                    }
                    
                } catch (Exception e) {
                    results.add(StockSyncResponse.StockSyncResult.builder()
                            .offerId(request.getOfferId())
                            .sku(request.getProductId())
                            .status("FAILED")
                            .errorMessage(e.getMessage())
                            .newQuantity(request.getQuantity())
                            .build());
                    failedCount++;
                    log.error("❌ Exception updating stock for offerId: {}, warehouseId: {}, error: {}", 
                            request.getOfferId(), warehouse.getWarehouseId(), e.getMessage());
                }
            }
            
            String status = failedCount == 0 ? "SUCCESS" : 
                           successCount == 0 ? "FAILED" : "PARTIAL_SUCCESS";
            
            StockSyncResponse response = StockSyncResponse.builder()
                    .marketplace("OZON")
                    .status(status)
                    .processedAt(LocalDateTime.now())
                    .totalItems(1)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .results(results)
                    .build();
            
            log.info("✅ Ozon stock sync completed for offerId: {} - {} success, {} failed", 
                    request.getOfferId(), successCount, failedCount);
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Critical error during Ozon stock sync for offerId: {}", 
                    request.getOfferId(), e.getMessage(), e);
            
            return StockSyncResponse.builder()
                    .marketplace("OZON")
                    .status("FAILED")
                    .processedAt(LocalDateTime.now())
                    .totalItems(1)
                    .successCount(0)
                    .failedCount(1)
                    .errorMessage("Critical error: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public StockSyncResponse syncStocks(StockItemReqest request) {
        // Делегируем к новому методу для совместимости
        return syncStock(request);
    }
    
    @Override
    public String getMarketplaceName() {
        return "OZON";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            return ozonApi.testConnection();
        } catch (Exception e) {
            log.error("❌ Ozon API connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
