package ru.dmitartur.client.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.dmitartur.client.repository.TelegramClientRepository;
import ru.dmitartur.client.kafka.TelegramOutgoingProducer;
import ru.dmitartur.common.events.EventType;

import java.util.List;

/**
 * Consumer для обработки событий заказов и отправки уведомлений в Telegram
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private final ObjectMapper objectMapper;
    private final TelegramClientRepository telegramClientRepository;
    private final TelegramOutgoingProducer telegramOutgoingProducer;

    @KafkaListener(topics = ru.dmitartur.common.kafka.KafkaTopics.ORDER_EVENTS_TOPIC, groupId = "telegram-notifications")
    public void onOrderEvent(String message) {
        try {
            JsonNode event = objectMapper.readTree(message);
            String eventType = event.path("eventType").asText();
            String postingNumber = event.path("postingNumber").asText();
            String source = event.path("source").asText();
            
            // Извлекаем дополнительную информацию о заказе
            String orderName = "";
            String totalPrice = "";
            JsonNode items = event.path("items");
            
            // Получаем название и стоимость из первого товара или из корневых полей
            if (items.isArray() && items.size() > 0) {
                JsonNode firstItem = items.get(0);
                orderName = firstItem.path("name").asText("");
            }
            
            // Пытаемся получить стоимость из корневых полей события
            totalPrice = event.path("totalPrice").asText("");
            if (totalPrice.isEmpty()) {
                // Если нет в корне, пытаемся вычислить из товаров
                if (items.isArray() && items.size() > 0) {
                    double total = 0.0;
                    for (JsonNode item : items) {
                        String priceStr = item.path("price").asText("0");
                        int quantity = item.path("quantity").asInt(1);
                        try {
                            total += Double.parseDouble(priceStr) * quantity;
                        } catch (NumberFormatException e) {
                            log.warn("⚠️ Invalid price format: {}", priceStr);
                        }
                    }
                    totalPrice = String.format("%.2f", total);
                }
            }

            log.info("📦 Processing order event: type={}, postingNumber={}, source={}, name={}, totalPrice={}", 
                    eventType, postingNumber, source, orderName, totalPrice);

            // Получаем всех активных пользователей для отправки уведомлений
            List<ru.dmitartur.client.entity.TelegramClient> clients = telegramClientRepository.findAll();

            ru.dmitartur.common.events.EventType type = ru.dmitartur.common.events.EventType.from(eventType);
            if (type == null) {
                log.warn("⚠️ Unknown order event type: {}", eventType);
                return;
            }
            switch (type) {
                case ORDER_CREATED:
                    for (var client : clients) {
                        telegramOutgoingProducer.sendOrderCreated(
                                client.getChatId(), 
                                client.getBotId(), 
                                postingNumber, 
                                source,
                                orderName,
                                totalPrice
                        );
                    }
                    log.info("✅ Sent ORDER_CREATED notifications to {} clients", clients.size());
                    break;

                case ORDER_CANCELLED:
                    for (var client : clients) {
                        telegramOutgoingProducer.sendOrderCancelled(
                                client.getChatId(), 
                                client.getBotId(), 
                                postingNumber, 
                                source,
                                orderName,
                                totalPrice
                        );
                    }
                    log.info("✅ Sent ORDER_CANCELLED notifications to {} clients", clients.size());
                    break;

                default:
                    log.warn("⚠️ Unknown order event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("❌ Failed to process order event", e);
        }
    }
}
