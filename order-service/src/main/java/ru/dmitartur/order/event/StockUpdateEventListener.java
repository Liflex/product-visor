package ru.dmitartur.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.grpc.client.ProductGrpcClient;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.entity.OrderItem;
import ru.dmitartur.order.kafka.OrderEventProducer;

import java.util.List;

/**
 * Обработчик событий заказов для обновления остатков товаров
 * 
 * Обрабатывает:
 * - OrderCreatedEvent - уменьшает остатки при создании заказа
 * - OrderCancelledEvent - возвращает остатки при отмене заказа
 * 
 * Также отправляет события в Kafka для других сервисов
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockUpdateEventListener {
    
    private final ProductGrpcClient productGrpcClient;
    private final OrderEventProducer orderEventProducer;
    
    /**
     * Обработка события создания заказа
     * Уменьшает остатки товаров и отправляет событие в Kafka
     */
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        log.info("📦 Processing stock update for created order: postingNumber={}", order.getPostingNumber());
        
        try {
            // Обновляем остатки через gRPC
            updateStockForOrder(order, false);
            
            // Отправляем событие в Kafka
            sendKafkaEvent(order, "ORDER_CREATED");
            
            log.info("✅ Stock update completed for created order: postingNumber={}", order.getPostingNumber());
            
        } catch (Exception e) {
            log.error("❌ Error processing stock update for created order: postingNumber={}, error={}", 
                    order.getPostingNumber(), e.getMessage());
        }
    }
    
    /**
     * Обработка события отмены заказа
     * Возвращает остатки товаров и отправляет событие в Kafka
     */
    @EventListener
    @Async("orderEventExecutor")
    public void handleOrderCancelled(OrderCancelledEvent event) {
        Order order = event.getOrder();
        log.info("📦 Processing stock update for cancelled order: postingNumber={}", order.getPostingNumber());
        
        try {
            // Возвращаем остатки через gRPC
            updateStockForOrder(order, true);
            
            // Отправляем событие в Kafka
            sendKafkaEvent(order, "ORDER_CANCELLED");
            
            log.info("✅ Stock update completed for cancelled order: postingNumber={}", order.getPostingNumber());
            
        } catch (Exception e) {
            log.error("❌ Error processing stock update for cancelled order: postingNumber={}, error={}", 
                    order.getPostingNumber(), e.getMessage());
        }
    }
    
    /**
     * Обновить остатки для заказа
     * @param order заказ
     * @param isCancellation true если это отмена заказа (возврат остатков), false если создание (уменьшение остатков)
     */
    private void updateStockForOrder(Order order, boolean isCancellation) {
        List<OrderItem> itemsWithProducts = order.getItems().stream()
            .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getOfferId() != null)
            .toList();
        
        if (itemsWithProducts.isEmpty()) {
            log.info("⏭️ No products found for stock update: postingNumber={}", order.getPostingNumber());
            return;
        }
        
        int updatedCount = 0;
        for (OrderItem item : itemsWithProducts) {
            try {
                // Для отмены заказа возвращаем остатки (положительное значение)
                // Для создания заказа уменьшаем остатки (отрицательное значение)
                int quantityChange = isCancellation ? item.getQuantity() : -item.getQuantity();
                
                boolean success = productGrpcClient.updateProductStockByArticle(item.getOfferId(), quantityChange);
                
                if (success) {
                    updatedCount++;
                    log.debug("✅ Stock updated for product: offerId={}, change={}, isCancellation={}", 
                            item.getOfferId(), quantityChange, isCancellation);
                } else {
                    log.warn("⚠️ Failed to update stock for product: offerId={}, change={}, isCancellation={}", 
                            item.getOfferId(), quantityChange, isCancellation);
                }
                
            } catch (Exception e) {
                log.error("❌ Error updating stock for product: offerId={}, error={}", 
                        item.getOfferId(), e.getMessage());
            }
        }
        
        log.info("📊 Stock update summary: postingNumber={}, totalItems={}, updatedItems={}, isCancellation={}", 
                order.getPostingNumber(), itemsWithProducts.size(), updatedCount, isCancellation);
    }
    
    /**
     * Отправить событие в Kafka
     */
    private void sendKafkaEvent(Order order, String eventType) {
        try {
            if ("ORDER_CREATED".equals(eventType)) {
                orderEventProducer.sendOrderCreatedEvent(createOrderData(order));
            } else if ("ORDER_CANCELLED".equals(eventType)) {
                orderEventProducer.sendOrderCancelledEvent(createOrderData(order));
            }
        } catch (Exception e) {
            log.error("❌ Error sending Kafka event: postingNumber={}, eventType={}, error={}", 
                    order.getPostingNumber(), eventType, e.getMessage());
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
