package ru.dmitartur.common.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Событие финализации регистрации пользователя с полными данными
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationSubmitEvent {
    private Long chatId;
    private String botId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime eventTime;
}

