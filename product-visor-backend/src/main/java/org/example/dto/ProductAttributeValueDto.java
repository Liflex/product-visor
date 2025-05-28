package org.example.dto;

import lombok.Data;

@Data
public class ProductAttributeValueDto {
    private Long id;
    private String value;
    private AttributeDto attribute;
    private Long productId;
}
