package ru.dmitartur.common.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * Событие, возникающее при получении команды /start от пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StartCommandEvent {
    private Long chatId;
    private String botId;
    private String username;
    private String firstName;
    private String lastName;
    private Instant eventTime;
}
