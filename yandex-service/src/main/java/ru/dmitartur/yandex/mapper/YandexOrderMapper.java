package ru.dmitartur.yandex.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.dto.OrderItemDto;
import ru.dmitartur.library.marketplace.mapper.BaseMarketplaceOrderMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class YandexOrderMapper implements BaseMarketplaceOrderMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public OrderDto mapOrderToDto(JsonNode marketplaceOrder) {
        try {
            OrderDto orderDto = new OrderDto();
            
            // Основная информация о заказе
            orderDto.setPostingNumber(extractOrderNumber(marketplaceOrder));
            orderDto.setSource(getOrderSource());
            orderDto.setMarket(ru.dmitartur.common.dto.Market.YANDEX);
            orderDto.setStatus(extractOrderStatus(marketplaceOrder));
            orderDto.setCreatedAt(parseDateTime(extractOrderCreatedAt(marketplaceOrder)));
            orderDto.setUpdatedAt(LocalDateTime.now());
            
            // Информация о покупателе
            if (marketplaceOrder.has("buyer")) {
                JsonNode buyer = marketplaceOrder.get("buyer");
                orderDto.setCustomerName(buyer.path("firstName").asText() + " " + buyer.path("lastName").asText());
                orderDto.setCustomerPhone(buyer.path("phone").asText());
            }
            
            // Адрес доставки
            if (marketplaceOrder.has("delivery")) {
                JsonNode delivery = marketplaceOrder.get("delivery");
                if (delivery.has("address")) {
                    JsonNode address = delivery.get("address");
                    orderDto.setAddress(address.path("fullAddress").asText());
                }
            }
            
            // Общая стоимость
            if (marketplaceOrder.has("totalPrice")) {
                JsonNode totalPrice = marketplaceOrder.get("totalPrice");
                BigDecimal price = new BigDecimal(totalPrice.path("value").asText("0"));
                orderDto.setTotalPrice(price);
            }
            
            // Товары в заказе
            JsonNode items = extractProducts(marketplaceOrder);
            if (items != null && items.isArray()) {
                List<OrderItemDto> orderItems = new ArrayList<>();
                for (JsonNode item : items) {
                    orderItems.add(mapProductToItem(item));
                }
                orderDto.setItems(orderItems);
            }
            
            return orderDto;
            
        } catch (Exception e) {
            log.error("❌ Error mapping Yandex order to DTO: {}", e.getMessage());
            throw new RuntimeException("Error mapping Yandex order to DTO", e);
        }
    }

    @Override
    public List<OrderDto> mapOrdersToDto(JsonNode marketplaceOrders) {
        List<OrderDto> orderDtos = new ArrayList<>();
        
        if (marketplaceOrders != null && marketplaceOrders.isArray()) {
            for (JsonNode order : marketplaceOrders) {
                if (isValidOrder(order)) {
                    try {
                        orderDtos.add(mapOrderToDto(order));
                    } catch (Exception e) {
                        log.warn("⚠️ Skipping invalid order: {}", e.getMessage());
                    }
                }
            }
        }
        
        return orderDtos;
    }

    @Override
    public OrderItemDto mapProductToItem(JsonNode product) {
        OrderItemDto itemDto = new OrderItemDto();
        
        itemDto.setOfferId(product.path("offerId").asText());
        itemDto.setName(product.path("name").asText());
        itemDto.setQuantity(product.path("quantity").asInt(1));
        
        if (product.has("price")) {
            JsonNode price = product.get("price");
            BigDecimal itemPrice = new BigDecimal(price.path("value").asText("0"));
            itemDto.setPrice(itemPrice);
        }
        
        itemDto.setSku(product.path("sku").asText());
        
        return itemDto;
    }

    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }

    @Override
    public String getOrderSource() {
        return "YANDEX_ORDERS";
    }

    @Override
    public boolean isValidOrder(JsonNode order) {
        return order != null && 
               order.has("id") && 
               order.has("status") &&
               !order.path("id").asText().isEmpty();
    }

    @Override
    public String extractOrderNumber(JsonNode order) {
        return order.path("id").asText();
    }

    @Override
    public String extractOrderStatus(JsonNode order) {
        String status = order.path("status").asText();
        // Маппинг статусов Yandex на общие статусы
        switch (status.toUpperCase()) {
            case "PROCESSING":
                return "PROCESSING";
            case "DELIVERED":
                return "DELIVERED";
            case "CANCELLED":
                return "CANCELLED";
            case "READY_TO_SHIP":
                return "READY_TO_SHIP";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    public String extractOrderCreatedAt(JsonNode order) {
        return order.path("createdAt").asText();
    }

    @Override
    public JsonNode extractProducts(JsonNode order) {
        return order.path("items");
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return LocalDateTime.now();
        }
        
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("⚠️ Error parsing date: {}, using current time", dateTimeStr);
            return LocalDateTime.now();
        }
    }
}
