package ru.dmitartur.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.dmitartur.common.security.Role;

import java.time.LocalDateTime;

/**
 * Универсальный DTO для пользователя, используемый во всех сервисах
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private String locale;
    private String timezone;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Получить полное имя пользователя
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return email;
        }
    }
    
    /**
     * Проверить, является ли пользователь администратором
     */
    public boolean isAdmin() {
        return Role.ADMIN.equals(role);
    }
    
    /**
     * Проверить, является ли пользователь сотрудником
     */
    public boolean isEmployee() {
        return Role.EMPLOYEE.equals(role);
    }
    
    /**
     * Проверить, является ли пользователь частным лицом
     */
    public boolean isPrivatePerson() {
        return Role.PRIVATE_PERSON.equals(role);
    }
}
