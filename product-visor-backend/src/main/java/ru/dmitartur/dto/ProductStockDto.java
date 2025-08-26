package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.common.enums.ProductStockType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class ProductStockDto {
    private UUID id;
    private Long productId;
    private Set<WarehouseDto> warehouses; // Changed to Set<WarehouseDto>
    private UUID userId;
    private ProductStockType stockType;
    private Integer quantity;
    private LocalDateTime lastSyncAt;
    private String syncStatus;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
