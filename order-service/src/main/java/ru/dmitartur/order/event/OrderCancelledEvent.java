package ru.dmitartur.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.dmitartur.order.entity.Order;

/**
 * Событие отмены заказа
 * Публикуется при изменении статуса заказа на CANCELLED
 */
@Getter
public class OrderCancelledEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderCancelledEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}
