package ru.dmitartur.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.grpc.client.ProductGrpcClient;
import ru.dmitartur.order.entity.Order;
import ru.dmitartur.order.kafka.OrderEventProducer;
import ru.dmitartur.common.events.EventType;

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
public class OrderEventListener {
    
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
            // Отправляем событие в Kafka
            orderEventProducer.sendOrderCreatedEvent(order);
            
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
            // Отправляем событие в Kafka
            orderEventProducer.sendOrderCancelledEvent(order);
            
            log.info("✅ Stock update completed for cancelled order: postingNumber={}", order.getPostingNumber());
            
        } catch (Exception e) {
            log.error("❌ Error processing stock update for cancelled order: postingNumber={}, error={}", 
                    order.getPostingNumber(), e.getMessage());
        }
    }
}
