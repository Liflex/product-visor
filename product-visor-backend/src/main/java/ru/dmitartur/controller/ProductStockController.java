package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.service.ProductStockService;
import ru.dmitartur.service.StockSyncService;
import ru.dmitartur.service.WarehouseService;
import ru.dmitartur.service.ProductService;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.dto.ProductStockDto;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.Warehouse;
import ru.dmitartur.mapper.ProductStockMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/product-stocks")
@RequiredArgsConstructor
public class ProductStockController {
    
    private final ProductStockService productStockService;
    private final StockSyncService stockSyncService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final ProductStockMapper productStockMapper;
    
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
        
        // Проверяем права доступа
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        // Проверяем, что товар принадлежит пользователю
        Product product = productService.validateProductOwnership(request.getProductId());
        
        // Проверяем, что все склады принадлежат пользователю
        warehouseService.validateWarehousesOwnership(request.getWarehouses());
        
        // Конвертируем DTO в Entity
        ProductStock productStock = productStockMapper.toEntity(request);
        productStock.setProduct(product);
        productStock.setUserId(userId);
        productStock.setSyncStatus("NEVER_SYNCED");
        
        // Добавляем склады
        if (request.getWarehouses() != null) {
            for (var warehouseDto : request.getWarehouses()) {
                Warehouse warehouse = warehouseService.getWarehouse(warehouseDto.getId());
                productStock.addWarehouse(warehouse);
            }
        }
        
        // Сохраняем
        ProductStock saved = productStockService.createProductStock(productStock);
        
        return ResponseEntity.ok(productStockMapper.toDto(saved));
    }
    
    /**
     * Обновить остаток товара
     */
    @PutMapping("/{productStockId}")
    public ResponseEntity<ProductStockDto> updateProductStock(
            @PathVariable UUID productStockId,
            @RequestBody ProductStockDto request) {
        log.info("📦 Updating stock: {} for product: {}", productStockId, request.getProductId());
        
        // Проверяем права доступа
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        // Получаем существующий остаток
        ProductStock existingStock = productStockService.getProductStockEntityById(productStockId);
        
        if (!existingStock.getUserId().equals(userId)) {
            throw new RuntimeException("Остаток товара не принадлежит пользователю");
        }
        
        // Проверяем, что все склады принадлежат пользователю
        warehouseService.validateWarehousesOwnership(request.getWarehouses());
        
        // Создаем новый ProductStock с обновленными данными
        ProductStock updatedStock = productStockMapper.toEntity(request);
        updatedStock.setId(existingStock.getId());
        updatedStock.setUserId(existingStock.getUserId());
        updatedStock.setProduct(existingStock.getProduct());
        updatedStock.setLastSyncAt(java.time.LocalDateTime.now());
        updatedStock.setSyncStatus("UPDATED");
        
        // Добавляем склады
        if (request.getWarehouses() != null) {
            for (var warehouseDto : request.getWarehouses()) {
                Warehouse warehouse = warehouseService.getWarehouse(warehouseDto.getId());
                updatedStock.addWarehouse(warehouse);
            }
        }
        
        // Сохраняем через сервис (который использует маппер для обновления)
        // Передаем контекст для правильного определения причины изменения
        ProductStock saved = productStockService.updateProductStockWithContext(updatedStock, "REST_API", "ProductStockController");
        
        return ResponseEntity.ok(productStockMapper.toDto(saved));
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
