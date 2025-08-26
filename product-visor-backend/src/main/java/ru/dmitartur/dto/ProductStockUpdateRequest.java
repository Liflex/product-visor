package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.common.enums.ProductStockType;

@Data
public class ProductStockUpdateRequest {
    private ProductStockType stockType;
    private Integer quantity;
    private String notes;
}

