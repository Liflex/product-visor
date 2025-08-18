package ru.dmitartur.ozon.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.dto.OrderItemDto;
import ru.dmitartur.common.enums.Market;
import ru.dmitartur.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Маппер для преобразования данных заказов из Ozon API в Order DTO
 */
@Slf4j
@Component
public class OzonOrderMapper {

    /**
     * Преобразовать JsonNode заказа из Ozon в OrderDto
     */
    public OrderDto mapOzonOrderToDto(JsonNode ozonOrder) {
        try {
            OrderDto orderDto = new OrderDto();
            
            // Основные поля
            orderDto.setPostingNumber(ozonOrder.path("posting_number").asText());
            orderDto.setSource("OZON_FBO");
            orderDto.setMarket(Market.OZON);
            
            // Статус
            String statusCode = ozonOrder.path("status").asText("unknown");
            orderDto.setStatus(OrderStatus.fromCode(statusCode));
            
            // Даты
            orderDto.setCreatedAt(parseDateTime(ozonOrder.path("created_at").asText()));
            orderDto.setUpdatedAt(LocalDateTime.now());
            
            // Товары
            List<OrderItemDto> items = new ArrayList<>();
            if (ozonOrder.has("products")) {
                JsonNode products = ozonOrder.get("products");
                BigDecimal totalPrice = BigDecimal.ZERO;
                
                for (JsonNode product : products) {
                    OrderItemDto item = mapProductToItem(product);
                    items.add(item);
                    
                    // Суммируем цену
                    if (item.getPrice() != null) {
                        totalPrice = totalPrice.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    }
                }
                
                orderDto.setTotalPrice(totalPrice);
            }
            
            orderDto.setItems(items);
            
            log.debug("✅ Mapped Ozon order to DTO: postingNumber={}, status={}, itemsCount={}", 
                     orderDto.getPostingNumber(), orderDto.getStatus(), items.size());
            
            return orderDto;
            
        } catch (Exception e) {
            log.error("❌ Error mapping Ozon order to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map Ozon order", e);
        }
    }

    /**
     * Преобразовать JsonNode товара в OrderItemDto
     */
    private OrderItemDto mapProductToItem(JsonNode product) {
        OrderItemDto item = new OrderItemDto();
        
        // SKU для поиска товара в нашей системе
        String sku = product.path("sku").asText();
        item.setSku(sku);
        
        // offer_id = article в нашей системе
        String offerId = product.path("offer_id").asText();
        item.setOfferId(offerId);
        
        // productId будет установлен позже при поиске товара в системе по article
        item.setProductId(null); // Пока не знаем ID нашего товара
        
        // Остальные поля
        item.setName(product.path("name").asText());
        item.setQuantity(product.path("quantity").asInt(1));
        
        // Цена
        if (product.has("price")) {
            try {
                item.setPrice(new BigDecimal(product.get("price").asText()));
            } catch (NumberFormatException e) {
                log.warn("❌ Failed to parse price for product {}: {}", offerId, e.getMessage());
                item.setPrice(BigDecimal.ZERO);
            }
        }
        
        return item;
    }

    /**
     * Преобразовать массив заказов из Ozon в список OrderDto
     */
    public List<OrderDto> mapOzonOrdersToDtoList(JsonNode resultArray) {
        List<OrderDto> orders = new ArrayList<>();
        
        if (resultArray != null && resultArray.isArray()) {
            for (JsonNode orderNode : resultArray) {
                try {
                    OrderDto orderDto = mapOzonOrderToDto(orderNode);
                    orders.add(orderDto);
                } catch (Exception e) {
                    log.error("❌ Error mapping Ozon order: {}", e.getMessage());
                    // Продолжаем обработку других заказов
                }
            }
        }
        
        log.info("✅ Mapped {} Ozon orders to DTOs", orders.size());
        return orders;
    }

    /**
     * Преобразовать массив FBS заказов из Ozon в список OrderDto
     */
    public List<OrderDto> mapOzonFbsOrdersToDtoList(JsonNode resultArray) {
        List<OrderDto> orders = new ArrayList<>();
        
        if (resultArray != null && resultArray.isArray()) {
            for (JsonNode orderNode : resultArray) {
                try {
                    OrderDto orderDto = mapOzonFbsOrderToDto(orderNode);
                    orders.add(orderDto);
                } catch (Exception e) {
                    log.error("❌ Error mapping Ozon FBS order: {}", e.getMessage());
                    // Продолжаем обработку других заказов
                }
            }
        }
        
        log.info("✅ Mapped {} Ozon FBS orders to DTOs", orders.size());
        return orders;
    }

    /**
     * Преобразовать JsonNode FBS заказа из Ozon в OrderDto
     */
    public OrderDto mapOzonFbsOrderToDto(JsonNode ozonOrder) {
        try {
            OrderDto orderDto = new OrderDto();
            
            // Основные поля
            orderDto.setPostingNumber(ozonOrder.path("posting_number").asText());
            orderDto.setSource("OZON_FBS");
            orderDto.setMarket(Market.OZON);
            
            // Статус FBS
            String statusCode = ozonOrder.path("status").asText("unknown");
            orderDto.setStatus(OrderStatus.fromCode(statusCode));
            
            // Даты - в FBS нет created_at, используем in_process_at как дату создания
            orderDto.setCreatedAt(parseDateTime(ozonOrder.path("in_process_at").asText()));
            orderDto.setUpdatedAt(LocalDateTime.now());
            
            // FBS даты - используем ozonCreatedAt для хранения даты создания от Ozon
            orderDto.setOzonCreatedAt(parseDateTime(ozonOrder.path("in_process_at").asText()));
            orderDto.setInProcessAt(parseDateTime(ozonOrder.path("in_process_at").asText()));
            orderDto.setShipmentDate(parseDateTime(ozonOrder.path("shipment_date").asText()));
            orderDto.setDeliveringDate(parseDateTime(ozonOrder.path("delivering_date").asText()));
            
            // FBS отмена
            if (ozonOrder.has("cancellation")) {
                JsonNode cancellation = ozonOrder.get("cancellation");
                if(!cancellation.get("cancel_reason_id").asText().equals("0")) {
                    orderDto.setCancelReason(cancellation.path("cancel_reason").asText());
                    orderDto.setCancelReasonId(cancellation.path("cancel_reason_id").asLong(0));
                    orderDto.setCancellationType(cancellation.path("cancellation_type").asText());
                }
            }
            
            // FBS доставка
            if (ozonOrder.has("delivery_method")) {
                JsonNode delivery = ozonOrder.get("delivery_method");
                orderDto.setDeliveryMethodName(delivery.path("name").asText());
            }
            orderDto.setTrackingNumber(ozonOrder.path("tracking_number").asText());
            orderDto.setSubstatus(ozonOrder.path("substatus").asText());
            orderDto.setIsExpress(ozonOrder.path("is_express").asBoolean(false));
            
            // Вычисляемые поля
            if (orderDto.getShipmentDate() != null && orderDto.getDeliveringDate() != null) {
                orderDto.setDaysInTransit((int) java.time.Duration.between(orderDto.getShipmentDate(), orderDto.getDeliveringDate()).toDays());
            }
            if (orderDto.getCreatedAt() != null && orderDto.getShipmentDate() != null) {
                orderDto.setDaysInProcessing((int) java.time.Duration.between(orderDto.getCreatedAt(), orderDto.getShipmentDate()).toDays());
            }
            
            // Товары FBS
            List<OrderItemDto> items = new ArrayList<>();
            if (ozonOrder.has("products")) {
                JsonNode products = ozonOrder.get("products");
                BigDecimal totalPrice = BigDecimal.ZERO;
                
                for (JsonNode product : products) {
                    OrderItemDto item = mapFbsProductToItem(product);
                    items.add(item);
                    
                    // Суммируем цену
                    if (item.getPrice() != null) {
                        totalPrice = totalPrice.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                    }
                }
                
                orderDto.setTotalPrice(totalPrice);
            }
            
            orderDto.setItems(items);
            
            log.debug("✅ Mapped Ozon FBS order to DTO: postingNumber={}, status={}, itemsCount={}", 
                     orderDto.getPostingNumber(), orderDto.getStatus(), items.size());
            
            return orderDto;
            
        } catch (Exception e) {
            log.error("❌ Error mapping Ozon FBS order to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map Ozon FBS order", e);
        }
    }

    /**
     * Преобразовать JsonNode товара FBS в OrderItemDto
     */
    private OrderItemDto mapFbsProductToItem(JsonNode product) {
        OrderItemDto item = new OrderItemDto();
        
        // SKU для поиска товара в нашей системе
        String sku = product.path("sku").asText();
        item.setSku(sku);
        
        // offer_id = article в нашей системе
        String offerId = product.path("offer_id").asText();
        item.setOfferId(offerId);
        
        // productId будет установлен позже при поиске товара в системе по article
        item.setProductId(null); // Пока не знаем ID нашего товара
        
        // Остальные поля
        item.setName(product.path("name").asText());
        item.setQuantity(product.path("quantity").asInt(1));
        
        // Цена
        if (product.has("price")) {
            try {
                item.setPrice(new BigDecimal(product.get("price").asText()));
            } catch (NumberFormatException e) {
                log.warn("❌ Failed to parse price for FBS product {}: {}", offerId, e.getMessage());
                item.setPrice(BigDecimal.ZERO);
            }
        }
        
        return item;
    }

    /**
     * Парсинг даты из строки
     */
    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            log.warn("❌ Failed to parse date: {}", dateString);
            return null;
        }
    }
}
