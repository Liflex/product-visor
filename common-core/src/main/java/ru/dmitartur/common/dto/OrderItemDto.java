package ru.dmitartur.common.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO для товара в заказе
 */
@Data
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String offerId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
    private String sku;
}
