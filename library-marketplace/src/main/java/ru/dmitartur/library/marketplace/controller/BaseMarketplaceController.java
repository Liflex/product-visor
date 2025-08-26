package ru.dmitartur.library.marketplace.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
import ru.dmitartur.common.dto.marketplace.SyncStatusResponse;
import ru.dmitartur.library.marketplace.service.BaseMarketplaceService;
import ru.dmitartur.library.marketplace.integration.BaseMarketplaceApi;
import ru.dmitartur.library.marketplace.scheduled.BaseMarketplaceScheduler;

/**
 * Базовый контроллер для маркетплейсов
 * Содержит общие методы, которые могут быть переопределены в конкретных реализациях
 */
@RequiredArgsConstructor
public abstract class BaseMarketplaceController {
    
    protected final String marketplaceName;
    protected final String basePath;
    
    /**
     * Получить список заказов FBO
     */
    @PostMapping("/orders/fbo/list")
    public ResponseEntity<JsonNode> fboList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(getMarketplaceService().fboPostingList(req));
    }
    
    /**
     * Загрузить исторические данные заказов
     */
    @PostMapping("/orders/fbo/backfill")
    public ResponseEntity<Integer> backfill(@RequestBody DateRangeDto range,
                                          @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(getMarketplaceService().backfillAllOrders(range, pageSize));
    }
    
    /**
     * Получить список заказов FBS
     */
    @PostMapping("/orders/fbs/list")
    public ResponseEntity<JsonNode> fbsList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(getMarketplaceService().fbsPostingList(req));
    }
    
    /**
     * Получить информацию о заказе FBS
     */
    @PostMapping("/orders/fbs/get")
    public ResponseEntity<JsonNode> fbsGet(@RequestBody JsonNode req) {
        return ResponseEntity.ok(getMarketplaceService().fbsPostingGet(req));
    }
    
    /**
     * Получить список складов
     */
    @GetMapping("/warehouses")
    public ResponseEntity<JsonNode> warehouses() {
        return ResponseEntity.ok(getMarketplaceApi().listWarehouses());
    }
    

    
    /**
     * Получить статус синхронизации
     */
    @GetMapping("/sync/status")
    public ResponseEntity<SyncStatusResponse> getSyncStatus() {
        var checkpoint = getScheduler().getLastSyncInfo();
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
    
    /**
     * Принудительная синхронизация
     */
    @PostMapping("/sync/force")
    public ResponseEntity<SyncStatusResponse> forceSync() {
        try {
            getScheduler().forceSync();
            return ResponseEntity.ok(new SyncStatusResponse(
                "IN_PROGRESS",
                null,
                0,
                0L,
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(new SyncStatusResponse(
                "FAILED",
                null,
                0,
                0L,
                e.getMessage()
            ));
        }
    }
    
    /**
     * Получить сервис маркетплейса
     */
    protected abstract BaseMarketplaceService getMarketplaceService();
    
    /**
     * Получить API маркетплейса
     */
    protected abstract BaseMarketplaceApi getMarketplaceApi();
    
    /**
     * Получить планировщик синхронизации
     */
    protected abstract BaseMarketplaceScheduler getScheduler();
    
    /**
     * Получить название маркетплейса
     */
    public String getMarketplaceName() {
        return marketplaceName;
    }
    
    /**
     * Получить базовый путь API
     */
    public String getBasePath() {
        return basePath;
    }
}

