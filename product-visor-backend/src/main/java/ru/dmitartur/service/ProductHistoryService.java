package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.dto.ProductHistoryDto;
import ru.dmitartur.entity.ProductHistory;
import ru.dmitartur.mapper.ProductHistoryMapper;
import ru.dmitartur.repository.ProductHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с историей изменений продукта
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductHistoryService {
    
    private final ProductHistoryRepository repository;
    private final ProductHistoryMapper mapper;
    
    /**
     * Сохранить запись об изменении
     */
    public ProductHistory saveHistory(Long productId, String fieldName, String oldValue, String newValue,
                                    String changeReason, String sourceSystem, String sourceId) {
        try {
            ProductHistory history = new ProductHistory(productId, fieldName, oldValue, newValue,
                    changeReason, sourceSystem, sourceId);
            
            ProductHistory saved = repository.save(history);
            log.info("📝 Saved product history: productId={}, field={}, reason={}, source={}", 
                    productId, fieldName, changeReason, sourceSystem);
            
            return saved;
        } catch (Exception e) {
            log.error("❌ Error saving product history: productId={}, field={}, error={}", 
                    productId, fieldName, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Сохранить запись об изменении с метаданными
     */
    public ProductHistory saveHistoryWithMetadata(Long productId, String fieldName, String oldValue, String newValue,
                                                String changeReason, String sourceSystem, String sourceId, String metadata) {
        try {
            ProductHistory history = new ProductHistory(productId, fieldName, oldValue, newValue,
                    changeReason, sourceSystem, sourceId);
            history.setMetadata(metadata);
            
            ProductHistory saved = repository.save(history);
            log.info("📝 Saved product history with metadata: productId={}, field={}, reason={}, source={}", 
                    productId, fieldName, changeReason, sourceSystem);
            
            return saved;
        } catch (Exception e) {
            log.error("❌ Error saving product history with metadata: productId={}, field={}, error={}", 
                    productId, fieldName, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Сохранить запись об изменении с метаданными и информацией о пользователе
     */
    public ProductHistory saveHistoryWithUserInfo(Long productId, String fieldName, String oldValue, String newValue,
                                                String changeReason, String sourceSystem, String sourceId, String metadata,
                                                UUID userId, UUID companyId) {
        try {
            ProductHistory history = new ProductHistory(productId, fieldName, oldValue, newValue,
                    changeReason, sourceSystem, sourceId, userId, companyId);
            history.setMetadata(metadata);
            
            ProductHistory saved = repository.save(history);
            log.info("📝 Saved product history with user info: productId={}, field={}, reason={}, source={}, userId={}, companyId={}", 
                    productId, fieldName, changeReason, sourceSystem, userId, companyId);
            
            return saved;
        } catch (Exception e) {
            log.error("❌ Error saving product history with user info: productId={}, field={}, error={}", 
                    productId, fieldName, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Найти историю изменений по ID продукта
     */
    public Page<ProductHistoryDto> findByProductId(Long productId, Pageable pageable) {
        log.debug("🔍 Finding product history by productId: {}", productId);
        Page<ProductHistory> page = repository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return page.map(mapper::toDto);
    }
    
    /**
     * Найти историю изменений по ID продукта и полю
     */
    public List<ProductHistoryDto> findByProductIdAndField(Long productId, String fieldName) {
        log.debug("🔍 Finding product history by productId: {} and field: {}", productId, fieldName);
        List<ProductHistory> history = repository.findByProductIdAndFieldNameOrderByCreatedAtDesc(productId, fieldName);
        return history.stream().map(mapper::toDto).toList();
    }
    
    /**
     * Найти историю изменений по причине
     */
    public Page<ProductHistoryDto> findByChangeReason(String changeReason, Pageable pageable) {
        log.debug("🔍 Finding product history by change reason: {}", changeReason);
        Page<ProductHistory> page = repository.findByChangeReasonOrderByCreatedAtDesc(changeReason, pageable);
        return page.map(mapper::toDto);
    }
    
    /**
     * Найти историю изменений по системе-источнику
     */
    public Page<ProductHistoryDto> findBySourceSystem(String sourceSystem, Pageable pageable) {
        log.debug("🔍 Finding product history by source system: {}", sourceSystem);
        Page<ProductHistory> page = repository.findBySourceSystemOrderByCreatedAtDesc(sourceSystem, pageable);
        return page.map(mapper::toDto);
    }
    
    /**
     * Найти историю изменений по ID источника
     */
    public List<ProductHistoryDto> findBySourceId(String sourceId) {
        log.debug("🔍 Finding product history by source ID: {}", sourceId);
        List<ProductHistory> history = repository.findBySourceIdOrderByCreatedAtDesc(sourceId);
        return history.stream().map(mapper::toDto).toList();
    }
    
    /**
     * Найти историю изменений за период
     */
    public Page<ProductHistoryDto> findByDateRange(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        log.debug("🔍 Finding product history by date range: {} to {}", fromDate, toDate);
        Page<ProductHistory> page = repository.findByDateRange(fromDate, toDate, pageable);
        return page.map(mapper::toDto);
    }
    
    /**
     * Найти последнее изменение для продукта и поля
     */
    public Optional<ProductHistoryDto> findLastChangeByProductAndField(Long productId, String fieldName) {
        log.debug("🔍 Finding last change for product: {} and field: {}", productId, fieldName);
        ProductHistory history = repository.findLastChangeByProductAndField(productId, fieldName);
        return Optional.ofNullable(history).map(mapper::toDto);
    }
    
    /**
     * Найти изменения по причине и периоду
     */
    public List<ProductHistoryDto> findByChangeReasonAndDateRange(String changeReason, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("🔍 Finding product history by reason: {} and date range: {} to {}", changeReason, fromDate, toDate);
        List<ProductHistory> history = repository.findByChangeReasonAndDateRange(changeReason, fromDate, toDate);
        return history.stream().map(mapper::toDto).toList();
    }
    
    /**
     * Получить статистику изменений по продукту
     */
    public ProductHistoryStats getProductHistoryStats(Long productId) {
        log.debug("📊 Getting product history stats for productId: {}", productId);
        
        List<ProductHistory> quantityChanges = repository.findByProductIdAndFieldNameOrderByCreatedAtDesc(productId, "quantity");
        
        int totalQuantityChanges = quantityChanges.size();
        int orderCreatedChanges = (int) quantityChanges.stream()
                .filter(h -> "ORDER_CREATED".equals(h.getChangeReason()))
                .count();
        int orderCancelledChanges = (int) quantityChanges.stream()
                .filter(h -> "ORDER_CANCELLED".equals(h.getChangeReason()))
                .count();
        
        return new ProductHistoryStats(productId, totalQuantityChanges, orderCreatedChanges, orderCancelledChanges);
    }
    
    /**
     * Статистика истории изменений продукта
     */
    public static class ProductHistoryStats {
        private final Long productId;
        private final int totalQuantityChanges;
        private final int orderCreatedChanges;
        private final int orderCancelledChanges;
        
        public ProductHistoryStats(Long productId, int totalQuantityChanges, int orderCreatedChanges, int orderCancelledChanges) {
            this.productId = productId;
            this.totalQuantityChanges = totalQuantityChanges;
            this.orderCreatedChanges = orderCreatedChanges;
            this.orderCancelledChanges = orderCancelledChanges;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public int getTotalQuantityChanges() { return totalQuantityChanges; }
        public int getOrderCreatedChanges() { return orderCreatedChanges; }
        public int getOrderCancelledChanges() { return orderCancelledChanges; }
    }
}
