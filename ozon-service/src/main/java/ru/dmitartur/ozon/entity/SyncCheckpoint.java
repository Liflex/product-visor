package ru.dmitartur.ozon.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Точка синхронизации с Ozon API
 * Хранит информацию о последней успешной синхронизации
 */
@Entity
@Table(name = "sync_checkpoints", schema = "ozon")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncCheckpoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "checkpoint_name", unique = true, nullable = false)
    private String checkpointName; // FBO_ORDERS, PRODUCTS, etc.
    
    @Column(name = "last_sync_at", nullable = false)
    private OffsetDateTime lastSyncAt;
    
    @Column(name = "last_order_id")
    private Long lastOrderId; // ID последнего обработанного заказа
    
    @Column(name = "last_posting_number")
    private String lastPostingNumber; // Номер последнего постинга
    
    @Column(name = "orders_processed")
    private Integer ordersProcessed; // Количество обработанных заказов в последней синхронизации
    
    @Column(name = "sync_duration_ms")
    private Long syncDurationMs; // Длительность синхронизации в миллисекундах
    
    @Column(name = "status")
    private String status; // SUCCESS, FAILED, IN_PROGRESS
    
    @Column(name = "error_message")
    private String errorMessage; // Сообщение об ошибке, если есть
    
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
    
    public SyncCheckpoint(String checkpointName, OffsetDateTime lastSyncAt) {
        this.checkpointName = checkpointName;
        this.lastSyncAt = lastSyncAt;
        this.status = "SUCCESS";
        this.ordersProcessed = 0;
    }
}
