package ru.dmitartur.library.marketplace.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import ru.dmitartur.common.dto.OrderDto;

/**
 * Базовый интерфейс для мапперов заказов маркетплейсов
 * Определяет общие методы для преобразования данных заказов
 */
public interface BaseMarketplaceOrderMapper {
    
    /**
     * Преобразовать заказ из API маркетплейса в OrderDto
     */
    OrderDto mapOrderToDto(JsonNode marketplaceOrder);
    
    /**
     * Преобразовать список заказов из API маркетплейса в список OrderDto
     */
    java.util.List<OrderDto> mapOrdersToDto(JsonNode marketplaceOrders);
    
    /**
     * Преобразовать товар из заказа в OrderItemDto
     */
    ru.dmitartur.common.dto.OrderItemDto mapProductToItem(JsonNode product);
    
    /**
     * Получить название маркетплейса
     */
    String getMarketplaceName();
    
    /**
     * Получить источник заказов (например, "OZON_FBO", "YANDEX_FBS")
     */
    String getOrderSource();
    
    /**
     * Проверить, что JsonNode содержит валидный заказ
     */
    boolean isValidOrder(JsonNode order);
    
    /**
     * Извлечь номер заказа из JsonNode
     */
    String extractOrderNumber(JsonNode order);
    
    /**
     * Извлечь статус заказа из JsonNode
     */
    String extractOrderStatus(JsonNode order);
    
    /**
     * Извлечь дату создания заказа из JsonNode
     */
    String extractOrderCreatedAt(JsonNode order);
    
    /**
     * Извлечь товары из заказа
     */
    JsonNode extractProducts(JsonNode order);
}

