package ru.dmitartur.order.service.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.grpc.*;

import java.util.Optional;

/**
 * gRPC сервис для работы с продуктами
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductGrpcService {

    private final ProductServiceGrpc.ProductServiceBlockingStub productServiceStub;

    /**
     * Найти продукт по артикулу через gRPC
     */
    public Optional<ProductService.ProductInfo> findProductByArticle(String article) {
        try {
            log.debug("🔍 gRPC: Searching product by article: {}", article);
            
            FindByArticleRequest request = FindByArticleRequest.newBuilder()
                    .setArticle(article)
                    .build();
            
            FindByArticleResponse response = productServiceStub.findByArticle(request);
            
            if (response.getFound()) {
                ProductInfoDto productDto = response.getProduct();
                ProductService.ProductInfo productInfo = convertToProductInfo(productDto);
                
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
    private ProductService.ProductInfo convertToProductInfo(ProductInfoDto productDto) {
        ProductService.ProductInfo productInfo = new ProductService.ProductInfo();
        productInfo.setId(productDto.getId());
        productInfo.setName(productDto.getName());
        productInfo.setSku(productDto.getSku());
        productInfo.setStock(productDto.getStock());
        productInfo.setBarcode(productDto.getBarcode());
        return productInfo;
    }
}
