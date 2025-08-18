package ru.dmitartur.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ru.dmitartur.common.enums.OrderStatus;

import java.time.LocalDateTime;

/**
 * История статусов заказа
 */
@Entity
@Table(name = "order_status_history", schema = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private OrderStatus previousStatus;
    
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "source")
    private String source; // OZON_API, MANUAL, etc.
    
    public OrderStatusHistory(Order order, OrderStatus status, OrderStatus previousStatus, String source) {
        this.order = order;
        this.status = status;
        this.previousStatus = previousStatus;
        this.changedAt = LocalDateTime.now();
        this.source = source;
    }
}
