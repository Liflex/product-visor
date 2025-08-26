package ru.dmitartur.library.marketplace.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;

/**
 * Базовый интерфейс для сервисов маркетплейсов
 * Определяет общие методы, которые должны быть реализованы для каждого маркетплейса
 */
public interface BaseMarketplaceService {
    
    /**
     * Получить список заказов FBO
     */
    JsonNode fboPostingList(JsonNode request);
    
    /**
     * Загрузить исторические данные заказов
     */
    Integer backfillAllOrders(DateRangeDto range, int pageSize);
    
    /**
     * Получить список заказов FBS
     */
    JsonNode fbsPostingList(JsonNode request);
    
    /**
     * Получить информацию о заказе FBS
     */
    JsonNode fbsPostingGet(JsonNode request);
    
    /**
     * Получить название маркетплейса
     */
    String getMarketplaceName();
    
    /**
     * Проверить подключение к API маркетплейса
     */
    boolean testConnection();
}

