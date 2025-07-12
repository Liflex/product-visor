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
    private byte[] image; // Add image field for database storage
    private String barcode;
    private Integer quantity = 0;
    private CategoryDto category;
    private List<ProductAttributeValueDto> productAttributeValues;
    private List<ProductMarketDto> productMarkets;
    private List<Long> marketIds; // Для создания связей с маркетами
    private List<Integer> marketQuantities; // Количество для каждого маркета
    private List<Double> marketPrices; // Цена для каждого маркета
}
