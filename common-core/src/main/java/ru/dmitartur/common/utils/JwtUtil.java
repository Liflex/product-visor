package ru.dmitartur.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import ru.dmitartur.common.security.CurrentUserDto;
import ru.dmitartur.common.security.JwtClaims;
import ru.dmitartur.common.security.Role;

import java.util.Optional;

public class JwtUtil {
    /**
     * Получить текущий JWT токен из SecurityContext
     */
    public static Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    /**
     * Получить subject (user id/email) из JWT
     */
    public static Optional<String> getCurrentSubject() {
        return getCurrentJwt().map(Jwt::getSubject);
    }

    /**
     * Получить claim по имени
     */
    public static Optional<Object> getClaim(String claimName) {
        return getCurrentJwt().map(jwt -> jwt.getClaim(claimName));
    }

    public static Optional<String> getCurrentId() {
        return getClaim(JwtClaims.USER_ID).map(Object::toString);
    }

    public static Optional<String> getCurrentEmail() {
        return getClaim(JwtClaims.EMAIL).map(Object::toString);
    }

    public static Optional<Role> getCurrentRole() {
        return getClaim(JwtClaims.ROLE)
            .map(Object::toString)
            .map(s -> {
                try {
                    return Role.valueOf(s);
                } catch (Exception e) {
                    return null;
                }
            });
    }

    public static Optional<String> getCurrentLocale() {
        return getClaim(JwtClaims.LOCALE).map(Object::toString);
    }

    public static Optional<String> getCurrentTimezone() {
        return getClaim(JwtClaims.TIMEZONE).map(Object::toString);
    }

    public static Optional<CurrentUserDto> getCurrentUser() {
        return getCurrentId().map(id -> new CurrentUserDto(
            id,
            getCurrentEmail().orElse(null),
            getCurrentRole().orElse(null),
            getCurrentLocale().orElse(null),
            getCurrentTimezone().orElse(null)
        ));
    }
} 