package ru.dmitartur.common.grpc.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.grpc.*;

import java.util.Optional;

/**
 * gRPC клиент для работы с продуктами
 * Общий компонент для использования в любом микросервисе
 */
@Slf4j
@Component
@ConditionalOnBean(ProductServiceGrpc.ProductServiceBlockingStub.class)
@RequiredArgsConstructor
public class ProductGrpcClient {

    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

    /**
     * Найти продукт по артикулу через gRPC
     */
    public Optional<ProductInfo> findProductByArticle(String article) {
        try {
            log.debug("🔍 gRPC: Searching product by article: {}", article);
            
            FindByArticleRequest request = FindByArticleRequest.newBuilder()
                    .setArticle(article)
                    .build();
            
            FindByArticleResponse response = productServiceStub.findByArticle(request);
            
            if (response.getFound()) {
                ProductInfoDto productDto = response.getProduct();
                ProductInfo productInfo = convertToProductInfo(productDto);
                
                log.debug("✅ gRPC: Product found by article: {} -> id={}, name={}", 
                         article, productInfo.getId(), productInfo.getName());
                return Optional.of(productInfo);
            } else {
                log.debug("❌ gRPC: Product not found: article={}", article);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.warn("❌ gRPC: Error searching product by article {}: {}", article, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Обновить остаток товара по артикулу через gRPC
     */
    public boolean updateProductStockByArticle(String article, int quantityChange) {
        try {
            log.debug("📦 gRPC: Updating product stock: article={}, change={}", article, quantityChange);
            
            UpdateStockRequest request = UpdateStockRequest.newBuilder()
                    .setArticle(article)
                    .setQuantityChange(quantityChange)
                    .build();
            
            UpdateStockResponse response = productServiceStub.updateStockByArticle(request);
            
            if (response.getSuccess()) {
                log.debug("✅ gRPC: Product stock updated successfully: article={}, change={}", article, quantityChange);
                return true;
            } else {
                log.warn("⚠️ gRPC: Failed to update product stock: article={}, error={}", 
                        article, response.getErrorMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ gRPC: Error updating product stock for article {}: {}", article, e.getMessage());
            return false;
        }
    }

    /**
     * Конвертировать ProductInfoDto в ProductInfo
     */
    private ProductInfo convertToProductInfo(ProductInfoDto productDto) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(productDto.getId());
        productInfo.setName(productDto.getName());
        productInfo.setSku(productDto.getSku());
        productInfo.setStock(productDto.getStock());
        productInfo.setBarcode(productDto.getBarcode());
        return productInfo;
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
