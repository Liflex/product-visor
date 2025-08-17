package ru.dmitartur.common.security;

public enum Role {
    PRIVATE_PERSON("частное лицо"),
    EMPLOYEE("сотрудник"),
    ADMIN("администратор заведения");

    private final String displayName;
    Role(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
} 