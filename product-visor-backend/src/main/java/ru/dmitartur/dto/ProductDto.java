package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.entity.PackageInfo;
import ru.dmitartur.entity.Product;

import java.util.List;
import java.util.UUID;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String article;
    private String imageUrl;
    private byte[] image; // Add image field for database storage
    private String barcode;
    private Integer quantity = 0; // количество на складе
    private CategoryDto category;
    private Double price; // цена товара
    private UUID ownerUserId;
    private UUID companyId;
    private List<ProductStockDto> productStocks;
    private PackageInfo packageInfo; // информация об упаковке
    private List<ProductAttributeValueDto> productAttributeValues;
    private List<ProductMarketDto> productMarkets;
    private List<Long> marketIds; // Для создания связей с маркетами
    private List<Integer> marketQuantities; // Количество для каждого маркета
    private List<Double> marketPrices; // Цена для каждого маркета
}
