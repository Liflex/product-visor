package ru.dmitartur.order.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.events.EventType;
import ru.dmitartur.order.entity.Order;

import java.time.LocalDateTime;
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
    public void sendOrderCreatedEvent(Order order) {
        sendEvent(order, EventType.ORDER_CREATED);
    }
    
    /**
     * Отправить событие отмены заказа
     */
    public void sendOrderCancelledEvent(Order order) {
        sendEvent(order, EventType.ORDER_CANCELLED);
    }
    
    private void sendEvent(Order order, EventType eventType) {
        try {
            // Создаем событие на основе данных заказа
            ObjectNode event = objectMapper.createObjectNode();
            event.put("eventType", eventType.name());
            event.put("postingNumber", order.getPostingNumber());
            event.put("source", order.getSource());
            event.put("eventTime", LocalDateTime.now().toString());
            
            // Добавляем товары заказа
            ObjectNode productsNode = objectMapper.createObjectNode();
            var productsArray = objectMapper.createArrayNode();
            
            double totalPrice = 0.0;
            for (var item : order.getItems()) {
                if (item.getProductId() != null) { // Только найденные продукты
                    ObjectNode product = objectMapper.createObjectNode();
                    product.put("offer_id", item.getOfferId()); // offer_id = article
                    product.put("name", item.getName());
                    product.put("quantity", item.getQuantity());
                    product.put("sku", item.getSku());
                    product.put("price", item.getPrice() != null ? item.getPrice().toString() : "0");
                    productsArray.add(product);
                    
                    // Вычисляем общую стоимость
                    if (item.getPrice() != null) {
                        totalPrice += item.getPrice().doubleValue() * item.getQuantity();
                    }
                }
            }
            event.set("items", productsArray);
            event.put("totalPrice", String.format("%.2f", totalPrice));
            
            // Добавляем название первого товара как название заказа
            if (!order.getItems().isEmpty()) {
                String firstProductName = order.getItems().get(0).getName();
                if (firstProductName != null && !firstProductName.isEmpty()) {
                    event.put("orderName", firstProductName);
                }
            }
            
            String message = objectMapper.writeValueAsString(event);
            String key = order.getPostingNumber();
            
            log.info("📤 Sending {} event to Kafka: postingNumber={}, items={}, totalPrice={}", 
                    eventType, key, order.getItems().size(), event.path("totalPrice").asText("N/A"));
            
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
                    eventType.name(), order.getPostingNumber(), e.getMessage());
        }
    }
}
