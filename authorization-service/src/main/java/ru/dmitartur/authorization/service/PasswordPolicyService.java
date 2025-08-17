package ru.dmitartur.authorization.service;

import org.springframework.stereotype.Service;

@Service
public class PasswordPolicyService {
    public void validateOrThrow(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasUpper || !hasLower || !hasDigit) {
            throw new IllegalArgumentException("Password must contain upper, lower case letters and digits");
        }
    }
}



