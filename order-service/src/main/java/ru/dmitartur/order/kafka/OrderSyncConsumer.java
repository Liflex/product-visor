package ru.dmitartur.order.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.order.service.OrderUpsertService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncConsumer {

    private final OrderUpsertService orderUpsertService;

    @KafkaListener(
        topics = KafkaTopics.ORDER_SYNC_TOPIC,
        groupId = "order-service-sync-group",
        containerFactory = "jsonKafkaListenerContainerFactory"
    )
    public void handleOrderUpsert(@Payload(required = false) OrderDto orderDto,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key,
                                  @Payload(required = false) Object raw) {
        try {
            if (orderDto == null && raw instanceof java.util.Map<?, ?> map) {
                // Фоллбэк: маппинг LinkedHashMap -> OrderDto, если вдруг не сработали type headers
                log.warn("⚠️ Payload arrived as Map; attempting to convert to OrderDto");
                orderDto = new com.fasterxml.jackson.databind.ObjectMapper().convertValue(map, OrderDto.class);
            }
            assert orderDto != null;
            log.info("📥 Received order upsert via Kafka: postingNumber={}, key companyId = {}", orderDto.getPostingNumber(), key);
            orderUpsertService.upsert(orderDto);
        } catch (Exception e) {
            log.error("❌ Failed to upsert order from Kafka: {}", e.getMessage(), e);
        }
    }
}


