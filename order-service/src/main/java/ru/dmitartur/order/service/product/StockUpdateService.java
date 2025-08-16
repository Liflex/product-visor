package ru.dmitartur.order.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;
import ru.dmitartur.order.kafka.OrderEventProducer;

import java.util.List;

/**
 * Сервис для обновления остатков товаров через Kafka события
 * Отдельная ответственность - только обновление остатков
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockUpdateService {
    
    private final OrderEventProducer orderEventProducer;
    
    /**
     * Обновить остатки для заказа через Kafka события
     * Отправляет событие только если есть найденные продукты
     */
    public void updateStockForOrder(Order order) {
        List<OrderItem> itemsWithProducts = order.getItems().stream()
            .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getOfferId() != null)
            .toList();
        
        if (!itemsWithProducts.isEmpty()) {
            log.info("📤 Sending stock update event for order: postingNumber={}, itemsWithProducts={}", 
                    order.getPostingNumber(), itemsWithProducts.size());
            orderEventProducer.sendOrderCreatedEvent(createOrderData(order));
        } else {
            log.info("⏭️ Skipping stock update for order without found products: postingNumber={}", 
                    order.getPostingNumber());
        }
    }
    
    /**
     * Обновить остатки при отмене заказа через Kafka события
     */
    public void updateStockForCancelledOrder(Order order) {
        List<OrderItem> itemsWithProducts = order.getItems().stream()
            .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getOfferId() != null)
            .toList();
        
        if (!itemsWithProducts.isEmpty()) {
            log.info("📤 Sending stock update event for cancelled order: postingNumber={}, itemsWithProducts={}", 
                    order.getPostingNumber(), itemsWithProducts.size());
            orderEventProducer.sendOrderCancelledEvent(createOrderData(order));
        } else {
            log.info("⏭️ Skipping stock update for cancelled order without found products: postingNumber={}", 
                    order.getPostingNumber());
        }
    }
    
    /**
     * Создать данные заказа для Kafka события
     */
    private com.fasterxml.jackson.databind.JsonNode createOrderData(Order order) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.node.ObjectNode orderData = mapper.createObjectNode();
            
            orderData.put("posting_number", order.getPostingNumber());
            orderData.put("source", order.getSource());
            orderData.put("status", order.getStatus().name());
            
            com.fasterxml.jackson.databind.node.ArrayNode products = mapper.createArrayNode();
            for (OrderItem item : order.getItems()) {
                if (item.getProductId() != null) { // Только найденные продукты
                    com.fasterxml.jackson.databind.node.ObjectNode product = mapper.createObjectNode();
                    product.put("offer_id", item.getOfferId()); // offer_id = article
                    product.put("name", item.getName());
                    product.put("quantity", item.getQuantity());
                    product.put("sku", item.getSku());
                    products.add(product);
                }
            }
            orderData.set("products", products);
            
            return orderData;
            
        } catch (Exception e) {
            log.error("❌ Error creating order data for Kafka: postingNumber={}, error={}", 
                    order.getPostingNumber(), e.getMessage());
            throw new RuntimeException("Failed to create order data for Kafka", e);
        }
    }
}
