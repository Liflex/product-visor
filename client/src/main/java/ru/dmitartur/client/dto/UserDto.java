package ru.dmitartur.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.dmitartur.client.enums.Role;
import ru.dmitartur.client.validation.RussianPhone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Пользователь системы")
public class UserDto {
    @Schema(description = "Уникальный идентификатор пользователя", example = "b3b6c1e2-8e2a-4c1a-9e2a-1b2c3d4e5f6a")
    private UUID id;

    @Email
    @NotBlank
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "URL фотографии пользователя", example = "https://example.com/photo.jpg")
    private String photo;

    @NotNull
    @Schema(description = "Роль пользователя", example = "EMPLOYEE")
    private Role role;

    @Schema(description = "Признак верификации пользователя", example = "true")
    private boolean isVerified;

    @Schema(description = "Дата регистрации пользователя (ISO 8601)", example = "2024-07-17T12:34:56.789")
    private LocalDateTime registrationDate;

    @NotBlank
    @Schema(description = "Локаль пользователя (язык и регион)", example = "ru_RU")
    private String locale;

    @NotBlank
    @Schema(description = "Часовой пояс пользователя", example = "Europe/Moscow")
    private String timezone;

    @NotBlank
    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @NotBlank
    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Отчество пользователя (опционально)", example = "Иванович")
    private String middleName;

    @Past
    @Schema(description = "Дата рождения пользователя (опционально)", example = "1990-01-01")
    private LocalDate birthDate;

    @NotBlank
    @RussianPhone
    @Schema(description = "Телефон пользователя (российский формат)", example = "+7 999 123-45-67")
    private String phone;
} 