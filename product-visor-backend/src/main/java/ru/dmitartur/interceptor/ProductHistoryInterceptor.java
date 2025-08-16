package ru.dmitartur.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.entity.Product;
import ru.dmitartur.service.ProductHistoryService;

/**
 * Interceptor для отслеживания изменений Product
 * Более простой подход без AOP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHistoryInterceptor {
    
    private final ProductHistoryService productHistoryService;
    
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
            }
        }
        
        return "unknown";
    }
}
