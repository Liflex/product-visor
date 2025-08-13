package ru.dmitartur.dto;

import lombok.Data;

@Data
public class MarketDto {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private byte[] image;
} 