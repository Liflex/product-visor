package ru.dmitartur.common.dto.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Запрос на синхронизацию остатков на маркетплейсах
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSyncRequest {
    private String marketplace;
    private UUID companyId;
    private List<StockItem> items;
    private List<WarehouseSyncInfo> warehouses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockItem {
        private String offerId;
        private Integer quantity;
        private String productId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarehouseSyncInfo {
        private String warehouseId; // внешний id склада на маркетплейсе
        private String warehouseType; // тип склада (FBS/FBO/...)
        private String warehouseName;
        private boolean isHomeWarehouse;
    }
}


