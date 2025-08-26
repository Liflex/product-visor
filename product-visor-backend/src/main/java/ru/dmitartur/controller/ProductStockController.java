package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.service.ProductStockService;
import ru.dmitartur.service.StockSyncService;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.dto.ProductStockDto;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/product-stocks")
@RequiredArgsConstructor
public class ProductStockController {
    
    private final ProductStockService productStockService;
    private final StockSyncService stockSyncService;
    
    /**
     * Получить остатки товара по всем складам пользователя
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductStockDto>> getProductStocks(@PathVariable Long productId) {
        log.info("📦 Getting stocks for product: {}", productId);
        List<ProductStockDto> stockDtos = productStockService.getProductStocksWithProduct(productId);
        return ResponseEntity.ok(stockDtos);
    }
    
    /**
     * Получить остатки товара на конкретном складе пользователя
     */
    @GetMapping("/product/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<ProductStockDto> getProductStock(@PathVariable Long productId, @PathVariable UUID warehouseId) {
        log.info("📦 Getting stock for product: {} on warehouse: {}", productId, warehouseId);
        ProductStockDto stock = productStockService.getProductStock(productId, warehouseId);
        return ResponseEntity.ok(stock);
    }
    
    /**
     * Получить остаток товара по ID
     */
    @GetMapping("/{productStockId}")
    public ResponseEntity<ProductStockDto> getProductStockById(@PathVariable UUID productStockId) {
        log.info("📦 Getting stock by ID: {}", productStockId);
        ProductStockDto stock = productStockService.getProductStockById(productStockId);
        return ResponseEntity.ok(stock);
    }
    
    /**
     * Создать новый остаток товара
     */
    @PostMapping
    public ResponseEntity<ProductStockDto> createProductStock(@RequestBody ProductStockDto request) {
        log.info("📦 Creating stock for product: {} with {} warehouses", request.getProductId(), 
                request.getWarehouses() != null ? request.getWarehouses().size() : 0);
        ProductStockDto stock = productStockService.createProductStock(request);
        return ResponseEntity.ok(stock);
    }
    
    /**
     * Обновить остаток товара
     */
    @PutMapping("/{productStockId}")
    public ResponseEntity<ProductStockDto> updateProductStock(
            @PathVariable UUID productStockId,
            @RequestBody ProductStockDto request) {
        log.info("📦 Updating stock: {} for product: {}", productStockId, request.getProductId());
        ProductStockDto stock = productStockService.updateProductStock(productStockId, request);
        return ResponseEntity.ok(stock);
    }
    
    /**
     * Удалить остаток товара
     */
    @DeleteMapping("/{productStockId}")
    public ResponseEntity<Void> deleteProductStock(@PathVariable UUID productStockId) {
        log.info("📦 Deleting stock: {}", productStockId);
        productStockService.deleteProductStock(productStockId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Синхронизировать остатки по ProductStock ID
     */
    @PostMapping("/{productStockId}/sync")
    public ResponseEntity<Void> syncStockById(@PathVariable UUID productStockId) {
        log.info("🔄 Syncing stock by ID: {}", productStockId);
        // TODO: Implement sync by ID method
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
