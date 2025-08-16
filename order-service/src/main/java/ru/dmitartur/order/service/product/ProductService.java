package ru.dmitartur.order.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Сервис для работы с продуктами через gRPC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductGrpcService productGrpcService;

    /**
     * Найти продукт по артикулу (article)
     */
    public Optional<ProductInfo> findProductByArticle(String article) {
        return productGrpcService.findProductByArticle(article);
    }

    /**
     * DTO для информации о продукте
     */
    public static class ProductInfo {
        private Long id;
        private String name;
        private String sku;
        private Integer stock;
        private String barcode;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
    }


}
