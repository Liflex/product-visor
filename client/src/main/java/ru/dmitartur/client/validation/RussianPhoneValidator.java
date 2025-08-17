package ru.dmitartur.client.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RussianPhoneValidator implements ConstraintValidator<RussianPhone, String> {
    private static final String RUSSIAN_PHONE_REGEX = "^((\\+7|7|8)[\\s-]?)?(\\(?\\d{3}\\)?[\\s-]?)?[\\d\\s-]{7,10}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true; // @NotBlank отдельно
        return value.matches(RUSSIAN_PHONE_REGEX);
    }
} 