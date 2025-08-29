package ru.dmitartur.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.dmitartur.event.ProductStockQuantityChangeEvent;
import ru.dmitartur.interceptor.ProductHistoryInterceptor;

/**
 * Слушатель событий изменения количества товара в ProductStock
 * Обрабатывает события асинхронно для отслеживания истории изменений
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStockQuantityChangeListener {

    private final ProductHistoryInterceptor productHistoryInterceptor;

    /**
     * Обрабатывает событие изменения количества товара
     */
    @Async
    @EventListener
    public void handleProductStockQuantityChange(ProductStockQuantityChangeEvent event) {
        try {
            log.info("📝 Processing ProductStock quantity change event: productStockId={}, productArticle={}, oldQuantity={}, newQuantity={}",
                    event.getProductStock().getId(), event.getProductStock().getProduct().getArticle(), event.getOldQuantity(),
                    event.getNewQuantity());

            // Определяем контекст из события или используем значения по умолчанию
            String sourceSystem = event.getSourceSystem() != null ? event.getSourceSystem() : "STOCK_SERVICE";
            String sourceId = event.getSourceId() != null ? event.getSourceId() : event.getProductStock().getId().toString();
            String changeReason = "STOCK_UPDATE"; // По умолчанию для событий из ProductStockService

            // Вызываем interceptor с данными из события, включая склады и контекст
            productHistoryInterceptor.trackProductStockQuantityChange(
                    event.getProductStock(),
                    event.getOldQuantity(),
                    event.getNewQuantity(),
                    changeReason,
                    sourceSystem,
                    sourceId);

            log.info("✅ Successfully processed ProductStock quantity change event: productStockId={}",
                    event.getProductStock().getId());

        } catch (Exception e) {
            log.error("❌ Error processing ProductStock quantity change event: productStockId={}, error={}",
                    event.getProductStock().getId(), e.getMessage(), e);
        }
    }

    /**
     * Определяет систему-источник на основе причины изменения
     */
    private String determineSourceSystem(String changeReason) {
        if (changeReason != null) {
            if (changeReason.contains("ORDER_")) {
                return "KAFKA";
            } else if (changeReason.equals("CREATED") || changeReason.equals("UPDATED")) {
                return "REST_API";
            } else if (changeReason.equals("STOCK_UPDATE")) {
                return "STOCK_SERVICE";
            }
        }
        return "UNKNOWN";
    }
}
