package org.example.dto;

import lombok.Data;
import org.example.entity.Category;
import org.example.entity.ProductAttributeValue;

import java.util.List;
import java.util.Set;


@Data
public class ProductDto {
    private Long id;
    private String name;
    private String imageUrl;
    private CategoryDto category;
    private List<ProductAttributeValueDto> productAttributeValues;

}
