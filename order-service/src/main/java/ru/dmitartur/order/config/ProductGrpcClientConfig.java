package ru.dmitartur.order.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmitartur.common.grpc.ProductServiceGrpc;

/**
 * Конфигурация gRPC клиента для product-service
 */
@Slf4j
@Configuration
public class ProductGrpcClientConfig {

    @Bean
    public ProductServiceGrpc.ProductServiceBlockingStub productServiceBlockingStub() {
        log.info("🔧 Creating gRPC client for product-service at: localhost:9093");
        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9093").usePlaintext().build();
        return ProductServiceGrpc.newBlockingStub(channel);
    }
}
