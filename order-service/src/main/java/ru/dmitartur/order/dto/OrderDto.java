package ru.dmitartur.order.dto;

import lombok.Data;
import ru.dmitartur.common.enums.Market;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String postingNumber;
    private String source;
    private Market market;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String customerName;
    private String customerPhone;
    private String address;
    private BigDecimal totalPrice;
    private List<OrderItemDto> items;
    
    // FBS поля - даты
    private OffsetDateTime inProcessAt;
    private OffsetDateTime shipmentDate;
    private OffsetDateTime deliveringDate;
    
    // FBS поля - отмена
    private String cancelReason;
    private Long cancelReasonId;
    private String cancellationType;
    
    // FBS поля - доставка
    private String trackingNumber;
    private String deliveryMethodName;
    private String substatus;
    private Boolean isExpress;
}
