package ru.dmitartur.ozon.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmitartur.common.grpc.ProductServiceGrpc;

/**
 * Конфигурация gRPC клиента для product-service
 * Используется для получения данных о продуктах (публичный API без авторизации)
 */
@Slf4j
@Configuration
public class ProductGrpcClientConfig {

    @Value("${grpc.client.product-service.address:localhost:9093}")
    private String productServiceAddress;

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub() {
        log.info("🔧 Creating gRPC client for product-service at: {}", productServiceAddress);
        ManagedChannel channel = ManagedChannelBuilder.forTarget(productServiceAddress).usePlaintext().build();
        return ProductServiceGrpc.newBlockingStub(channel);
    }
}
