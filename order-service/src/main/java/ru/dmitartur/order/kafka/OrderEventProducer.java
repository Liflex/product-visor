package ru.dmitartur.order.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.events.EventType;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.mapper.OrderMapper;

import java.util.concurrent.CompletableFuture;

/**
 * Producer для отправки событий заказов в Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderEventProducer {

    @PostConstruct
    public void init() {
        log.info("🚀 OrderEventProducer initialized and ready to send events to topic: {}", orderEventsTopic);
    }

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OrderMapper orderMapper;

    @Value("${kafka.topics.order-events:order-events}")
    private String orderEventsTopic; // kept for config override; default ties to KafkaTopics.ORDER_EVENTS_TOPIC

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
            OrderDto dto = orderMapper.toDto(order);
            dto.setEventType(eventType);
            String key = order.getPostingNumber();

            log.info("📤 Sending {} event to Kafka: postingNumber={}, items={}",
                    eventType, key, order.getItems().size());

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(KafkaTopics.ORDER_EVENTS_TOPIC, key, dto);

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
