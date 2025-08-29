package ru.dmitartur.ozon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.kafka.KafkaTopics;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncProducer {

    @Qualifier("jsonKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrder(String companyId, OrderDto order) {
        kafkaTemplate.send(KafkaTopics.ORDER_SYNC_TOPIC, companyId, order)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("❌ Failed to publish order to {}: {}", KafkaTopics.ORDER_SYNC_TOPIC, throwable.getMessage());
                    } else {
                        log.debug("✅ Published order {} to {} p{}@{}", order.getPostingNumber(), KafkaTopics.ORDER_SYNC_TOPIC,
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}


