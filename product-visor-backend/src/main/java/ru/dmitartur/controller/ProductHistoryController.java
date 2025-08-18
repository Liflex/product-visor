package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.dto.ProductHistoryDto;
import ru.dmitartur.service.ProductHistoryService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller для работы с историей изменений продукта
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/product-history")
@RequiredArgsConstructor
public class ProductHistoryController {
    
    private final ProductHistoryService productHistoryService;
    
    /**
     * Получить историю изменений по ID продукта
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ProductHistoryDto>> getProductHistory(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Getting product history: productId={}, page={}, size={}", productId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductHistoryDto> history = productHistoryService.findByProductId(productId, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить историю изменений по ID продукта и полю
     */
    @GetMapping("/product/{productId}/field/{fieldName}")
    public ResponseEntity<List<ProductHistoryDto>> getProductFieldHistory(
            @PathVariable Long productId,
            @PathVariable String fieldName) {
        
        log.info("📋 Getting product field history: productId={}, field={}", productId, fieldName);
        
        List<ProductHistoryDto> history = productHistoryService.findByProductIdAndField(productId, fieldName);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить историю изменений по причине
     */
    @GetMapping("/reason/{changeReason}")
    public ResponseEntity<Page<ProductHistoryDto>> getHistoryByReason(
            @PathVariable String changeReason,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Getting history by reason: reason={}, page={}, size={}", changeReason, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductHistoryDto> history = productHistoryService.findByChangeReason(changeReason, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить историю изменений по системе-источнику
     */
    @GetMapping("/source/{sourceSystem}")
    public ResponseEntity<Page<ProductHistoryDto>> getHistoryBySourceSystem(
            @PathVariable String sourceSystem,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Getting history by source system: system={}, page={}, size={}", sourceSystem, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductHistoryDto> history = productHistoryService.findBySourceSystem(sourceSystem, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить историю изменений по ID источника
     */
    @GetMapping("/source-id/{sourceId}")
    public ResponseEntity<List<ProductHistoryDto>> getHistoryBySourceId(@PathVariable String sourceId) {
        log.info("📋 Getting history by source ID: {}", sourceId);
        
        List<ProductHistoryDto> history = productHistoryService.findBySourceId(sourceId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить историю изменений за период
     */
    @GetMapping("/date-range")
    public ResponseEntity<Page<ProductHistoryDto>> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Getting history by date range: from={}, to={}, page={}, size={}", fromDate, toDate, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductHistoryDto> history = productHistoryService.findByDateRange(fromDate, toDate, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить последнее изменение для продукта и поля
     */
    @GetMapping("/product/{productId}/field/{fieldName}/last")
    public ResponseEntity<ProductHistoryDto> getLastChangeByProductAndField(
            @PathVariable Long productId,
            @PathVariable String fieldName) {
        
        log.info("📋 Getting last change: productId={}, field={}", productId, fieldName);
        
        return productHistoryService.findLastChangeByProductAndField(productId, fieldName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Получить изменения по причине и периоду
     */
    @GetMapping("/reason/{changeReason}/date-range")
    public ResponseEntity<List<ProductHistoryDto>> getHistoryByReasonAndDateRange(
            @PathVariable String changeReason,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        
        log.info("📋 Getting history by reason and date range: reason={}, from={}, to={}", changeReason, fromDate, toDate);
        
        List<ProductHistoryDto> history = productHistoryService.findByChangeReasonAndDateRange(changeReason, fromDate, toDate);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Получить статистику истории изменений по продукту
     */
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<ProductHistoryService.ProductHistoryStats> getProductHistoryStats(@PathVariable Long productId) {
        log.info("📊 Getting product history stats: productId={}", productId);
        
        ProductHistoryService.ProductHistoryStats stats = productHistoryService.getProductHistoryStats(productId);
        return ResponseEntity.ok(stats);
    }
}
