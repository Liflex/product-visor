package ru.dmitartur.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.client.entity.TelegramClient;
import ru.dmitartur.client.mapper.TelegramClientMapper;
import ru.dmitartur.client.repository.TelegramClientRepository;
import ru.dmitartur.client.kafka.TelegramOutgoingProducer;
import ru.dmitartur.common.events.StartCommandEvent;
import ru.dmitartur.common.events.UserRegistrationSubmitEvent;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Service that encapsulates all CRUD and business operations around {@link TelegramClient}.
 *
 * Responsibilities:
 * - Create/Update {@link TelegramClient} from incoming domain events
 * - Maintain premium status lifecycle
 * - Emit user-facing notifications to telegram.outgoing topic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramClientService {

    private final TelegramClientRepository telegramClientRepository;
    private final TelegramClientMapper telegramClientMapper;
    private final TelegramOutgoingProducer telegramOutgoingProducer;

    /**
     * Upsert client profile from a finalized registration event and notify user.
     *
     * - If client does not exist, creates a new one
     * - Updates profile fields from payload
     * - Initializes {@code registeredAt} if missing
     * - Sends confirmation message to telegram.outgoing with template 'user.registered'
     */
    @Transactional
    public void upsertFromStart(StartCommandEvent event) {
        long chatId = event.getChatId();
        TelegramClient client = telegramClientRepository.findById(chatId).orElseGet(TelegramClient::new);
        TelegramClient mapped = telegramClientMapper.toEntity(event);

        client.setChatId(mapped.getChatId());
        client.setBotId(mapped.getBotId());
        client.setFirstName(mapped.getFirstName());
        client.setLastName(mapped.getLastName());
        client.setEmail(mapped.getEmail());
        client.setUsername(mapped.getUsername());
        if (client.getRegisteredAt() == null) {
            client.setRegisteredAt(LocalDateTime.now());
        }
        telegramClientRepository.save(client);

        telegramOutgoingProducer.sendUserRegistered(chatId, event.getBotId(), mapped.getFirstName());
    }

    @Transactional
    public void upsertFromRegistration(UserRegistrationSubmitEvent event) {
        long chatId = event.getChatId();
        TelegramClient client = telegramClientRepository.findById(chatId).orElseGet(TelegramClient::new);
        TelegramClient mapped = telegramClientMapper.toEntity(event);

        client.setChatId(mapped.getChatId());
        client.setBotId(mapped.getBotId());
        client.setFirstName(mapped.getFirstName());
        client.setLastName(mapped.getLastName());
        client.setEmail(mapped.getEmail());
        client.setUsername(mapped.getUsername());
        if (client.getRegisteredAt() == null) {
            client.setRegisteredAt(LocalDateTime.now());
        }
        telegramClientRepository.save(client);

        telegramOutgoingProducer.sendUserRegistered(chatId, event.getBotId(), mapped.getFirstName());
    }
}


