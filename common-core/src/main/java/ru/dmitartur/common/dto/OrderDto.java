package ru.dmitartur.common.dto;

import lombok.Data;
import ru.dmitartur.common.enums.Market;
import ru.dmitartur.common.enums.OrderStatus;
import ru.dmitartur.common.events.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Общий DTO для заказа, используемый для gRPC коммуникации между сервисами
 */
@Data
public class OrderDto {
    private Long id;
    private String postingNumber;
    private String source;
    private Market market;
    private UUID companyId;
    private UUID ownerUserId; // ID владельца заказа
    private String warehouseId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime ozonCreatedAt; // Дата создания заказа в Ozon
    private String customerName;
    private String customerPhone;
    private String address;
    private BigDecimal totalPrice;
    private List<OrderItemDto> items;
    private EventType eventType;

    // FBS поля - даты
    private LocalDateTime inProcessAt;
    private LocalDateTime shipmentDate;
    private LocalDateTime deliveringDate;

    // FBS поля - отмена
    private String cancelReason;
    private Long cancelReasonId;
    private String cancellationType;

    // FBS поля - доставка
    private String trackingNumber;
    private String deliveryMethodName;
    private String substatus;
    private Boolean isExpress;

    // Вычисляемые поля
    private Integer daysInTransit;
    private Integer daysInProcessing;
}
