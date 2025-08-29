package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.listener.ProductStockEventPublisher;
import ru.dmitartur.repository.ProductStockRepository;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.dto.ProductStockDto;
import ru.dmitartur.mapper.ProductStockMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockService {
    
    private final ProductStockRepository productStockRepository;
    private final ProductStockMapper productStockMapper;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final ProductStockEventPublisher eventPublisher;
    
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
     * Получить ProductStock Entity по ID с проверкой прав доступа
     */
    public ProductStock getProductStockEntityById(UUID productStockId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        ProductStock stock = productStockRepository.findById(productStockId)
                .orElseThrow(() -> new RuntimeException("Остатки товара не найдены"));
        
        if (!stock.getUserId().equals(userId)) {
            throw new RuntimeException("Остатки товара не принадлежат пользователю");
        }
        
        return stock;
    }
    
    /**
     * Создать новый остаток товара
     */
    @Transactional
    public ProductStock createProductStock(ProductStock productStock) {
        ProductStock saved = productStockRepository.save(productStock);
        
        // Публикуем событие создания
        if (saved.getQuantity() != null && saved.getQuantity() > 0) {
            eventPublisher.publishQuantityChangeEventWithContext(saved, 0, saved.getQuantity(), "STOCK_SERVICE", "CREATED");
        }
        
        return saved;
    }
    
    /**
     * Обновить остаток товара
     */
    @Transactional
    public ProductStock updateProductStock(ProductStock productStock) {
        // Получаем существующий ProductStock для обновления
        ProductStock existingStock = productStockRepository.findById(productStock.getId())
                .orElseThrow(() -> new RuntimeException("ProductStock не найден"));
        
        // Сохраняем старое количество для отслеживания изменений
        Integer oldQuantity = existingStock.getQuantity();
        
        // Обновляем Entity через маппер
        productStockMapper.updateProductStock(existingStock, productStock);
        
        // Сохраняем обновленный Entity
        ProductStock saved = productStockRepository.save(existingStock);
        
        // Публикуем событие изменения количества, если оно изменилось
        if (saved.getQuantity() != null && !saved.getQuantity().equals(oldQuantity)) {
            eventPublisher.publishQuantityChangeEventWithContext(saved, oldQuantity, saved.getQuantity(), "STOCK_SERVICE", "UPDATED");
        }
        
        return saved;
    }
    
    /**
     * Обновить остаток товара с контекстом для правильного определения причины изменения
     */
    @Transactional
    public ProductStock updateProductStockWithContext(ProductStock productStock, String sourceSystem, String sourceId) {
        // Получаем существующий ProductStock для обновления
        ProductStock existingStock = productStockRepository.findById(productStock.getId())
                .orElseThrow(() -> new RuntimeException("ProductStock не найден"));
        
        // Сохраняем старое количество для отслеживания изменений
        Integer oldQuantity = existingStock.getQuantity();
        
        // Обновляем Entity через маппер
        productStockMapper.updateProductStock(existingStock, productStock);
        
        // Сохраняем обновленный Entity
        ProductStock saved = productStockRepository.save(existingStock);
        
        // Публикуем событие изменения количества, если оно изменилось
        if (saved.getQuantity() != null && !saved.getQuantity().equals(oldQuantity)) {
            // Передаем контекст в событие
            eventPublisher.publishQuantityChangeEventWithContext(saved, oldQuantity, saved.getQuantity(), sourceSystem, sourceId);
        }
        
        return saved;
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
    
    /**
     * Найти ProductStock по артикулу, складу и компании
     * Используется для обработки событий заказов из Kafka
     */
    public Optional<ProductStock> findProductStockByArticleAndWarehouse(String article, String warehouseId, UUID companyId) {
        return productStockRepository.findByArticleAndWarehouseIdAndCompanyId(article, warehouseId, companyId);
    }
}
