package ru.dmitartur.library.marketplace.integration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Базовый интерфейс для API маркетплейсов
 * Определяет общие методы для работы с внешними API маркетплейсов
 */
public interface BaseMarketplaceApi {
    
    /**
     * Получить список складов
     */
    JsonNode listWarehouses();

    /**
     * Получить список заказов FBO
     */
    JsonNode getFboOrders(JsonNode request);

    /**
     * Получить список заказов FBS
     */
    JsonNode getFbsOrders(JsonNode request);

    /**
     * Получить информацию о заказе FBS
     */
    JsonNode getFbsOrder(JsonNode request);
    
    /**
     * Проверить подключение к API
     */
    boolean testConnection();
    
    /**
     * Получить название маркетплейса
     */
    String getMarketplaceName();
}

