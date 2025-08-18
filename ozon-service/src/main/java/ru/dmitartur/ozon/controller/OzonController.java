package ru.dmitartur.ozon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.ozon.dto.DateRangeDto;
import ru.dmitartur.ozon.service.OzonService;
import ru.dmitartur.ozon.scheduled.OzonBackfillScheduler;

@RestController
@RequestMapping("/api/ozon")
@RequiredArgsConstructor
public class OzonController {
    private final OzonService ozonService;
    private final OzonBackfillScheduler scheduler;

    @PostMapping("/orders/fbo/list")
    public ResponseEntity<JsonNode> fboList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(ozonService.fboPostingList(req));
    }

    @PostMapping("/orders/fbo/backfill")
    public ResponseEntity<Integer> backfill(@RequestBody DateRangeDto range,
                                            @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(ozonService.backfillAllOrders(range, pageSize));
    }

    @PostMapping("/orders/fbs/list")
    public ResponseEntity<JsonNode> fbsList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(ozonService.fbsPostingList(req));
    }

    @PostMapping("/orders/fbs/test")
    public ResponseEntity<JsonNode> fbsTest() {
        // Создаем тестовый запрос для FBS API согласно документации
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var req = mapper.createObjectNode();
        req.put("limit", 10);
        req.put("offset", 0);
        
        // Правильный формат согласно документации
        var filter = req.putObject("filter");
        filter.put("since", "2024-08-01T00:00:00Z");
        filter.put("to", "2025-08-15T23:59:59Z");
        
        return ResponseEntity.ok(ozonService.fbsPostingList(req));
    }

    @PostMapping("/orders/fbs/get")
    public ResponseEntity<JsonNode> fbsGet(@RequestBody JsonNode req) {
        return ResponseEntity.ok(ozonService.fbsPostingGet(req));
    }

    /**
     * Update OZON stock by article (offer_id). Optional warehouseId can be provided as request param.
     * Body example: { "quantity": 42 }
     */
    @PostMapping("/stock/{offerId}")
    public ResponseEntity<JsonNode> updateOzonStock(@PathVariable("offerId") String offerId,
                                                    @RequestBody JsonNode body,
                                                    @RequestParam(value = "warehouseId", required = false) String warehouseId) {
        int qty = body.path("quantity").asInt();
        JsonNode result = ozonService.updateStock(offerId, qty, warehouseId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sync/status")
    public ResponseEntity<SyncStatusResponse> getSyncStatus() {
        var checkpoint = scheduler.getLastSyncInfo();
        if (checkpoint.isPresent()) {
            var cp = checkpoint.get();
            return ResponseEntity.ok(new SyncStatusResponse(
                cp.getStatus(),
                cp.getLastSyncAt(),
                cp.getOrdersProcessed(),
                cp.getSyncDurationMs(),
                cp.getErrorMessage()
            ));
        } else {
            return ResponseEntity.ok(new SyncStatusResponse(
                "NEVER_SYNCED",
                null,
                0,
                0L,
                null
            ));
        }
    }

    @PostMapping("/sync/force")
    public ResponseEntity<SyncStatusResponse> forceSync() {
        try {
            scheduler.forceSync();
            return getSyncStatus();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new SyncStatusResponse(
                "FAILED",
                null,
                0,
                0L,
                e.getMessage()
            ));
        }
    }

    /**
     * DTO для ответа о статусе синхронизации
     */
    public static class SyncStatusResponse {
        private String status;
        private java.time.LocalDateTime lastSyncAt;
        private Integer ordersProcessed;
        private Long syncDurationMs;
        private String errorMessage;

        public SyncStatusResponse(String status, java.time.LocalDateTime lastSyncAt,
                                Integer ordersProcessed, Long syncDurationMs, String errorMessage) {
            this.status = status;
            this.lastSyncAt = lastSyncAt;
            this.ordersProcessed = ordersProcessed;
            this.syncDurationMs = syncDurationMs;
            this.errorMessage = errorMessage;
        }

        // Getters
        public String getStatus() { return status; }
        public java.time.LocalDateTime getLastSyncAt() { return lastSyncAt; }
        public Integer getOrdersProcessed() { return ordersProcessed; }
        public Long getSyncDurationMs() { return syncDurationMs; }
        public String getErrorMessage() { return errorMessage; }
    }
}


