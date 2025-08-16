package ru.dmitartur.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.order.entity.OrderItem;
import ru.dmitartur.order.repository.OrderItemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для анализа заказов
 */
@Slf4j
@RestController
@RequestMapping("/api/orders/analysis")
@RequiredArgsConstructor
public class OrderAnalysisController {

    private final OrderItemRepository orderItemRepository;

    /**
     * Получить статистику по ненайденным продуктам
     */
    @GetMapping("/missing-products")
    public ResponseEntity<Map<String, Object>> getMissingProductsStats() {
        log.info("📊 Analyzing missing products statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Общее количество товаров в заказах
        long totalItems = orderItemRepository.count();
        stats.put("totalItems", totalItems);
        
        // Количество товаров с ненайденными продуктами
        long missingProducts = orderItemRepository.countByProductIdIsNull();
        stats.put("missingProducts", missingProducts);
        
        // Количество товаров с найденными продуктами
        long foundProducts = totalItems - missingProducts;
        stats.put("foundProducts", foundProducts);
        
        // Процент ненайденных продуктов
        double missingPercentage = totalItems > 0 ? (double) missingProducts / totalItems * 100 : 0;
        stats.put("missingPercentage", Math.round(missingPercentage * 100.0) / 100.0);
        
        log.info("📊 Missing products analysis: total={}, missing={} ({}%), found={}", 
                totalItems, missingProducts, missingPercentage, foundProducts);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Получить список товаров с ненайденными продуктами
     */
    @GetMapping("/missing-products/list")
    public ResponseEntity<Map<String, Object>> getMissingProductsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Fetching missing products list: page={}, size={}", page, size);
        
        Page<OrderItem> missingProductsPage = orderItemRepository.findByProductIdIsNull(
                PageRequest.of(page, size));
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", missingProductsPage.getContent());
        response.put("totalElements", missingProductsPage.getTotalElements());
        response.put("totalPages", missingProductsPage.getTotalPages());
        response.put("currentPage", missingProductsPage.getNumber());
        response.put("size", missingProductsPage.getSize());
        
        log.info("📋 Missing products list loaded: {} items", missingProductsPage.getContent().size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получить уникальные SKU ненайденных продуктов
     */
    @GetMapping("/missing-products/skus")
    public ResponseEntity<Map<String, Object>> getMissingProductSkus() {
        log.info("🔍 Fetching unique missing product SKUs");
        
        List<String> missingSkus = orderItemRepository.findDistinctSkuByProductIdIsNull();
        
        Map<String, Object> response = new HashMap<>();
        response.put("missingSkus", missingSkus);
        response.put("count", missingSkus.size());
        
        log.info("🔍 Found {} unique missing SKUs", missingSkus.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Получить статистику по SKU
     */
    @GetMapping("/sku-stats")
    public ResponseEntity<Map<String, Object>> getSkuStats() {
        log.info("📊 Analyzing SKU statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Общее количество уникальных SKU
        long totalUniqueSkus = orderItemRepository.countDistinctSku();
        stats.put("totalUniqueSkus", totalUniqueSkus);
        
        // Количество уникальных SKU с ненайденными продуктами
        long missingUniqueSkus = orderItemRepository.countDistinctSkuByProductIdIsNull();
        stats.put("missingUniqueSkus", missingUniqueSkus);
        
        // Количество уникальных SKU с найденными продуктами
        long foundUniqueSkus = totalUniqueSkus - missingUniqueSkus;
        stats.put("foundUniqueSkus", foundUniqueSkus);
        
        // Процент ненайденных уникальных SKU
        double missingSkuPercentage = totalUniqueSkus > 0 ? 
                (double) missingUniqueSkus / totalUniqueSkus * 100 : 0;
        stats.put("missingSkuPercentage", Math.round(missingSkuPercentage * 100.0) / 100.0);
        
        log.info("📊 SKU statistics: total={}, missing={} ({}%), found={}", 
                totalUniqueSkus, missingUniqueSkus, missingSkuPercentage, foundUniqueSkus);
        
        return ResponseEntity.ok(stats);
    }
}
