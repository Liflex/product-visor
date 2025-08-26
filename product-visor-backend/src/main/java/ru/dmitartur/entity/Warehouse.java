package ru.dmitartur.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import ru.dmitartur.common.enums.Marketplace;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "warehouses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Warehouse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "warehouse_type", nullable = false)
    private WarehouseType warehouseType; // FBS, FBO
    
    @Column(name = "external_warehouse_id")
    private String externalWarehouseId; // ID склада на маркетплейсе (обязательно для обновления стоков)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "marketplace")
    private Marketplace marketplace;
    
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_home_warehouse", nullable = false)
    private boolean isHomeWarehouse = false; // Домашний склад (FBS)
    
    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum WarehouseType {
        FBS, // Fulfillment by Seller - склад продавца
        FBO  // Fulfillment by Ozon - склад маркетплейса
    }
    
    // Marketplace enum moved to common-core
    
    /**
     * Проверить, является ли склад домашним FBS
     */
    public boolean isHomeFbsWarehouse() {
        return isHomeWarehouse && warehouseType == WarehouseType.FBS;
    }
    
    /**
     * Проверить, является ли склад FBO
     */
    public boolean isFboWarehouse() {
        return warehouseType == WarehouseType.FBO;
    }
}
