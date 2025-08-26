package ru.dmitartur.yandex.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.library.marketplace.controller.BaseMarketplaceController;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
import ru.dmitartur.common.dto.marketplace.SyncStatusResponse;
import ru.dmitartur.library.marketplace.service.BaseMarketplaceService;
import ru.dmitartur.library.marketplace.integration.BaseMarketplaceApi;
import ru.dmitartur.library.marketplace.scheduled.BaseMarketplaceScheduler;
import ru.dmitartur.yandex.service.YandexService;
import ru.dmitartur.yandex.integration.YandexApi;
import ru.dmitartur.yandex.scheduled.YandexScheduler;

import java.util.Optional;

@RestController
@RequestMapping("/api/yandex")
public class YandexController extends BaseMarketplaceController {
    
    private final YandexService yandexService;
    private final YandexScheduler scheduler;
    private final YandexApi yandexApi;
    
    public YandexController(YandexService yandexService, YandexScheduler scheduler, YandexApi yandexApi) {
        super("Yandex", "/api/yandex");
        this.yandexService = yandexService;
        this.scheduler = scheduler;
        this.yandexApi = yandexApi;
    }

    @PostMapping("/orders/fbo/list")
    public ResponseEntity<JsonNode> fboList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(yandexService.fboPostingList(req));
    }

    @PostMapping("/orders/fbo/backfill")
    public ResponseEntity<Integer> backfill(@RequestBody DateRangeDto range,
                                           @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(yandexService.backfillAllOrders(range, pageSize));
    }

    @PostMapping("/orders/fbs/list")
    public ResponseEntity<JsonNode> fbsList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(yandexService.fbsPostingList(req));
    }

    @PostMapping("/orders/fbs/test")
    public ResponseEntity<JsonNode> fbsTest() {
        // Создаем тестовый запрос для Yandex API
        var mapper = new ObjectMapper();
        var req = mapper.createObjectNode();
        req.put("pageSize", 1);
        req.put("pageToken", "");
        
        var filter = req.putObject("filter");
        filter.put("dateFrom", "2024-08-01T00:00:00Z");
        filter.put("dateTo", "2024-08-02T23:59:59Z");
        
        return ResponseEntity.ok(yandexService.fbsPostingList(req));
    }

    @PostMapping("/orders/force-sync-all-companies")
    public ResponseEntity<String> forceSyncAllCompanies() {
        scheduler.forceSyncAllCompanies();
        return ResponseEntity.ok("Force sync for all companies started");
    }

    @PostMapping("/orders/fbs/get")
    public ResponseEntity<JsonNode> fbsGet(@RequestBody JsonNode req) {
        return ResponseEntity.ok(yandexService.fbsPostingGet(req));
    }

    @PostMapping("/stocks/update")
    public ResponseEntity<JsonNode> updateStock(@RequestParam String offerId,
                                               @RequestParam int newQuantity,
                                               @RequestParam(required = false) String warehouseId) {
        return ResponseEntity.ok(yandexService.updateStock(offerId, newQuantity, warehouseId));
    }

    @GetMapping("/orders/sync-status")
    public ResponseEntity<SyncStatusResponse> getSyncStatus() {
        return ResponseEntity.ok(scheduler.getLastSyncInfo()
                .map(checkpoint -> new SyncStatusResponse(
                        checkpoint.getStatus(),
                        checkpoint.getLastSyncAt(),
                        checkpoint.getOrdersProcessed(),
                        checkpoint.getSyncDurationMs(),
                        checkpoint.getErrorMessage()))
                .orElse(new SyncStatusResponse(
                        "NO_SYNC",
                        null,
                        0,
                        0L,
                        null)));
    }

    @Override
    protected BaseMarketplaceService getMarketplaceService() {
        return yandexService;
    }
    
    @Override
    protected BaseMarketplaceApi getMarketplaceApi() {
        return yandexApi;
    }
    
    @Override
    protected BaseMarketplaceScheduler getScheduler() {
        return scheduler;
    }
}
