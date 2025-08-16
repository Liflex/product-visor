package ru.dmitartur.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.dmitartur.common.grpc.*;
import ru.dmitartur.entity.Product;
import ru.dmitartur.service.ProductService;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * gRPC сервер для работы с продуктами
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class ProductGrpcServer extends ProductServiceGrpc.ProductServiceImplBase {

    private final ProductService productService;

    @Override
    public void findByArticle(FindByArticleRequest request, StreamObserver<FindByArticleResponse> responseObserver) {
        try {
            String article = request.getArticle();
            log.info("🔍 gRPC: Searching product by article: {}", article);

            Optional<Product> productOpt = productService.findByArticle(article);
            
            FindByArticleResponse.Builder responseBuilder = FindByArticleResponse.newBuilder();
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                ProductInfoDto productDto = convertToProductInfoDto(product);
                
                responseBuilder.setFound(true)
                             .setProduct(productDto);
                
                log.info("✅ gRPC: Product found by article: {} -> id={}, name={}", 
                        article, product.getId(), product.getName());
            } else {
                responseBuilder.setFound(false);
                log.info("❌ gRPC: Product not found by article: {}", article);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("❌ gRPC: Error searching product by article: {}", e.getMessage(), e);
            
            FindByArticleResponse errorResponse = FindByArticleResponse.newBuilder()
                    .setFound(false)
                    .setErrorMessage("Error searching product: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateStockByArticle(UpdateStockRequest request, StreamObserver<UpdateStockResponse> responseObserver) {
        try {
            String article = request.getArticle();
            int quantityChange = request.getQuantityChange();
            
            log.info("📦 gRPC: Updating product stock: article={}, change={}", article, quantityChange);
            
            boolean success = productService.updateQuantityByArticle(article, quantityChange);
            
            UpdateStockResponse.Builder responseBuilder = UpdateStockResponse.newBuilder()
                    .setSuccess(success);
            
            if (!success) {
                responseBuilder.setErrorMessage("Product not found or update failed");
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
            if (success) {
                log.info("✅ gRPC: Product stock updated successfully: article={}, change={}", article, quantityChange);
            } else {
                log.warn("⚠️ gRPC: Failed to update product stock: article={}", article);
            }
            
        } catch (Exception e) {
            log.error("❌ gRPC: Error updating product stock: {}", e.getMessage(), e);
            
            UpdateStockResponse errorResponse = UpdateStockResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("Error updating stock: " + e.getMessage())
                    .build();
            
            responseObserver.onNext(errorResponse);
            responseObserver.onCompleted();
        }
    }

    /**
     * Конвертировать Product в ProductInfoDto
     */
    private ProductInfoDto convertToProductInfoDto(Product product) {
        return ProductInfoDto.newBuilder()
                .setId(product.getId())
                .setName(product.getName() != null ? product.getName() : "")
                .setArticle(product.getArticle() != null ? product.getArticle() : "")
                .setSku("") // SKU не используется в Product entity
                .setStock(product.getQuantity() != null ? product.getQuantity() : 0)
                .setBarcode(product.getBarcode() != null ? product.getBarcode() : "")
                .setDescription("") // Description не используется в Product entity
                .setPrice(product.getPrice() != null ? product.getPrice().toString() : "0")
                .setCreatedAt("") // CreatedAt не используется в Product entity
                .setUpdatedAt("") // UpdatedAt не используется в Product entity
                .build();
    }
}
