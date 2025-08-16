package ru.dmitartur.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;
    private Long productId;
    private String offerId;
    private String name;
    private Integer quantity;
    private BigDecimal price;
}
