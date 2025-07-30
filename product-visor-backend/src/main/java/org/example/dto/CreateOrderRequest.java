package org.example.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    private ProductDto product;
    private MarketDto market;
    private String orderBarcode;
    private BigDecimal price;
} 