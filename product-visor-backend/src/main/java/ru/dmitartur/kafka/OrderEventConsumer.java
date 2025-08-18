package ru.dmitartur.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.dmitartur.service.ProductService;
import ru.dmitartur.context.ChangeContextHolder;
import ru.dmitartur.common.events.EventType;

/**
 * Consumer для обработки событий заказов из Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventConsumer {
    
    @javax.annotation.PostConstruct
    public void init() {
        log.info("🚀 OrderEventConsumer initialized and ready to consume from topic: {}", orderEventsTopic);
    }
    
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;
    
    @KafkaListener(topics = "${kafka.topics.order-events:order-events}", 
                   groupId = "${kafka.consumer.group-id:product-visor-group}")
    public void handleOrderEvent(@Payload String message, 
                                @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                @Header(KafkaHeaders.OFFSET) long offset) {
        
        try {
            log.debug("📥 Received order event: topic={}, partition={}, offset={}", topic, partition, offset);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();
            String postingNumber = event.path("postingNumber").asText();
            JsonNode items = event.path("items");
            String rawMarket = event.hasNonNull("market") ? event.get("market").asText(null) : null;
            String derivedMarket = rawMarket != null ? rawMarket.toLowerCase() :
                (event.hasNonNull("source") && event.get("source").asText("").toUpperCase().startsWith("OZON") ? "ozon" : null);
            
            log.info("📦 Processing {} event: postingNumber={}, items={}", eventType, postingNumber, items.size());
            
            try (AutoCloseable ignored = ChangeContextHolder.withContext(
                new ChangeContextHolder.ChangeContext(derivedMarket, "KAFKA", postingNumber))) {
                EventType type = EventType.from(eventType);
                if (type == null) {
                    log.warn("⚠️ Unknown event type: {}", eventType);
                    return;
                }
                switch (type) {
                    case ORDER_CREATED:
                        handleOrderCreated(items, postingNumber);
                        break;
                    case ORDER_CANCELLED:
                        handleOrderCancelled(items, postingNumber);
                        break;
                    default:
                        log.warn("⚠️ Unhandled event type: {}", eventType);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing order event: message={}, error={}", message, e.getMessage(), e);
        } finally {
            // Автоматическое подтверждение сообщения
            // ack.acknowledge(); // Убрано, так как используем автоматическое подтверждение
        }
    }
    
    /**
     * Обработка события создания заказа - уменьшаем количество товаров
     */
    private void handleOrderCreated(JsonNode items, String postingNumber) {
        for (JsonNode item : items) {
            try {
                String article = item.path("offer_id").asText(); // offer_id = article
                int quantity = item.path("quantity").asInt(1);
                
                updateProductQuantity(article, -quantity, "ORDER_CREATED", postingNumber);
            } catch (Exception e) {
                log.error("❌ Error updating product quantity for ORDER_CREATED: item={}, error={}", 
                        item.toString(), e.getMessage());
            }
        }
    }
    
    /**
     * Обработка события отмены заказа - увеличиваем количество товаров
     */
    private void handleOrderCancelled(JsonNode items, String postingNumber) {
        for (JsonNode item : items) {
            try {
                String article = item.path("offer_id").asText(); // offer_id = article
                int quantity = item.path("quantity").asInt(1);
                
                updateProductQuantity(article, quantity, "ORDER_CANCELLED", postingNumber);
            } catch (Exception e) {
                log.error("❌ Error updating product quantity for ORDER_CANCELLED: item={}, error={}", 
                        item.toString(), e.getMessage());
            }
        }
    }
    
    /**
     * Обновление количества товара по артикулу
     */
    private void updateProductQuantity(String article, int quantityChange, String eventType, String postingNumber) {
        if (article == null || article.isEmpty()) {
            log.warn("⚠️ Article is null or empty for {}: postingNumber={}", eventType, postingNumber);
            return;
        }

        try {
            java.util.Optional<ru.dmitartur.entity.Product> productOpt = productService.findByArticle(article);
            if (!productOpt.isPresent()) {
                log.warn("⚠️ Product not found by article: article={}, event={}, postingNumber={}", article, eventType, postingNumber);
                return;
            }

            ru.dmitartur.entity.Product product = productOpt.get();
            int oldQuantity = product.getQuantity();
            int newQuantity = productService.computeQuantityWithDelta(oldQuantity, quantityChange);
            product.setQuantity(newQuantity);

            ru.dmitartur.entity.Product saved = productService.update(product);
            if (newQuantity != oldQuantity) {
                productService.trackQuantityChange(saved, oldQuantity, newQuantity);
            }

            log.info("✅ Updated product quantity: article={}, change={}, event={}, postingNumber={}", article, quantityChange, eventType, postingNumber);
        } catch (Exception e) {
            log.error("❌ Error updating product quantity: article={}, event={}, postingNumber={}, error={}", article, eventType, postingNumber, e.getMessage());
        }
    }
}
