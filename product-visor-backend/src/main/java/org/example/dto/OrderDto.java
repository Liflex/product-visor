package org.example.dto;

import lombok.Data;
import org.example.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {
    private Long id;
    private ProductDto product;
    private MarketDto market;
    private String orderBarcode;
    private BigDecimal price;
    private LocalDateTime orderDate;
    private OrderStatus status;
} 