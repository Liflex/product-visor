package ru.dmitartur.common.dto.marketplace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusResponse {
    private String status;
    private LocalDateTime lastSyncTime;
    private int processedOrders;
    private long syncDurationMs;
    private String errorMessage;
}

