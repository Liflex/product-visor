package ru.dmitartur.dto;

import lombok.Data;

@Data
public class ProductMarketDto {
    private Long id;
    private Integer quantity;
    private Double price;
    private MarketDto market;
    private ProductDto product;
}