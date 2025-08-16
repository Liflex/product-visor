package ru.dmitartur.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;
import ru.dmitartur.order.entity.OrderStatusHistory;
import ru.dmitartur.order.mapper.OrderMapper;
import ru.dmitartur.order.repository.OrderRepository;
import ru.dmitartur.order.repository.OrderStatusHistoryRepository;
import ru.dmitartur.common.enums.OrderStatus;
import ru.dmitartur.order.service.product.ProductService;
import ru.dmitartur.order.service.product.StockUpdateService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Процессор для пакетной обработки заказов из внешних источников (Ozon, Yandex и т.д.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderBatchProcessor {
    
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final StockUpdateService stockUpdateService;

    /**
     * Обработать пакет заказов из Ozon API
     */
    public int processOzonBatch(JsonNode payload) {
        log.info("🔄 Starting batch processing of orders from Ozon");
        
        if (!validatePayload(payload)) {
            return 0;
        }
        
        JsonNode result = payload.get("result");
        log.info("📊 Found {} orders in result array", result.size());
        
        int count = 0;
        for (JsonNode orderData : result) {
            try {
                if (processOrder(orderData)) {
                    count++;
                }
            } catch (Exception e) {
                log.error("❌ Error processing order: {}", e.getMessage(), e);
            }
        }
        
        log.info("✅ Batch processing completed: {} orders processed successfully", count);
        return count;
    }

    /**
     * Валидация входящего payload
     */
    private boolean validatePayload(JsonNode payload) {
        if (payload == null) {
            log.error("❌ Payload is null");
            return false;
        }
        
        log.debug("📦 Payload structure: {}", payload.toPrettyString());
        
        if (!payload.has("result")) {
            log.error("❌ Payload does not contain 'result' field. Available fields: {}", 
                     payload.fieldNames().toString());
            return false;
        }
        
        JsonNode result = payload.get("result");
        if (!result.isArray()) {
            log.error("❌ 'result' field is not an array. Type: {}", result.getNodeType());
            return false;
        }
        
        return true;
    }

    /**
     * Обработать один заказ
     */
    private boolean processOrder(JsonNode orderData) {
        String postingNumber = orderData.path("posting_number").asText();
        if (postingNumber == null || postingNumber.isEmpty()) {
            log.warn("❌ Skipping order with empty posting_number: {}", orderData.toPrettyString());
            return false;
        }
        
        log.debug("🔄 Processing order: postingNumber={}", postingNumber);
        
        Order order = findOrCreateOrder(orderData, postingNumber);
        updateOrderStatus(order, orderData, postingNumber);
        updateOrderDates(order, orderData, postingNumber);
        updateOrderItems(order, orderData, postingNumber);
        
        orderRepository.save(order);
        log.debug("✅ Successfully processed order: postingNumber={}", postingNumber);
        
        // Обновляем остатки через Kafka события
        stockUpdateService.updateStockForOrder(order);
        
        return true;
    }

    /**
     * Найти существующий заказ или создать новый
     */
    private Order findOrCreateOrder(JsonNode orderData, String postingNumber) {
        return orderRepository.findByPostingNumber(postingNumber)
                .orElseGet(() -> {
                    log.info("🆕 Creating new order: postingNumber={}", postingNumber);
                    String status = orderData.path("status").asText("unknown");
                    String created = orderData.path("created_at").asText(null);
                    OffsetDateTime createdAt = parseDateTime(created);
                    return orderMapper.toEntity(postingNumber, status, createdAt);
                });
    }

    /**
     * Обновить статус заказа и сохранить историю изменений
     */
    private void updateOrderStatus(Order order, JsonNode orderData, String postingNumber) {
        String newStatusStr = orderData.path("status").asText("unknown");
        OrderStatus newStatus = OrderStatus.fromCode(newStatusStr);
        boolean statusChanged = !newStatus.equals(order.getStatus());
        
        if (statusChanged) {
            log.info("🔄 Status changed for order {}: {} -> {}", postingNumber, order.getStatus(), newStatus);
            order.setStatus(newStatus);
            
            // Сохраняем историю статусов
            OrderStatusHistory history = new OrderStatusHistory(
                order, newStatus, order.getStatus(), "OZON_API"
            );
            orderStatusHistoryRepository.save(history);
            log.info("📝 Status history saved for order {}: {} -> {}", 
                    postingNumber, order.getStatus(), newStatus);
        } else {
            order.setStatus(newStatus);
        }
    }

    /**
     * Обновить даты заказа
     */
    private void updateOrderDates(Order order, JsonNode orderData, String postingNumber) {
        // Устанавливаем дату создания от Ozon
        String created = orderData.path("created_at").asText(null);
        if (created != null) {
            OffsetDateTime ozonCreatedAt = parseDateTime(created);
            if (ozonCreatedAt != null) {
                order.setOzonCreatedAt(ozonCreatedAt);
                log.debug("✅ Set Ozon created date for order {}: {}", postingNumber, order.getOzonCreatedAt());
            }
        }
        
        // Устанавливаем нашу дату обновления
        order.setUpdatedAt(OffsetDateTime.now());
    }

    /**
     * Обновить товары заказа
     */
    private void updateOrderItems(Order order, JsonNode orderData, String postingNumber) {
        // Очищаем существующие товары
        order.getItems().clear();
        
        if (!orderData.has("products")) {
            return;
        }
        
        JsonNode products = orderData.get("products");
        log.debug("📦 Processing {} products for order {}", products.size(), postingNumber);
        
        for (JsonNode product : products) {
            OrderItem orderItem = createOrderItem(order, product, postingNumber);
            order.getItems().add(orderItem);
        }
        
        // Логируем информацию о товарах без привязки к продуктам
        logUnmappedItems(order);
    }

    /**
     * Создать товар заказа
     */
    private OrderItem createOrderItem(Order order, JsonNode product, String postingNumber) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        
        String offerId = product.path("offer_id").asText();
        String sku = product.path("sku").asText();
        
        // Пытаемся найти продукт по article (offer_id)
        Long productId = findProductIdByArticle(offerId);
        
        orderItem.setProductId(productId);
        orderItem.setSku(sku);
        orderItem.setOfferId(offerId);
        orderItem.setName(product.path("name").asText(null));
        orderItem.setQuantity(product.path("quantity").asInt(1));
        
        // Устанавливаем цену
        setOrderItemPrice(orderItem, product, postingNumber);
        
        log.debug("✅ Added product: offerId={}, name={}, quantity={}, productId={}", 
                orderItem.getOfferId(), orderItem.getName(), orderItem.getQuantity(), orderItem.getProductId());
        
        return orderItem;
    }

    /**
     * Найти ID продукта по article
     */
    private Long findProductIdByArticle(String offerId) {
        if (offerId == null || offerId.isEmpty()) {
            return null;
        }
        
        try {
            Optional<ProductService.ProductInfo> productInfo = productService.findProductByArticle(offerId);
            
            if (productInfo.isPresent()) {
                log.debug("✅ Found product by article: article={}, productId={}", offerId, productInfo.get().getId());
                return productInfo.get().getId();
            } else {
                log.debug("⚠️ Product not found by article: article={}, will save with productId=null", offerId);
                return null;
            }
        } catch (Exception e) {
            log.warn("⚠️ Error searching product by article {}: {}, will save with productId=null", 
                    offerId, e.getMessage());
            return null;
        }
    }

    /**
     * Установить цену товара заказа
     */
    private void setOrderItemPrice(OrderItem orderItem, JsonNode product, String postingNumber) {
        if (product.has("price")) {
            try {
                orderItem.setPrice(new BigDecimal(product.get("price").asText()));
            } catch (Exception e) {
                log.warn("❌ Failed to parse price for product in order {}: {}", postingNumber, e.getMessage());
            }
        }
    }

    /**
     * Логировать товары без привязки к продуктам
     */
    private void logUnmappedItems(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getOfferId() != null && item.getProductId() == null) {
                log.info("📊 Order item without product mapping: article={}, name={}, quantity={}", 
                        item.getOfferId(), item.getName(), item.getQuantity());
            }
        }
    }

    /**
     * Парсинг даты с обработкой ошибок
     */
    private OffsetDateTime parseDateTime(String dateString) {
        if (dateString == null) {
            return null;
        }
        
        try {
            return OffsetDateTime.parse(dateString);
        } catch (Exception e) {
            log.warn("❌ Failed to parse date: {}", e.getMessage());
            return null;
        }
    }
}
