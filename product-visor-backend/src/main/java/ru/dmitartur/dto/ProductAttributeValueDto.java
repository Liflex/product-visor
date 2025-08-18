package ru.dmitartur.dto;

import lombok.Data;

@Data
public class ProductAttributeValueDto {
    private Long id;
    private Long productId;
    private String value;
    private AttributeDto attribute;
}
