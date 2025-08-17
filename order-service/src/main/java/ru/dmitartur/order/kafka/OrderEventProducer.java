package ru.dmitartur.order.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Producer для отправки событий заказов в Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventProducer {
    
    @javax.annotation.PostConstruct
    public void init() {
        log.info("🚀 OrderEventProducer initialized and ready to send events to topic: {}", orderEventsTopic);
    }
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic;
    
    /**
     * Отправить событие создания заказа
     */
    public void sendOrderCreatedEvent(JsonNode orderData) {
        sendEvent(orderData, "ORDER_CREATED");
    }
    
    /**
     * Отправить событие отмены заказа
     */
    public void sendOrderCancelledEvent(JsonNode orderData) {
        sendEvent(orderData, "ORDER_CANCELLED");
    }
    
    private void sendEvent(JsonNode orderData, String eventType) {
        try {
            // Создаем событие на основе данных заказа
            ObjectNode event = objectMapper.createObjectNode();
            event.put("eventType", eventType);
            event.put("postingNumber", orderData.path("posting_number").asText());
            event.put("source", orderData.path("source").asText());
            event.put("eventTime", OffsetDateTime.now().toString());
            event.set("items", orderData.path("products"));
            
            // Добавляем общую стоимость заказа
            JsonNode products = orderData.path("products");
            if (products.isArray() && products.size() > 0) {
                double totalPrice = 0.0;
                for (JsonNode product : products) {
                    String priceStr = product.path("price").asText("0");
                    int quantity = product.path("quantity").asInt(1);
                    try {
                        totalPrice += Double.parseDouble(priceStr) * quantity;
                    } catch (NumberFormatException e) {
                        log.warn("⚠️ Invalid price format in product: {}", priceStr);
                    }
                }
                event.put("totalPrice", String.format("%.2f", totalPrice));
            }
            
            // Добавляем название первого товара как название заказа
            if (products.isArray() && products.size() > 0) {
                String firstProductName = products.get(0).path("name").asText("");
                if (!firstProductName.isEmpty()) {
                    event.put("orderName", firstProductName);
                }
            }
            
            String message = objectMapper.writeValueAsString(event);
            String key = orderData.path("posting_number").asText();
            
            log.info("📤 Sending {} event to Kafka: postingNumber={}, items={}, totalPrice={}", 
                    eventType, key, products.size(), event.path("totalPrice").asText("N/A"));
            
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(orderEventsTopic, key, message);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("✅ {} event sent successfully: postingNumber={}, partition={}, offset={}", 
                            eventType, key, result.getRecordMetadata().partition(), 
                            result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to send {} event: postingNumber={}, error={}", 
                            eventType, key, ex.getMessage());
                    // Не выбрасываем исключение, чтобы не прерывать основной поток
                }
            });
            
        } catch (Exception e) {
            log.error("❌ Error sending {} event: postingNumber={}, error={}", 
                    eventType, orderData.path("posting_number").asText(), e.getMessage());
        }
    }
}
