package ru.dmitartur.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.entity.Warehouse;
import ru.dmitartur.service.ProductHistoryService;
import ru.dmitartur.kafka.StockEventProducer;
import ru.dmitartur.context.ChangeContextHolder;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.util.ChangeReasonUtil;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Interceptor для отслеживания изменений Product
 * Более простой подход без AOP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHistoryInterceptor {
    
    private final ProductHistoryService productHistoryService;
    private final StockEventProducer stockEventProducer;

    /**
     * Отследить изменение количества в ProductStock (новый метод для ManyToMany)
     */
    public void trackProductStockQuantityChange(ProductStock stock, Integer oldQuantity, Integer newQuantity,
                                               String changeReason, String sourceSystem, String sourceId) {
        try {
            Product product = stock.getProduct();
            Set<Warehouse> warehouses = stock.getWarehouses();
            
            log.info("🔄 Starting ProductStock quantity change tracking: stockId={}, productId={}, productArticle={}, oldQty={}, newQty={}, changeReason={}, sourceSystem={}", 
                    stock.getId(), product.getId(), product.getArticle(), oldQuantity, newQuantity, changeReason, sourceSystem);
            
            if (warehouses == null || warehouses.isEmpty()) {
                log.warn("⚠️ ProductStock {} has no warehouses, skipping history tracking", stock.getId());
                return;
            }

            log.debug("📦 Processing {} warehouses for stock change: stockId={}, warehouses={}", 
                    warehouses.size(), stock.getId(), 
                    warehouses.stream().map(w -> w.getId() + "(" + w.getName() + ")").collect(Collectors.joining(", ")));

            // Создаем метаданные для всех складов
            String warehouseMetadata = warehouses.stream()
                    .map(warehouse -> String.format(
                            "{\"warehouseId\":\"%s\",\"externalWarehouseId\":\"%s\",\"warehouseType\":\"%s\",\"marketplace\":\"%s\"}",
                            warehouse.getId() != null ? warehouse.getId().toString() : "",
                            warehouse.getExternalWarehouseId() != null ? warehouse.getExternalWarehouseId() : "",
                            warehouse.getWarehouseType() != null ? warehouse.getWarehouseType().name() : "",
                            warehouse.getMarketplace() != null ? warehouse.getMarketplace().name() : ""
                    ))
                    .collect(Collectors.joining(","));
            
            String metadata = String.format("{\"warehouses\":[%s],\"stockType\":\"%s\"}", 
                    warehouseMetadata, stock.getStockType().name());

            // Определяем причину изменения и систему-источник
            String finalChangeReason = ChangeReasonUtil.determineChangeReason(sourceSystem, sourceId);
            String finalSourceSystem = ChangeReasonUtil.determineSourceSystem(finalChangeReason, sourceId);
            
            // Получаем информацию о пользователе и компании
            UUID userId = null;
            UUID companyId = null;
            
            try {
                userId = JwtUtil.getRequiredOwnerId();
                var companyIdOpt = JwtUtil.resolveEffectiveCompanyId();
                if (companyIdOpt.isPresent()) {
                    companyId = UUID.fromString(companyIdOpt.get());
                }
            } catch (Exception e) {
                log.warn("⚠️ Could not get user/company info for history tracking: {}", e.getMessage());
            }

            log.debug("💾 Saving history with metadata: productId={}, metadata={}, changeReason={}, sourceSystem={}, userId={}, companyId={}", 
                    product.getId(), metadata, finalChangeReason, finalSourceSystem, userId, companyId);

            // Сохраняем историю с информацией о пользователе
            if (userId != null) {
                productHistoryService.saveHistoryWithUserInfo(
                        product.getId(),
                        "product_stock.quantity",
                        String.valueOf(oldQuantity),
                        String.valueOf(newQuantity),
                        finalChangeReason,
                        finalSourceSystem,
                        sourceId,
                        metadata,
                        userId,
                        companyId
                );
            } else {
                // Fallback для случаев, когда нет информации о пользователе
                productHistoryService.saveHistoryWithMetadata(
                        product.getId(),
                        "product_stock.quantity",
                        String.valueOf(oldQuantity),
                        String.valueOf(newQuantity),
                        finalChangeReason,
                        finalSourceSystem,
                        sourceId,
                        metadata
                );
            }

            log.info("✅ History saved successfully for productId={}, stockId={}", product.getId(), stock.getId());

            // Отправляем события для каждого склада
            ChangeContextHolder.ChangeContext ctx = ChangeContextHolder.get();
            String originMarket = (ctx != null) ? ctx.originMarket : null;
            
            log.debug("📤 Sending Kafka events for {} warehouses: stockId={}, originMarket={}", 
                    warehouses.size(), stock.getId(), originMarket);
            
            for (Warehouse warehouse : warehouses) {
                log.debug("📤 Sending stock change event for warehouse: productId={}, warehouseId={}, warehouseName={}, marketplace={}, oldQty={}, newQty={}", 
                        product.getId(), warehouse.getId(), warehouse.getName(), 
                        warehouse.getMarketplace() != null ? warehouse.getMarketplace().name() : "UNKNOWN",
                        oldQuantity, newQuantity);
                
                stockEventProducer.sendStockChangedForWarehouse(
                        product.getId(),
                        product.getArticle(),
                        warehouse.getId() != null ? warehouse.getId().toString() : null,
                        warehouse.getExternalWarehouseId(),
                        warehouse.getWarehouseType() != null ? warehouse.getWarehouseType().name() : null,
                        oldQuantity,
                        newQuantity,
                        finalChangeReason,
                        finalSourceSystem,
                        sourceId,
                        originMarket,
                        warehouse.getCompanyId().toString()
                );
                
                log.debug("✅ Stock change event sent for warehouse: productId={}, warehouseId={}, oldQty={}, newQty={}", 
                        product.getId(), warehouse.getId(), oldQuantity, newQuantity);
            }
            
            log.info("✅ ProductStock quantity change tracking completed: productId={}, stockId={}, warehouses={}, oldQty={}, newQty={}, eventsSent={}", 
                    product.getId(), stock.getId(), warehouses.size(), oldQuantity, newQuantity, warehouses.size());
                    
        } catch (Exception e) {
            log.error("❌ Error tracking product stock quantity change: stockId={}, productId={}, oldQty={}, newQty={}, error={}", 
                    stock.getId(), stock.getProduct() != null ? stock.getProduct().getId() : "UNKNOWN", 
                    oldQuantity, newQuantity, e.getMessage(), e);
        }
    }
}
