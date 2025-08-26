package ru.dmitartur.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.order.dto.OrderDto;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.service.OrderService;
import ru.dmitartur.order.service.OrderContextEnricher;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderContextEnricher orderContextEnricher;

    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto dto) {
        // Обогащаем контекстом (пользователь/компания)
        var entity = orderService.getOrderMapper().toEntity(dto);
        orderContextEnricher.enrich(entity);
        var saved = orderService.save(entity);
        return ResponseEntity.ok(orderService.getOrderMapper().toDto(saved));
    }

    @PutMapping
    public ResponseEntity<OrderDto> update(@RequestBody OrderDto dto) {
        var entity = orderService.getOrderMapper().toEntity(dto);
        orderContextEnricher.enrich(entity);
        var saved = orderService.update(entity);
        return ResponseEntity.ok(orderService.getOrderMapper().toDto(saved));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        log.info("📋 Fetching orders with filters: page={}, size={}, status={}, dateFrom={}, dateTo={}", 
                page, size, status, dateFrom, dateTo);
        Page<OrderDto> orders = orderService.findWithFiltersDto(PageRequest.of(page, size), status, dateFrom, dateTo);
        log.info("✅ Orders fetched successfully: totalElements={}, totalPages={}", 
                orders.getTotalElements(), orders.getTotalPages());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/market/{market}")
    public ResponseEntity<Page<OrderDto>> listByMarket(@PathVariable String market,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String dateFrom,
                                                      @RequestParam(required = false) String dateTo) {
        log.info("📋 Fetching orders by market with filters: market={}, page={}, size={}, status={}, dateFrom={}, dateTo={}", 
                market, page, size, status, dateFrom, dateTo);
        try {
            ru.dmitartur.common.enums.Market marketEnum = ru.dmitartur.common.enums.Market.valueOf(market);
            Page<OrderDto> orders = orderService.findByMarketWithFiltersDto(marketEnum, PageRequest.of(page, size), status, dateFrom, dateTo);
            log.info("✅ Orders by market fetched successfully: market={}, totalElements={}, totalPages={}", 
                    market, orders.getTotalElements(), orders.getTotalPages());
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            log.warn("❌ Invalid market parameter: {}", market);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{postingNumber}")
    public ResponseEntity<OrderDto> get(@PathVariable String postingNumber) {
        log.info("🔍 Fetching order by posting number: {}", postingNumber);
        return orderService.findByPostingNumberDto(postingNumber)
                .map(order -> {
                    log.info("✅ Order found: postingNumber={}, id={}", postingNumber, order.getId());
                    return ResponseEntity.ok(order);
                })
                .orElseGet(() -> {
                    log.warn("❌ Order not found: postingNumber={}", postingNumber);
                    return ResponseEntity.notFound().build();
                });
    }
}






