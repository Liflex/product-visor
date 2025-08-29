package ru.dmitartur.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.entity.Warehouse;

import java.util.Set;
import java.util.UUID;

/**
 * Событие изменения количества товара в ProductStock
 * Используется для асинхронного отслеживания изменений
 */
@Getter
public class ProductStockQuantityChangeEvent extends ApplicationEvent {
    
    private final ProductStock productStock;
    private final Integer oldQuantity;
    private final Integer newQuantity;
    private final String sourceSystem;
    private final String sourceId;

    public ProductStockQuantityChangeEvent(Object source, ProductStock productStock, 
                                         Integer oldQuantity, Integer newQuantity) {
        super(source);
        this.productStock = productStock;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.sourceSystem = null;
        this.sourceId = null;
    }
    
    public ProductStockQuantityChangeEvent(Object source, ProductStock productStock, 
                                         Integer oldQuantity, Integer newQuantity,
                                         String sourceSystem, String sourceId) {
        super(source);
        this.productStock = productStock;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.sourceSystem = sourceSystem;
        this.sourceId = sourceId;
    }
}
