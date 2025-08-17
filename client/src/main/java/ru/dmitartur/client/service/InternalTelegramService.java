package ru.dmitartur.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.client.repository.TelegramClientRepository;

@Service
@RequiredArgsConstructor
public class InternalTelegramService {

    private final TelegramClientRepository telegramClientRepository;

    @Transactional(readOnly = true)
    public boolean existsByChatId(long chatId) {
        return telegramClientRepository.existsById(chatId);
    }
} 