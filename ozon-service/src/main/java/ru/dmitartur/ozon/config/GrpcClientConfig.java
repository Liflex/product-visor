package ru.dmitartur.ozon.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.client.order-service.address:localhost:9099}")
    private String orderServiceAddress;

    @Bean
    public OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderInternalServiceBlockingStub() {
        ManagedChannel ch = ManagedChannelBuilder.forTarget(orderServiceAddress).usePlaintext().build();
        return OrderInternalServiceGrpc.newBlockingStub(ch);
    }
}


