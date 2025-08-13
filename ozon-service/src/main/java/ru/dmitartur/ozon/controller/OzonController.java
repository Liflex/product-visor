package ru.dmitartur.ozon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import ru.dmitartur.ozon.service.OzonService;
import ru.dmitartur.ozon.service.OzonOrderSyncService;
import ru.dmitartur.ozon.dto.DateRangeDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ozon")
@RequiredArgsConstructor
public class OzonController {
    private final OzonService ozonService;
    private final OzonOrderSyncService orderSyncService;

    @PostMapping("/orders/fbo/list")
    public ResponseEntity<JsonNode> fboList(@RequestBody JsonNode req) {
        return ResponseEntity.ok(ozonService.fboPostingList(req));
    }

    @PostMapping("/orders/fbo/backfill")
    public ResponseEntity<Integer> backfill(@RequestBody DateRangeDto range,
                                            @RequestParam(defaultValue = "100") int pageSize) {
        return ResponseEntity.ok(orderSyncService.backfillFboOrders(range, pageSize));
    }
}


