package ru.dmitartur.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.event.ProductStockQuantityChangeEvent;

/**
 * Сервис для публикации событий, связанных с ProductStock
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Публикует событие изменения количества товара
     */
    public void publishQuantityChangeEvent(ProductStock productStock, Integer oldQuantity, 
                                         Integer newQuantity) {
        try {
            ProductStockQuantityChangeEvent event = new ProductStockQuantityChangeEvent(
                this, productStock, oldQuantity, newQuantity);
            
            eventPublisher.publishEvent(event);
            
            log.debug("📢 Published ProductStock quantity change event: productStockId={}, oldQuantity={}, newQuantity={}",
                    productStock.getId(), oldQuantity, newQuantity);
        } catch (Exception e) {
            log.error("❌ Error publishing ProductStock quantity change event: productStockId={}, error={}", 
                    productStock.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Публикует событие изменения количества товара с контекстом
     */
    public void publishQuantityChangeEventWithContext(ProductStock productStock, Integer oldQuantity, 
                                                    Integer newQuantity, String sourceSystem, String sourceId) {
        try {
            ProductStockQuantityChangeEvent event = new ProductStockQuantityChangeEvent(
                this, productStock, oldQuantity, newQuantity, sourceSystem, sourceId);
            
            eventPublisher.publishEvent(event);
            
            log.debug("📢 Published ProductStock quantity change event with context: productStockId={}, oldQuantity={}, newQuantity={}, sourceSystem={}, sourceId={}",
                    productStock.getId(), oldQuantity, newQuantity, sourceSystem, sourceId);
        } catch (Exception e) {
            log.error("❌ Error publishing ProductStock quantity change event with context: productStockId={}, error={}", 
                    productStock.getId(), e.getMessage(), e);
        }
    }
}
