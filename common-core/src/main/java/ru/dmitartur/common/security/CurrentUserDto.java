package ru.dmitartur.common.security;

import lombok.Value;

@Value
public class CurrentUserDto {
    String id;
    String email;
    Role role;
    String locale;
    String timezone;
} 