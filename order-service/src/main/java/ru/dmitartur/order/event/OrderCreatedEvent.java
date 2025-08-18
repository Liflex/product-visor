package ru.dmitartur.order.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.dmitartur.order.entity.Order;

/**
 * Событие создания заказа
 * Публикуется после успешного сохранения заказа в базе данных
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    
    private final Order order;
    
    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
    }
}

