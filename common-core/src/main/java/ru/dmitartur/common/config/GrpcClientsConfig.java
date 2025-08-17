package ru.dmitartur.common.config;

import io.grpc.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import ru.dmitartur.common.grpc.OzonInternalServiceGrpc;
import ru.dmitartur.common.security.MachineTokenService;

@Configuration
@RequiredArgsConstructor
public class GrpcClientsConfig {

    private final MachineTokenService machineTokenService;

    @Value("${grpc.client.order-service.address:localhost:9098}")
    private String orderServiceAddress;

    @Value("${grpc.client.ozon-service.address:localhost:7097}")
    private String ozonServiceAddress;

    private static final Metadata.Key<String> AUTHORIZATION = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private ClientInterceptor authInterceptor() {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
                return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        String token = machineTokenService.getBearerToken();
                        if (token != null && !token.isBlank()) {
                            headers.put(AUTHORIZATION, "Bearer " + token);
                        }
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }

    private ManagedChannel channelFor(String address) {
        return ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    }

    @Bean
    public OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderInternalBlockingStub() {
        ManagedChannel ch = channelFor(orderServiceAddress);
        return OrderInternalServiceGrpc.newBlockingStub(ch).withInterceptors(authInterceptor());
    }

    @Bean
    public OzonInternalServiceGrpc.OzonInternalServiceBlockingStub ozonInternalBlockingStub() {
        ManagedChannel ch = channelFor(ozonServiceAddress);
        return OzonInternalServiceGrpc.newBlockingStub(ch).withInterceptors(authInterceptor());
    }
}


