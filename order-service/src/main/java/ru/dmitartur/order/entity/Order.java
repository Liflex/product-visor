package ru.dmitartur.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import ru.dmitartur.common.enums.Market;
import ru.dmitartur.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "orders", schema = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "posting_number", unique = true, nullable = false, length = 100)
    private String postingNumber;

    @Column(name = "source", length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "market", length = 50)
    private ru.dmitartur.common.enums.Market market;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private OrderStatus status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "ozon_created_at")
    private OffsetDateTime ozonCreatedAt; // Дата создания заказа в Ozon

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    // FBS поля - даты
    @Column(name = "in_process_at")
    private OffsetDateTime inProcessAt; // Дата начала обработки

    @Column(name = "shipment_date")
    private OffsetDateTime shipmentDate; // Дата отправки

    @Column(name = "delivering_date")
    private OffsetDateTime deliveringDate; // Дата начала доставки

    // FBS поля - отмена
    @Column(name = "cancel_reason")
    private String cancelReason; // Причина отмены

    @Column(name = "cancel_reason_id")
    private Long cancelReasonId; // ID причины отмены

    @Column(name = "cancellation_type")
    private String cancellationType; // Тип отмены

    // FBS поля - доставка
    @Column(name = "tracking_number")
    private String trackingNumber; // Номер отслеживания

    @Column(name = "delivery_method_name")
    private String deliveryMethodName; // Название способа доставки

    @Column(name = "substatus")
    private String substatus; // Подстатус заказа

    @Column(name = "is_express")
    private Boolean isExpress; // Экспресс доставка

    // Вычисляемые методы
    public Integer getDaysInTransit() {
        if (shipmentDate != null && deliveringDate != null) {
            return (int) java.time.Duration.between(shipmentDate, deliveringDate).toDays();
        }
        return null;
    }

    public Integer getDaysInProcessing() {
        if (createdAt != null && shipmentDate != null) {
            return (int) java.time.Duration.between(createdAt, shipmentDate).toDays();
        }
        return null;
    }
}


