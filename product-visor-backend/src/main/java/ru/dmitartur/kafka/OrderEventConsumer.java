package ru.dmitartur.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.dto.OrderItemDto;
import ru.dmitartur.context.ChangeContextHolder;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.service.ProductStockService;
import ru.dmitartur.listener.ProductStockEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static ru.dmitartur.common.kafka.KafkaTopics.ORDER_EVENTS_TOPIC;

/**
 * Consumer для обработки событий заказов из Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventConsumer {

    @PostConstruct
    public void init() {
        log.info("🚀 OrderEventConsumer initialized and ready to consume from topic: {}", ORDER_EVENTS_TOPIC);
    }

    private final ProductStockService productStockService;
    private final ProductStockEventPublisher eventPublisher;

    @KafkaListener(topics = ORDER_EVENTS_TOPIC,
            groupId = "${kafka.consumer.group-id:product-visor-group}",
            containerFactory = "jsonKafkaListenerContainerFactory")
    public void handleOrderEvent(@Payload OrderDto message,
                                 @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                 @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {

        log.debug("📥 Received order event: topic={}, partition={}, offset={}", topic, partition, offset);
        try (AutoCloseable ignored = ChangeContextHolder.withContext(
                new ChangeContextHolder.ChangeContext(message.getMarket().name(), "KAFKA", key))) {
            if (message.getEventType() == null) {
                log.warn("⚠️ Unknown event type for key: {}", key);
                return;
            }
            switch (message.getEventType()) {
                case ORDER_CREATED:
                    handleOrderCreated(message, key);
                    break;
                case ORDER_CANCELLED:
                    handleOrderCancelled(message, key);
                    break;
                default:
                    log.warn("⚠️ Unhandled event type: {}", message.getEventType());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Обработка события создания заказа - уменьшаем количество товаров
     */
    private void handleOrderCreated(OrderDto message, String postingNumber) {
        for (OrderItemDto item : message.getItems()) {
            try {
                updateProductQuantity(item.getOfferId(), -item.getQuantity(), "ORDER_CREATED", postingNumber, message.getWarehouseId(), message.getCompanyId());
            } catch (Exception e) {
                log.error("❌ Error updating product quantity for ORDER_CREATED: item={}, error={}",
                        item.toString(), e.getMessage());
            }
        }
    }

    /**
     * Обработка события отмены заказа - увеличиваем количество товаров
     */
    private void handleOrderCancelled(OrderDto message, String postingNumber) {
        for (OrderItemDto item : message.getItems()) {
            try {
                updateProductQuantity(item.getOfferId(), item.getQuantity(), "ORDER_CANCELLED", postingNumber, message.getWarehouseId(), message.getCompanyId());
            } catch (Exception e) {
                log.error("❌ Error updating product quantity for ORDER_CANCELLED: item={}, error={}",
                        item.toString(), e.getMessage());
            }
        }
    }

    /**
     * Обновление количества товара по артикулу
     */
    private void updateProductQuantity(String article, int quantityChange, String eventType, String postingNumber, String warehouseId, UUID companyId) {
        if (article == null || article.isEmpty()) {
            log.warn("⚠️ Article is null or empty for {}: postingNumber={}", eventType, postingNumber);
            return;
        }

        try {
            // Находим ProductStock для указанного артикула, склада и компании
            Optional<ProductStock> stockOpt = productStockService.findProductStockByArticleAndWarehouse(
                    article, warehouseId, companyId);

            if (stockOpt.isEmpty()) {
                log.warn("⚠️ ProductStock not found: article={}, warehouseId={}, companyId={}, event={}, postingNumber={}",
                        article, warehouseId, companyId, eventType, postingNumber);
                return;
            }

            ProductStock stock = stockOpt.get();

            // Обновляем количество
            stock.setQuantity(stock.getQuantity() + quantityChange);

            productStockService.updateProductStock(stock);

            log.info("✅ Updated ProductStock quantity: article={}, warehouseId={}, companyId={}, change={}, event={}, postingNumber={}",
                    article, warehouseId, companyId, quantityChange, eventType, postingNumber);
        } catch (Exception e) {
            log.error("❌ Error updating product quantity: article={}, event={}, postingNumber={}, error={}", article, eventType, postingNumber, e.getMessage());
        }
    }
}
