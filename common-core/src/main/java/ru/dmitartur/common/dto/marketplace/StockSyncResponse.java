package ru.dmitartur.common.dto.marketplace;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSyncResponse {
    
    private String marketplace;
    private String status; // "SUCCESS", "PARTIAL_SUCCESS", "FAILED"
    private LocalDateTime processedAt;
    private int totalItems;
    private int successCount;
    private int failedCount;
    private List<StockSyncResult> results;
    private String errorMessage;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockSyncResult {
        private String offerId;
        private String sku;
        private String status; // "SUCCESS", "FAILED"
        private String errorMessage;
        private Integer oldQuantity;
        private Integer newQuantity;
    }
}

