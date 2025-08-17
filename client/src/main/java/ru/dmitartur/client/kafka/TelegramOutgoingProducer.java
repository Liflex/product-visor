package ru.dmitartur.client.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramOutgoingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${oficiant.topics.telegram-outgoing:telegram.outgoing.messages}")
    private String telegramOutgoingTopic;

    public void sendWelcomeMessage(long chatId, String botId, String firstName) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "welcome")
                            .set("params", objectMapper.createObjectNode().put("first_name", firstName)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send welcome message", e);
        }
    }

    public void sendWelcomeBackMessage(long chatId, String botId, String firstName) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "welcome_back")
                            .set("params", objectMapper.createObjectNode().put("first_name", firstName)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send welcome back message", e);
        }
    }

    public void sendUserRegistered(long chatId, String botId, String firstName) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "user.registered")
                            .set("params", objectMapper.createObjectNode().put("first_name", firstName)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send user.registered message", e);
        }
    }

    public void sendSubscriptionActivated(long chatId, String botId) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "subscription.activated")
                            .set("params", objectMapper.createObjectNode()))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send subscription.activated message", e);
        }
    }

    public void sendSubscriptionExpired(long chatId, String botId) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "subscription.expired")
                            .set("params", objectMapper.createObjectNode()))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send subscription.expired message", e);
        }
    }

    public void sendOrderCreated(long chatId, String botId, String postingNumber, String source, String orderName, String totalPrice) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "order.created")
                            .set("params", objectMapper.createObjectNode()
                                    .put("posting_number", postingNumber)
                                    .put("source", source)
                                    .put("order_name", orderName)
                                    .put("total_price", totalPrice)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send order.created message", e);
        }
    }

    public void sendOrderCancelled(long chatId, String botId, String postingNumber, String source, String orderName, String totalPrice) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", "order.cancelled")
                            .set("params", objectMapper.createObjectNode()
                                    .put("posting_number", postingNumber)
                                    .put("source", source)
                                    .put("order_name", orderName)
                                    .put("total_price", totalPrice)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send order.cancelled message", e);
        }
    }

    /**
     * Generic method to send any message to Telegram
     */
    public void sendMessage(long chatId, String botId, String template, Object params) {
        try {
            String payload = objectMapper.createObjectNode()
                    .put("chat_id", chatId)
                    .put("bot_id", botId)
                    .set("body", objectMapper.createObjectNode()
                            .put("template", template)
                            .set("params", objectMapper.valueToTree(params)))
                    .toString();
            kafkaTemplate.send(telegramOutgoingTopic, String.valueOf(chatId), payload);
        } catch (Exception e) {
            log.error("Failed to send message with template: {}", template, e);
        }
    }
}


