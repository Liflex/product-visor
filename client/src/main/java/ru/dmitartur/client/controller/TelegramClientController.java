package ru.dmitartur.client.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.client.service.TelegramClientService;

@Slf4j
@RestController
@RequestMapping("/api/v1/telegram-client")
@RequiredArgsConstructor
public class TelegramClientController {

    private final TelegramClientService telegramClientService;


}

