package ru.dmitartur.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import ru.dmitartur.common.enums.ProductStockType;

@Entity
@Table(name = "product_stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProductStock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "product_stock_warehouses",
        joinColumns = @JoinColumn(name = "product_stock_id"),
        inverseJoinColumns = @JoinColumn(name = "warehouse_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_stock_id", "warehouse_id"})
    )
    @Builder.Default
    private Set<Warehouse> warehouses = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(name = "stock_type", nullable = false)
    private ProductStockType stockType = ProductStockType.FBS;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
    
    @Column(name = "sync_status")
    private String syncStatus = "NEVER_SYNCED";
    
    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version = 0L;
    
    /**
     * Обновить количество
     */
    public void updateQuantity(Integer newQuantity) {
        this.quantity = newQuantity;
    }
    
    /**
     * Добавить склад к остатку
     */
    public void addWarehouse(Warehouse warehouse) {
        if (warehouses == null) {
            warehouses = new HashSet<>();
        }
        warehouses.add(warehouse);
    }
    
    /**
     * Удалить склад из остатка
     */
    public void removeWarehouse(Warehouse warehouse) {
        if (warehouses != null) {
            warehouses.remove(warehouse);
        }
    }
    
    /**
     * Получить список ID складов
     */
    public Set<UUID> getWarehouseIds() {
        if (warehouses == null) {
            return new HashSet<>();
        }
        return warehouses.stream()
                .map(Warehouse::getId)
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Проверить, содержит ли остаток указанный склад
     */
    public boolean containsWarehouse(UUID warehouseId) {
        if (warehouses == null) {
            return false;
        }
        return warehouses.stream()
                .anyMatch(warehouse -> warehouse.getId().equals(warehouseId));
    }
}
