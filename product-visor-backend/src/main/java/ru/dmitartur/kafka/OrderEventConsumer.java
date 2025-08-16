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
import ru.dmitartur.entity.Product;
import ru.dmitartur.service.ProductService;

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
                                @Header(KafkaHeaders.OFFSET) long offset,
                                @Header(KafkaHeaders.ACKNOWLEDGMENT) org.springframework.kafka.support.Acknowledgment ack) {
        
        try {
            log.debug("📥 Received order event: topic={}, partition={}, offset={}", topic, partition, offset);
            
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();
            String postingNumber = event.path("postingNumber").asText();
            JsonNode items = event.path("items");
            
            log.info("📦 Processing {} event: postingNumber={}, items={}", eventType, postingNumber, items.size());
            
            switch (eventType) {
                case "ORDER_CREATED":
                    handleOrderCreated(items, postingNumber);
                    break;
                case "ORDER_CANCELLED":
                    handleOrderCancelled(items, postingNumber);
                    break;
                default:
                    log.warn("⚠️ Unknown event type: {}", eventType);
            }
            
        } catch (Exception e) {
            log.error("❌ Error processing order event: message={}, error={}", message, e.getMessage(), e);
        } finally {
            // Подтверждаем обработку сообщения
            ack.acknowledge();
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
            boolean updated = productService.updateQuantityByArticle(article, quantityChange);
            
            if (updated) {
                log.info("✅ Updated product quantity: article={}, change={}, event={}, postingNumber={}", 
                        article, quantityChange, eventType, postingNumber);
            } else {
                log.warn("⚠️ Product not found by article: article={}, event={}, postingNumber={}", 
                        article, eventType, postingNumber);
            }
            
        } catch (Exception e) {
            log.error("❌ Error updating product quantity: article={}, event={}, postingNumber={}, error={}", 
                    article, eventType, postingNumber, e.getMessage());
        }
    }
}
