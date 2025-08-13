package ru.dmitartur.order.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import ru.dmitartur.common.grpc.JsonRequest;
import ru.dmitartur.common.grpc.JsonResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.dmitartur.order.service.OrderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInternalGrpcServer extends OrderInternalServiceGrpc.OrderInternalServiceImplBase {

    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${grpc.server.port:7068}")
    private int grpcPort;

    private Server server;

    @PostConstruct
    public void start() throws Exception {
        server = ServerBuilder.forPort(grpcPort)
                .addService(this)
                .build()
                .start();
        log.info("order-service gRPC server started on port {}", grpcPort);
    }

    @PreDestroy
    public void stop() {
        if (server != null) server.shutdown();
    }

    @Override
    public void upsertOrders(JsonRequest request, StreamObserver<JsonResponse> responseObserver) {
        try {
            JsonNode payload = objectMapper.readTree(request.getJson());
            int upserted = orderService.upsertBatch(payload);
            var resp = objectMapper.createObjectNode().put("upserted", upserted);
            responseObserver.onNext(JsonResponse.newBuilder().setJson(resp.toString()).build());
        } catch (Exception e) {
            var resp = objectMapper.createObjectNode().put("error", e.getMessage());
            responseObserver.onNext(JsonResponse.newBuilder().setJson(resp.toString()).build());
        }
        responseObserver.onCompleted();
    }
}


