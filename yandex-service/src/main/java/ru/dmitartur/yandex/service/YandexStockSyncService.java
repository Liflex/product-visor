package ru.dmitartur.yandex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.dto.marketplace.StockItemReqest;
import ru.dmitartur.common.dto.marketplace.StockSyncResponse;
import ru.dmitartur.library.marketplace.service.CompanyCredentialsService;
import ru.dmitartur.yandex.integration.YandexApi;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexStockSyncService {
    
    private final YandexApi yandexApi;
    private final CompanyCredentialsService credentialsService;
    
    /**
     * Синхронизировать один товар с Yandex API (новый метод для StockItemReqest)
     */
    public StockSyncResponse syncStock(StockItemReqest request) {
        log.info("🔄 Starting Yandex stock sync for offerId: {}, quantity: {}", 
                request.getOfferId(), request.getQuantity());
        
        try {
            // Фильтруем только Yandex склады
            List<StockItemReqest.WarehouseSyncInfo> yandexWarehouses = request.getWarehouses().stream()
                    .filter(warehouse -> "YANDEX".equalsIgnoreCase(warehouse.getMarketplace()))
                    .collect(Collectors.toList());
            
            if (yandexWarehouses.isEmpty()) {
                log.warn("⚠️ No Yandex warehouses found for offerId: {}", request.getOfferId());
                return StockSyncResponse.builder()
                        .marketplace("YANDEX")
                        .status("SKIPPED")
                        .processedAt(LocalDateTime.now())
                        .totalItems(1)
                        .successCount(0)
                        .failedCount(0)
                        .errorMessage("No Yandex warehouses found")
                        .build();
            }
            
            // Получаем access token из CompanyCredentials (берем из первого склада)
            UUID companyId = yandexWarehouses.get(0).getCompanyId();
            Optional<String> accessTokenOpt = credentialsService.getValidAccessToken(companyId, "YANDEX");
            if (accessTokenOpt.isEmpty()) {
                String error = "No valid access token found for company: " + companyId;
                log.error("❌ {}", error);
                return StockSyncResponse.builder()
                        .marketplace("YANDEX")
                        .status("FAILED")
                        .processedAt(LocalDateTime.now())
                        .totalItems(1)
                        .successCount(0)
                        .failedCount(1)
                        .errorMessage(error)
                        .build();
            }
            
            String accessToken = accessTokenOpt.get();
            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new java.util.ArrayList<>();
            
            // Обновляем остатки для каждого Yandex склада
            for (StockItemReqest.WarehouseSyncInfo warehouse : yandexWarehouses) {
                try {
                    log.debug("🔄 Updating stock for offerId: {}, quantity: {}, warehouseId: {}", 
                            request.getOfferId(), request.getQuantity(), warehouse.getWarehouseId());
                    
                    boolean success = yandexApi.updateStock(
                            request.getOfferId(),
                            request.getProductId(), // Используем productId как SKU
                            request.getQuantity(),
                            warehouse.getWarehouseId(),
                            accessToken
                    );
                    
                    if (success) {
                        successCount++;
                        log.debug("✅ Stock updated successfully for offerId: {}, warehouseId: {}", 
                                request.getOfferId(), warehouse.getWarehouseId());
                    } else {
                        failedCount++;
                        String error = String.format("Failed to update stock for offerId: %s, warehouseId: %s", 
                                request.getOfferId(), warehouse.getWarehouseId());
                        errors.add(error);
                        log.warn("❌ {}", error);
                    }
                    
                } catch (Exception e) {
                    failedCount++;
                    String error = String.format("Exception updating stock for offerId: %s, warehouseId: %s - %s", 
                            request.getOfferId(), warehouse.getWarehouseId(), e.getMessage());
                    errors.add(error);
                    log.error("❌ {}", error, e);
                }
            }
            
            String status = failedCount == 0 ? "SUCCESS" : 
                           successCount == 0 ? "FAILED" : "PARTIAL_SUCCESS";
            
            StockSyncResponse response = StockSyncResponse.builder()
                    .marketplace("YANDEX")
                    .status(status)
                    .processedAt(LocalDateTime.now())
                    .totalItems(1)
                    .successCount(successCount)
                    .failedCount(failedCount)
                    .errorMessage(failedCount > 0 ? String.join("; ", errors) : null)
                    .build();
            
            log.info("✅ Yandex stock sync completed for offerId: {} - {} successful, {} failed", 
                    request.getOfferId(), successCount, failedCount);
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ Critical error during Yandex stock sync for offerId: {}", 
                    request.getOfferId(), e.getMessage(), e);
            
            return StockSyncResponse.builder()
                    .marketplace("YANDEX")
                    .status("FAILED")
                    .processedAt(LocalDateTime.now())
                    .totalItems(1)
                    .successCount(0)
                    .failedCount(1)
                    .errorMessage("Critical error: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * Синхронизировать остатки товаров с Yandex API (старый метод для совместимости)
     */
    public StockSyncResponse syncStocks(StockItemReqest request) {
        // Делегируем к новому методу для совместимости
        return syncStock(request);
    }
}
