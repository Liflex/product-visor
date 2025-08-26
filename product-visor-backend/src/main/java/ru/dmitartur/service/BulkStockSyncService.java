package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.enums.ProductStockType;
import ru.dmitartur.entity.Product;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.repository.ProductStockRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkStockSyncService {

    private final ProductStockRepository productStockRepository;
    private final StockSyncService stockSyncService;

    /**
     * Синхронизировать остатки для выбранных продуктов
     */
    public void syncStocksForProducts(List<Long> productIds, ProductStockType stockType, UUID userId) {
        log.info("🚀 Starting bulk stock sync: productIds={}, stockType={}, userId={}", productIds, stockType, userId);

        // 1) Берем сразу ProductStock по списку продуктов и типу
        List<ProductStock> stocks = productStockRepository.findByProductIdInAndStockTypeAndUserId(
                productIds, stockType, userId);

        if (stocks.isEmpty()) {
            log.warn("❌ No ProductStock found for given products and type: productIds={}, stockType={}", productIds, stockType);
            throw new RuntimeException("Не найдено остатков для синхронизации по заданным продуктам и типу");
        }

        stockSyncService.syncStocksToMarketplace(stocks);

        // 3) Для каждого продукта формируем уникальный Set складов из остатков и синхронизируем
        int syncedProducts = 0;
        for (ProductStock stock : stocks) {
            Product product = stock.getProduct();

            if (stock.getWarehouses().isEmpty()) {
                log.warn("⚠️ Product {} has no warehouses in stocks", product.getId());
            }

            String marketplace = getMarketplaceFromStockType(stockType);
            log.info("➡️ Syncing product id={}, article={}, companyId={} to marketplace={} for warehouses={}",
                    product.getId(), product.getArticle(), product.getCompanyId(), marketplace, stock.getWarehouseIds());

            // Обновляем статус синхронизации по всем stock записям продукта
            stock.setLastSyncAt(LocalDateTime.now());
            stock.setSyncStatus("SYNCING");
            productStockRepository.save(stock);

            syncedProducts++;
        }

        log.info("✅ Bulk sync completed: requested={}, withStocks={}, type={}", productIds.size(), syncedProducts, stockType);
    }

    /**
     * Получить статистику синхронизации
     */
    public Map<String, Object> getSyncStats(List<Long> productIds, ProductStockType stockType, UUID userId) {
        Map<String, Object> stats = new HashMap<>();

        // Получаем остатки
        List<ProductStock> stocks = productStockRepository.findByProductIdInAndStockTypeAndUserId(
                productIds, stockType, userId);

        // Подсчитываем статистику
        long totalProducts = productIds != null ? productIds.size() : 0;
        long productsWithStocks = stocks.stream()
                .map(stock -> stock.getProduct().getId())
                .distinct()
                .count();

        long totalQuantity = stocks.stream()
                .mapToInt(ProductStock::getQuantity)
                .sum();

        long syncingCount = stocks.stream()
                .filter(stock -> "SYNCING".equals(stock.getSyncStatus()))
                .count();

        long syncedCount = stocks.stream()
                .filter(stock -> "SYNCED".equals(stock.getSyncStatus()))
                .count();

        // Подсчитываем общее количество уникальных складов
        long totalWarehouses = stocks.stream()
                .flatMap(stock -> stock.getWarehouseIds().stream())
                .distinct()
                .count();

        stats.put("totalProducts", totalProducts);
        stats.put("productsWithStocks", productsWithStocks);
        stats.put("totalStocks", stocks.size());
        stats.put("totalQuantity", totalQuantity);
        stats.put("totalWarehouses", totalWarehouses);
        stats.put("syncingCount", syncingCount);
        stats.put("syncedCount", syncedCount);
        stats.put("stockType", stockType);

        return stats;
    }

    /**
     * Получить маркетплейс из типа остатка
     */
    private String getMarketplaceFromStockType(ProductStockType stockType) {
        return switch (stockType) {
            case YANDEX_FBO -> "YANDEX";
            case OZON_FBO -> "OZON";
            default -> "ALL"; // Для FBS отправляем на все маркетплейсы
        };
    }
}
