package ru.dmitartur.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dmitartur.order.entity.OrderStatusHistory;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    
    /**
     * Найти историю статусов для заказа, отсортированную по дате изменения
     */
    List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
    
    /**
     * Найти последний статус для заказа
     */
    OrderStatusHistory findFirstByOrderIdOrderByChangedAtDesc(Long orderId);
}
