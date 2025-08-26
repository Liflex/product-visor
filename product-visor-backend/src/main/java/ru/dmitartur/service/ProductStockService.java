package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.Warehouse;
import ru.dmitartur.common.enums.ProductStockType;
import ru.dmitartur.repository.ProductStockRepository;
import ru.dmitartur.repository.ProductRepository;
import ru.dmitartur.repository.WarehouseRepository;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.interceptor.ProductHistoryInterceptor;
import ru.dmitartur.dto.ProductStockDto;
import ru.dmitartur.dto.WarehouseDto;
import ru.dmitartur.mapper.ProductStockMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockService {
    
    private final ProductStockRepository productStockRepository;
    private final ProductHistoryInterceptor productHistoryInterceptor;
    private final ProductStockMapper productStockMapper;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    
    /**
     * Получить остатки товара по всем складам пользователя
     */
    public List<ProductStockDto> getProductStocks(Long productId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        List<ProductStock> stocks = productStockRepository.findByProductIdAndUserIdWithWarehouses(productId, userId);
        return stocks.stream()
                .map(productStockMapper::toDto)
                .toList();
    }
    
    /**
     * Получить остатки товара с загрузкой продукта
     */
    public List<ProductStockDto> getProductStocksWithProduct(Long productId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        List<ProductStock> stocks = productStockRepository.findByProductIdWithProduct(productId, userId);
        return stocks.stream()
                .map(productStockMapper::toDto)
                .toList();
    }
    
    /**
     * Получить остатки товара на конкретном складе
     */
    public ProductStockDto getProductStock(Long productId, UUID warehouseId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        ProductStock stock = productStockRepository.findByProductIdAndWarehouseIdAndUserId(productId, warehouseId, userId)
                .orElseThrow(() -> new RuntimeException("Остатки товара не найдены"));
        
        return productStockMapper.toDto(stock);
    }

    /**
     * Получить ProductStock по ID с проверкой прав доступа
     */
    public ProductStockDto getProductStockById(UUID productStockId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        ProductStock stock = productStockRepository.findById(productStockId)
                .orElseThrow(() -> new RuntimeException("Остатки товара не найдены"));
        
        if (!stock.getUserId().equals(userId)) {
            throw new RuntimeException("Остатки товара не принадлежат пользователю");
        }
        
        return productStockMapper.toDto(stock);
    }
    
    /**
     * Создать новый остаток товара
     */
    public ProductStockDto createProductStock(ProductStockDto productStockDto) {
        // Проверяем, что товар принадлежит пользователю через ProductService
        Product product = productService.validateProductOwnership(productStockDto.getProductId());
        
        // Проверяем, что все склады принадлежат пользователю через WarehouseService
        warehouseService.validateWarehousesOwnership(productStockDto.getWarehouses());
        
        // Создаем новый остаток
        ProductStock stock = new ProductStock();
        stock.setProduct(product);
        stock.setUserId(product.getOwnerUserId());
        stock.setStockType(productStockDto.getStockType());
        stock.setQuantity(productStockDto.getQuantity());
        stock.setNotes(productStockDto.getNotes());
        stock.setSyncStatus("NEVER_SYNCED");
        
        // Добавляем склады
        if (productStockDto.getWarehouses() != null) {
            for (WarehouseDto warehouseDto : productStockDto.getWarehouses()) {
                Warehouse warehouse = warehouseService.getWarehouse(warehouseDto.getId());
                stock.addWarehouse(warehouse);
            }
        }
        
        ProductStock saved = productStockRepository.save(stock);
        
        return productStockMapper.toDto(saved);
    }
    
    /**
     * Обновить остаток товара
     */
    public ProductStockDto updateProductStock(UUID productStockId, ProductStockDto productStockDto) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        // Получаем существующий остаток
        ProductStock existingStock = productStockRepository.findById(productStockId)
                .orElseThrow(() -> new RuntimeException("Остаток товара не найден"));
        
        if (!existingStock.getUserId().equals(userId)) {
            throw new RuntimeException("Остаток товара не принадлежит пользователю");
        }
        
        // Сохраняем старое количество для отслеживания изменений
        int oldQuantity = existingStock.getQuantity() != null ? existingStock.getQuantity() : 0;
        
        // Обновляем основные поля
        existingStock.setStockType(productStockDto.getStockType());
        existingStock.setQuantity(productStockDto.getQuantity());
        existingStock.setNotes(productStockDto.getNotes());
        
        // Очищаем и обновляем склады
        existingStock.getWarehouses().clear();
        if (productStockDto.getWarehouses() != null) {
            for (WarehouseDto warehouseDto : productStockDto.getWarehouses()) {
                Warehouse warehouse = warehouseService.getWarehouse(warehouseDto.getId());
                existingStock.addWarehouse(warehouse);
            }
        }
        
        // Проверяем, что все склады принадлежат пользователю через WarehouseService
        warehouseService.validateWarehousesOwnership(productStockDto.getWarehouses());
        
        existingStock.setLastSyncAt(LocalDateTime.now());
        existingStock.setSyncStatus("UPDATED");
        
        ProductStock saved = productStockRepository.save(existingStock);
        
        // Отслеживаем изменение количества, если оно изменилось
        if (saved.getQuantity() != null && saved.getQuantity() != oldQuantity) {
            productHistoryInterceptor.trackProductStockQuantityChange(saved, oldQuantity, saved.getQuantity());
        }
        
        return productStockMapper.toDto(saved);
    }
    
    /**
     * Удалить остаток товара
     */
    public void deleteProductStock(UUID productStockId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        ProductStock stock = productStockRepository.findById(productStockId)
                .orElseThrow(() -> new RuntimeException("Остаток товара не найден"));
        
        if (!stock.getUserId().equals(userId)) {
            throw new RuntimeException("Остаток товара не принадлежит пользователю");
        }
        
        productStockRepository.delete(stock);
    }
}
