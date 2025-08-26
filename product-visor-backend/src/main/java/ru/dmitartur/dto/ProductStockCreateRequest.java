package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.common.enums.ProductStockType;

import java.util.UUID;

@Data
public class ProductStockCreateRequest {
    private Long productId;
    private UUID warehouseId;
    private ProductStockType stockType;
    private Integer quantity;
    private String notes;
}

