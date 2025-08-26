package ru.dmitartur.common.security;

import ru.dmitartur.common.utils.JwtUtil;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Утилиты для проверки прав доступа
 */
public class SecurityUtils {
    
    /**
     * Проверить, имеет ли текущий пользователь доступ к компании
     */
    public static boolean hasAccessToCompany(UUID companyId) {
        // Получаем доступные компании из JWT
        Optional<Set<String>> availableCompanies = JwtUtil.getAvailableCompanyIds();
        
        if (availableCompanies.isPresent()) {
            return availableCompanies.get().contains(companyId.toString());
        }
        
        // Если список компаний не в токене, проверяем текущую компанию
        Optional<String> currentCompanyId = JwtUtil.resolveEffectiveCompanyId();
        return currentCompanyId.isPresent() && currentCompanyId.get().equals(companyId.toString());
    }
    
    /**
     * Проверить, является ли текущий пользователь администратором
     */
    public static boolean isAdmin() {
        return JwtUtil.getCurrentRole()
                .map(role -> role.name().equals("ADMIN") || role.name().equals("SUPER_ADMIN"))
                .orElse(false);
    }
    
    /**
     * Проверить, является ли текущий пользователь суперадминистратором
     */
    public static boolean isSuperAdmin() {
        return JwtUtil.getCurrentRole()
                .map(role -> role.name().equals("SUPER_ADMIN"))
                .orElse(false);
    }
    
    /**
     * Получить текущий ID пользователя или выбросить исключение
     */
    public static String getCurrentUserIdOrThrow() {
        return JwtUtil.getCurrentId()
                .orElseThrow(() -> new IllegalStateException("User ID not found in JWT"));
    }
    
    /**
     * Получить текущий ID компании или выбросить исключение
     */
    public static UUID getCurrentCompanyIdOrThrow() {
        return JwtUtil.resolveEffectiveCompanyId()
                .map(UUID::fromString)
                .orElseThrow(() -> new IllegalStateException("Company ID not found in JWT or headers"));
    }
    
    /**
     * Проверить доступ к компании и выбросить исключение при отсутствии доступа
     */
    public static void requireAccessToCompany(UUID companyId) {
        if (!hasAccessToCompany(companyId)) {
            throw new SecurityException("Access denied to company: " + companyId + " for user: " + getCurrentUserIdOrThrow());
        }
    }
    
    /**
     * Проверить административные права и выбросить исключение при их отсутствии
     */
    public static void requireAdminAccess() {
        if (!isAdmin()) {
            throw new SecurityException("Admin access required for user: " + getCurrentUserIdOrThrow());
        }
    }
}
