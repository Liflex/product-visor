package ru.dmitartur.ozon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа со статусом подключения к Ozon API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatusResponse {
    
    /**
     * Существуют ли учетные данные
     */
    private boolean exists;
    

    
    /**
     * Результат тестирования подключения к API
     */
    private boolean apiConnectionTest;
    
    /**
     * Ошибка подключения к API (если есть)
     */
    private String connectionError;
    
    /**
     * Статус синхронизации
     */
    private String syncStatus;
    
    /**
     * Время последней синхронизации
     */
    private String lastSyncAt;
    
    /**
     * Последняя ошибка
     */
    private String errorMessage;
}
