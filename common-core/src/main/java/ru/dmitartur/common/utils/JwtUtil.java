package ru.dmitartur.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.dmitartur.common.security.CurrentUserDto;
import ru.dmitartur.common.security.JwtClaims;
import ru.dmitartur.common.security.Role;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
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

    /**
     * Получить обязательный ownerId как UUID, или бросить IllegalStateException
     */
    public static UUID getRequiredOwnerId() {
        String id = getCurrentId().orElseThrow(() -> new IllegalStateException("Missing user_id in JWT"));
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid user_id format, expected UUID, got: " + id, e);
        }
    }

    /**
     * Получить обязательный ownerId как Long, или бросить IllegalStateException
     * @deprecated Use getRequiredOwnerId() instead
     */
    @Deprecated
    public static long getRequiredOwnerIdLong() {
        String id = getCurrentId().orElseThrow(() -> new IllegalStateException("Missing user_id in JWT"));
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid user_id format, expected numeric id, got: " + id, e);
        }
    }

    /**
     * Получить company id из JWT claims (приоритетно) или пусто, если нет
     */
    public static Optional<String> getCurrentCompanyId() {
        return getClaim(JwtClaims.COMPANY_ID).map(Object::toString).filter(StringUtils::hasText);
    }

    /**
     * Получить множество доступных компаний пользователя из JWT (если включено в токен)
     */
    @SuppressWarnings("unchecked")
    public static Optional<Set<String>> getAvailableCompanyIds() {
        return getClaim(JwtClaims.COMPANY_IDS)
            .map(value -> {
                if (value instanceof Iterable<?> iterable) {
                    Set<String> set = StreamSupport.stream(iterable.spliterator(), false)
                            .map(Object::toString)
                            .collect(Collectors.toSet());
                    return (Set<String>) set;
                }
                return Set.of(value.toString());
            });
    }

    /**
     * Получить company id из заголовка X-Company-Id, если присутствует
     */
    public static Optional<String> getCompanyIdFromHeader() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                String header = servletAttrs.getRequest().getHeader("X-Company-Id");
                if (StringUtils.hasText(header)) {
                    return Optional.of(header);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to read X-Company-Id header: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Эффективный company id: сначала из заголовка, затем из JWT claims
     */
    public static Optional<String> resolveEffectiveCompanyId() {
        return getCompanyIdFromHeader().or(() -> getCurrentCompanyId());
    }
} 