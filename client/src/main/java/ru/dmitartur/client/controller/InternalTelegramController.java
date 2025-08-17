package ru.dmitartur.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dmitartur.client.service.InternalTelegramService;

import java.util.Map;

@RestController
@RequestMapping("/internal/telegram")
@RequiredArgsConstructor
public class InternalTelegramController {

    private final InternalTelegramService internalTelegramService;

    @GetMapping("/exists/{chatId}")
    public ResponseEntity<Map<String, Object>> exists(@PathVariable("chatId") long chatId) {
        boolean exists = internalTelegramService.existsByChatId(chatId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
} 