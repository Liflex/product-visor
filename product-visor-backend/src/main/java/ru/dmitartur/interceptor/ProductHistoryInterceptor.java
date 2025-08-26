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

import java.util.Set;
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
     * Отследить изменение количества товара
     */
    public void trackQuantityChange(Product product, int oldQuantity, int newQuantity, 
                                   String changeReason, String sourceSystem, String sourceId) {
        try {
            productHistoryService.saveHistory(
                product.getId(),
                "quantity",
                String.valueOf(oldQuantity),
                String.valueOf(newQuantity),
                changeReason,
                sourceSystem,
                sourceId
            );
            
            log.debug("📝 Tracked quantity change: productId={}, oldQuantity={}, newQuantity={}, reason={}, source={}", 
                    product.getId(), oldQuantity, newQuantity, changeReason, sourceSystem);
            ChangeContextHolder.ChangeContext ctx = ChangeContextHolder.get();
            String originMarket = (ctx != null) ? ctx.originMarket : null;
            stockEventProducer.sendStockChanged(
                product.getId(),
                product.getArticle(),
                oldQuantity,
                newQuantity,
                changeReason,
                sourceSystem,
                sourceId,
                originMarket
            );
                    
        } catch (Exception e) {
            log.error("❌ Error tracking quantity change: productId={}, error={}", 
                    product.getId(), e.getMessage());
        }
    }
    
    /**
     * Отследить изменение количества товара с автоматическим определением контекста
     */
    public void trackQuantityChange(Product product, int oldQuantity, int newQuantity) {
        String changeReason = determineChangeReason();
        String sourceSystem = determineSourceSystem();
        String sourceId = determineSourceId();
        
        trackQuantityChange(product, oldQuantity, newQuantity, changeReason, sourceSystem, sourceId);
    }

    /**
     * Отследить изменение количества в ProductStock (с указанием склада)
     * @deprecated Use trackProductStockQuantityChange(ProductStock stock, int oldQuantity, int newQuantity) instead
     */
    @Deprecated
    public void trackProductStockQuantityChange(Product product, Warehouse warehouse, int oldQuantity, int newQuantity) {
        String changeReason = determineChangeReason();
        String sourceSystem = determineSourceSystem();
        String sourceId = determineSourceId();

        try {
            String metadata = String.format("{\"warehouseId\":\"%s\",\"externalWarehouseId\":\"%s\",\"warehouseType\":\"%s\"}",
                    warehouse != null && warehouse.getId() != null ? warehouse.getId().toString() : "",
                    warehouse != null ? String.valueOf(warehouse.getExternalWarehouseId()) : "",
                    warehouse != null && warehouse.getWarehouseType() != null ? warehouse.getWarehouseType().name() : "");

            productHistoryService.saveHistoryWithMetadata(
                    product.getId(),
                    "product_stock.quantity",
                    String.valueOf(oldQuantity),
                    String.valueOf(newQuantity),
                    changeReason,
                    sourceSystem,
                    sourceId,
                    metadata
            );

            ChangeContextHolder.ChangeContext ctx = ChangeContextHolder.get();
            String originMarket = (ctx != null) ? ctx.originMarket : null;
            stockEventProducer.sendStockChangedForWarehouse(
                    product.getId(),
                    product.getArticle(),
                    warehouse != null && warehouse.getId() != null ? warehouse.getId().toString() : null,
                    warehouse != null ? warehouse.getExternalWarehouseId() : null,
                    warehouse != null && warehouse.getWarehouseType() != null ? warehouse.getWarehouseType().name() : null,
                    oldQuantity,
                    newQuantity,
                    changeReason,
                    sourceSystem,
                    sourceId,
                    originMarket
            );
        } catch (Exception e) {
            log.error("❌ Error tracking product stock quantity change: productId={}, error={}", product.getId(), e.getMessage());
        }
    }

    /**
     * Отследить изменение количества в ProductStock (новый метод для ManyToMany)
     */
    public void trackProductStockQuantityChange(ProductStock stock, int oldQuantity, int newQuantity,
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

            log.debug("💾 Saving history with metadata: productId={}, metadata={}", product.getId(), metadata);

            productHistoryService.saveHistoryWithMetadata(
                    product.getId(),
                    "product_stock.quantity",
                    String.valueOf(oldQuantity),
                    String.valueOf(newQuantity),
                    changeReason,
                    sourceSystem,
                    sourceId,
                    metadata
            );

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
                        changeReason,
                        sourceSystem,
                        sourceId,
                        originMarket
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

    /**
     * Отследить изменение количества в ProductStock с автоматическим определением контекста
     */
    public void trackProductStockQuantityChange(ProductStock stock, int oldQuantity, int newQuantity) {
        String changeReason = determineChangeReason();
        String sourceSystem = determineSourceSystem();
        String sourceId = determineSourceId();
        
        trackProductStockQuantityChange(stock, oldQuantity, newQuantity, changeReason, sourceSystem, sourceId);
    }
    
    /**
     * Определить причину изменения по контексту выполнения
     */
    private String determineChangeReason() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();
            
            // Определяем причину по контексту
            if (className.contains("OrderEventConsumer")) {
                if (methodName.contains("handleOrderCreated")) {
                    return "ORDER_CREATED";
                } else if (methodName.contains("handleOrderCancelled")) {
                    return "ORDER_CANCELLED";
                }
            } else if (className.contains("ProductController")) {
                return "MANUAL_UPDATE";
            } else if (className.contains("ProductStockService")) {
                return "STOCK_UPDATE";
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Определить систему-источник по контексту выполнения
     */
    private String determineSourceSystem() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            if (className.contains("OrderEventConsumer")) {
                return "KAFKA";
            } else if (className.contains("ProductController")) {
                return "REST_API";
            } else if (className.contains("ProductStockService")) {
                return "STOCK_SERVICE";
            }
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Определить ID источника по контексту выполнения
     */
    private String determineSourceId() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            
            if (className.contains("OrderEventConsumer")) {
                return "kafka_event";
            } else if (className.contains("ProductController")) {
                return "rest_api";
            } else if (className.contains("ProductStockService")) {
                return "stock_service";
            }
        }
        
        return "unknown";
    }
}
