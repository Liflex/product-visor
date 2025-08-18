package ru.dmitartur.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * История изменений продукта
 */
@Entity
@Table(name = "product_history", schema = "visor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "field_name", nullable = false)
    private String fieldName; // quantity, name, price, etc.
    
    @Column(name = "old_value")
    private String oldValue;
    
    @Column(name = "new_value")
    private String newValue;
    
    @Column(name = "change_reason", nullable = false)
    private String changeReason; // ORDER_CREATED, ORDER_CANCELLED, MANUAL_UPDATE, etc.
    
    @Column(name = "source_system")
    private String sourceSystem; // KAFKA, REST_API, MANUAL, etc.
    
    @Column(name = "source_id")
    private String sourceId; // posting_number, user_id, etc.
    
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Column(name = "metadata")
    private String metadata; // JSON для дополнительных данных
    
    public ProductHistory(Long productId, String fieldName, String oldValue, String newValue, 
                         String changeReason, String sourceSystem, String sourceId) {
        this.productId = productId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeReason = changeReason;
        this.sourceSystem = sourceSystem;
        this.sourceId = sourceId;
        this.createdAt = LocalDateTime.now();
    }
}
