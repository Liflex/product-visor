package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.common.enums.ProductStockType;

import java.util.List;

@Data
public class BulkStockSyncRequest {
    private List<Long> productIds;
    private ProductStockType stockType;
}

