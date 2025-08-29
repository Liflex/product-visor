package ru.dmitartur.client.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.client.metrics.BusinessMetrics;
import ru.dmitartur.client.service.TelegramClientService;
import ru.dmitartur.common.events.EventType;
import ru.dmitartur.common.events.StartCommandEvent;
import ru.dmitartur.common.events.UserRegistrationSubmitEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsConsumer {

    private final ObjectMapper objectMapper;
    private final TelegramClientService telegramClientService;
    private final BusinessMetrics businessMetrics;

    @KafkaListener(topics = ru.dmitartur.common.kafka.KafkaTopics.USER_EVENTS_TOPIC, groupId = "client-service")
    @Transactional
    public void onUserEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String type = root.path("type").asText("");
            
            if (EventType.StartCommand.name().equals(type)) {
                StartCommandEvent event = objectMapper.treeToValue(root, StartCommandEvent.class);
                telegramClientService.upsertFromStart(event);
                businessMetrics.incrementStartCommand();
            } else if (EventType.UserRegistrationSubmit.name().equals(type)) {
                UserRegistrationSubmitEvent event = objectMapper.treeToValue(root, UserRegistrationSubmitEvent.class);
                telegramClientService.upsertFromRegistration(event);
                businessMetrics.incrementUserRegistration();
            }
        } catch (Exception e) {
            log.error("Failed to process user event", e);
        }
    }
}


