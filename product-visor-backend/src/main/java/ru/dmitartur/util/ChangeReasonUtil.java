package ru.dmitartur.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Утилита для определения причины изменения и системы-источника
 */
@Slf4j
@Component
public class ChangeReasonUtil {
    
    /**
     * Определяет причину изменения на основе контекста
     */
    public static String determineChangeReason(String sourceSystem, String sourceId) {
        if (sourceSystem == null) {
            return "UNKNOWN";
        }
        
        switch (sourceSystem.toUpperCase()) {
            case "KAFKA":
                if (sourceId != null && sourceId.contains("ORDER_")) {
                    return sourceId; // ORDER_CREATED, ORDER_CANCELLED, etc.
                }
                return "SYNC_UPDATE";
                
            case "REST_API":
                // Определяем по sourceId или контексту
                if (sourceId != null && sourceId.contains("ProductStockController")) {
                    return "MANUAL_UPDATE";
                }
                return "API_UPDATE";
                
            case "STOCK_SERVICE":
                return "STOCK_UPDATE";
                
            case "SYNC_SERVICE":
                return "SYNC_UPDATE";
                
            default:
                return "UNKNOWN";
        }
    }
    
    /**
     * Определяет систему-источник на основе контекста
     */
    public static String determineSourceSystem(String changeReason, String sourceId) {
        if (changeReason == null) {
            return "UNKNOWN";
        }
        
        if (changeReason.startsWith("ORDER_")) {
            return "KAFKA";
        } else if (changeReason.equals("MANUAL_UPDATE")) {
            return "REST_API";
        } else if (changeReason.equals("STOCK_UPDATE")) {
            return "STOCK_SERVICE";
        } else if (changeReason.equals("SYNC_UPDATE")) {
            return "SYNC_SERVICE";
        }
        
        return "UNKNOWN";
    }
    
    /**
     * Определяет, является ли изменение ручным (пользовательским)
     */
    public static boolean isManualChange(String changeReason) {
        return "MANUAL_UPDATE".equals(changeReason) || "API_UPDATE".equals(changeReason);
    }
    
    /**
     * Определяет, является ли изменение автоматическим (системным)
     */
    public static boolean isAutomaticChange(String changeReason) {
        return changeReason.startsWith("ORDER_") || 
               "STOCK_UPDATE".equals(changeReason) || 
               "SYNC_UPDATE".equals(changeReason);
    }
}
