package ru.dmitartur.order.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.grpc.*;
import ru.dmitartur.common.grpc.client.ProductGrpcClient;
import ru.dmitartur.order.service.OrderService;

@Slf4j
@Component
@RequiredArgsConstructor
@GrpcService
public class OrderInternalGrpcServer extends OrderInternalServiceGrpc.OrderInternalServiceImplBase {

    private final OrderService orderService;
    private final ProductGrpcClient productGrpcClient;
    private final ObjectMapper objectMapper = new ObjectMapper();



    @Override
    public void upsertOrdersDto(UpsertOrdersRequest request, StreamObserver<UpsertOrdersResponse> responseObserver) {
        try {
            log.info("🔄 Received upsertOrdersDto request with {} orders", request.getOrdersCount());
            
            int processedCount = 0;
            for (var orderDto : request.getOrdersList()) {
                try {
                    // Проверяем, существует ли заказ
                    var existingOrder = orderService.findByPostingNumber(orderDto.getPostingNumber());
                    
                    // Конвертируем gRPC OrderDto в Order entity
                    var order = convertGrpcOrderToEntity(orderDto);
                    
                    // Ищем товары по SKU и устанавливаем productId
                    for (var item : order.getItems()) {
                        if (item.getOfferId() != null && !item.getOfferId().isEmpty()) {
                            try {
                                var productInfo = productGrpcClient.findProductByArticle(item.getOfferId());
                                if (productInfo.isPresent()) {
                                    item.setProductId(productInfo.get().getId());
                                    log.debug("✅ Found product by OFFER_ID: OFFER_ID={}, productId={}",
                                            item.getOfferId(), item.getProductId());
                                } else {
                                    log.warn("⚠️ Product not found by OFFER_ID: OFFER_ID={}, will save with productId=null",
                                            item.getOfferId());
                                }
                            } catch (Exception e) {
                                log.warn("⚠️ Error searching product by OFFER_ID {}: {}, will save with productId=null",
                                        item.getOfferId(), e.getMessage());
                            }
                        }
                    }
                    
                    if (existingOrder.isPresent()) {
                        // Обновляем существующий заказ
                        order.setId(existingOrder.get().getId());
                        orderService.update(order);
                        log.debug("🔄 Updated existing order: {}", orderDto.getPostingNumber());
                    } else {
                        // Создаем новый заказ
                        orderService.save(order);
                        log.debug("✅ Created new order: {}", orderDto.getPostingNumber());
                    }
                    processedCount++;
                } catch (Exception e) {
                    log.error("❌ Error processing order {}: {}", orderDto.getPostingNumber(), e.getMessage());
                }
            }
            
            var response = UpsertOrdersResponse.newBuilder()
                    .setProcessedCount(processedCount)
                    .setSuccess(true)
                    .build();
            
            responseObserver.onNext(response);
            log.info("✅ upsertOrdersDto completed: {} orders processed", processedCount);
            
        } catch (Exception e) {
            log.error("❌ Error in upsertOrdersDto: {}", e.getMessage(), e);
            var response = UpsertOrdersResponse.newBuilder()
                    .setProcessedCount(0)
                    .setSuccess(false)
                    .addErrors(e.getMessage())
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void findOrder(ru.dmitartur.common.grpc.FindOrderRequest request, 
                         StreamObserver<ru.dmitartur.common.grpc.FindOrderResponse> responseObserver) {
        try {
            log.info("🔍 Received findOrder request for posting number: {}", request.getPostingNumber());
            
            var orderOptional = orderService.findByPostingNumberDto(request.getPostingNumber());
            
            if (orderOptional.isPresent()) {
                var orderDto = orderOptional.get();
                var grpcOrderDto = convertOrderDtoToGrpc(orderDto);
                
                var response = ru.dmitartur.common.grpc.FindOrderResponse.newBuilder()
                        .setOrder(grpcOrderDto)
                        .setFound(true)
                        .build();
                
                responseObserver.onNext(response);
                log.info("✅ Order found: postingNumber={}", request.getPostingNumber());
            } else {
                var response = ru.dmitartur.common.grpc.FindOrderResponse.newBuilder()
                        .setFound(false)
                        .build();
                
                responseObserver.onNext(response);
                log.info("❌ Order not found: postingNumber={}", request.getPostingNumber());
            }
            
        } catch (Exception e) {
            log.error("❌ Error in findOrder: {}", e.getMessage(), e);
            var response = ru.dmitartur.common.grpc.FindOrderResponse.newBuilder()
                    .setFound(false)
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getOrdersByMarket(ru.dmitartur.common.grpc.GetOrdersByMarketRequest request,
                                 StreamObserver<ru.dmitartur.common.grpc.GetOrdersByMarketResponse> responseObserver) {
        try {
            log.info("📋 Received getOrdersByMarket request: market={}, page={}, size={}", 
                    request.getMarket(), request.getPage(), request.getSize());
            
            var market = ru.dmitartur.common.enums.Market.valueOf(request.getMarket().name());
            var pageable = org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize());
            
            var ordersPage = orderService.findByMarketDto(market, pageable);
            
            var grpcOrders = ordersPage.getContent().stream()
                    .map(this::convertOrderDtoToGrpc)
                    .toList();
            
            var response = ru.dmitartur.common.grpc.GetOrdersByMarketResponse.newBuilder()
                    .addAllOrders(grpcOrders)
                    .setTotalElements((int) ordersPage.getTotalElements())
                    .setTotalPages(ordersPage.getTotalPages())
                    .setCurrentPage(ordersPage.getNumber())
                    .setSize(ordersPage.getSize())
                    .build();
            
            responseObserver.onNext(response);
            log.info("✅ getOrdersByMarket completed: market={}, totalElements={}, totalPages={}", 
                    market, ordersPage.getTotalElements(), ordersPage.getTotalPages());
            
        } catch (Exception e) {
            log.error("❌ Error in getOrdersByMarket: {}", e.getMessage(), e);
            var response = ru.dmitartur.common.grpc.GetOrdersByMarketResponse.newBuilder()
                    .setTotalElements(0)
                    .setTotalPages(0)
                    .setCurrentPage(0)
                    .setSize(0)
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    /**
     * Конвертировать gRPC OrderDto в Order entity
     */
    private ru.dmitartur.order.entity.Order convertGrpcOrderToEntity(ru.dmitartur.common.grpc.OrderDto grpcOrder) {
        var order = new ru.dmitartur.order.entity.Order();
        order.setPostingNumber(grpcOrder.getPostingNumber());
        order.setSource(grpcOrder.getSource());
        order.setMarket(ru.dmitartur.common.enums.Market.valueOf(grpcOrder.getMarket().name()));
        order.setStatus(ru.dmitartur.common.enums.OrderStatus.valueOf(grpcOrder.getStatus().name()));
        
        if (!grpcOrder.getCreatedAt().isEmpty()) {
            order.setCreatedAt(java.time.LocalDateTime.parse(grpcOrder.getCreatedAt()));
        }
        if (!grpcOrder.getOzonCreatedAt().isEmpty()) {
            order.setOzonCreatedAt(java.time.LocalDateTime.parse(grpcOrder.getOzonCreatedAt()));
        }
        if (!grpcOrder.getUpdatedAt().isEmpty()) {
            order.setUpdatedAt(java.time.LocalDateTime.parse(grpcOrder.getUpdatedAt()));
        }
        
        order.setCustomerName(grpcOrder.getCustomerName());
        order.setCustomerPhone(grpcOrder.getCustomerPhone());
        order.setAddress(grpcOrder.getAddress());
        
        if (!grpcOrder.getTotalPrice().isEmpty()) {
            order.setTotalPrice(new java.math.BigDecimal(grpcOrder.getTotalPrice()));
        }
        
        // FBS поля - даты
        if (!grpcOrder.getInProcessAt().isEmpty()) {
            order.setInProcessAt(java.time.LocalDateTime.parse(grpcOrder.getInProcessAt()));
        }
        if (!grpcOrder.getShipmentDate().isEmpty()) {
            order.setShipmentDate(java.time.LocalDateTime.parse(grpcOrder.getShipmentDate()));
        }
        if (!grpcOrder.getDeliveringDate().isEmpty()) {
            order.setDeliveringDate(java.time.LocalDateTime.parse(grpcOrder.getDeliveringDate()));
        }
        
        // FBS поля - отмена
        if (!grpcOrder.getCancelReason().isEmpty()) {
            order.setCancelReason(grpcOrder.getCancelReason());
        }
        if (grpcOrder.getCancelReasonId() > 0) {
            order.setCancelReasonId(grpcOrder.getCancelReasonId());
        }
        if (!grpcOrder.getCancellationType().isEmpty()) {
            order.setCancellationType(grpcOrder.getCancellationType());
        }
        
        // FBS поля - доставка
        if (!grpcOrder.getTrackingNumber().isEmpty()) {
            order.setTrackingNumber(grpcOrder.getTrackingNumber());
        }
        if (!grpcOrder.getDeliveryMethodName().isEmpty()) {
            order.setDeliveryMethodName(grpcOrder.getDeliveryMethodName());
        }
        if (!grpcOrder.getSubstatus().isEmpty()) {
            order.setSubstatus(grpcOrder.getSubstatus());
        }
        order.setIsExpress(grpcOrder.getIsExpress());
        
        // Конвертируем items
        var items = grpcOrder.getItemsList().stream()
                .map(grpcItem -> convertGrpcOrderItemToEntity(grpcItem, order))
                .toList();
        order.setItems(new java.util.ArrayList<>(items));
        
        return order;
    }

    /**
     * Конвертировать gRPC OrderItemDto в OrderItem entity
     */
    private ru.dmitartur.order.entity.OrderItem convertGrpcOrderItemToEntity(ru.dmitartur.common.grpc.OrderItemDto grpcItem, ru.dmitartur.order.entity.Order order) {
        var item = new ru.dmitartur.order.entity.OrderItem();
        item.setOrder(order); // Устанавливаем связь с заказом
        item.setProductId(grpcItem.getProductId() > 0 ? grpcItem.getProductId() : null);
        item.setOfferId(grpcItem.getOfferId());
        item.setName(grpcItem.getName());
        item.setQuantity(grpcItem.getQuantity());
        item.setSku(grpcItem.getSku());
        
        if (!grpcItem.getPrice().isEmpty()) {
            item.setPrice(new java.math.BigDecimal(grpcItem.getPrice()));
        }
        
        return item;
    }

    /**
     * Конвертировать OrderDto в gRPC OrderDto
     */
    private ru.dmitartur.common.grpc.OrderDto convertOrderDtoToGrpc(ru.dmitartur.order.dto.OrderDto orderDto) {
        var grpcOrder = ru.dmitartur.common.grpc.OrderDto.newBuilder()
                .setId(orderDto.getId())
                .setPostingNumber(orderDto.getPostingNumber())
                .setSource(orderDto.getSource())
                .setMarket(ru.dmitartur.common.grpc.Market.valueOf(orderDto.getMarket().name()));
        
        // Статус - конвертируем строку в enum
        try {
            var status = ru.dmitartur.common.enums.OrderStatus.valueOf(orderDto.getStatus());
            grpcOrder.setStatus(ru.dmitartur.common.grpc.OrderStatus.valueOf(status.name()));
        } catch (IllegalArgumentException e) {
            grpcOrder.setStatus(ru.dmitartur.common.grpc.OrderStatus.UNKNOWN);
        }
        
        // Даты
        if (orderDto.getCreatedAt() != null) {
            grpcOrder.setCreatedAt(orderDto.getCreatedAt().toString());
        }
        if (orderDto.getUpdatedAt() != null) {
            grpcOrder.setUpdatedAt(orderDto.getUpdatedAt().toString());
        }
        
        // Основные поля
        grpcOrder.setCustomerName(orderDto.getCustomerName() != null ? orderDto.getCustomerName() : "");
        grpcOrder.setCustomerPhone(orderDto.getCustomerPhone() != null ? orderDto.getCustomerPhone() : "");
        grpcOrder.setAddress(orderDto.getAddress() != null ? orderDto.getAddress() : "");
        
        if (orderDto.getTotalPrice() != null) {
            grpcOrder.setTotalPrice(orderDto.getTotalPrice().toString());
        }
        
        // FBS поля - даты
        if (orderDto.getInProcessAt() != null) {
            grpcOrder.setInProcessAt(orderDto.getInProcessAt().toString());
        }
        if (orderDto.getShipmentDate() != null) {
            grpcOrder.setShipmentDate(orderDto.getShipmentDate().toString());
        }
        if (orderDto.getDeliveringDate() != null) {
            grpcOrder.setDeliveringDate(orderDto.getDeliveringDate().toString());
        }
        
        // FBS поля - отмена
        if (orderDto.getCancelReason() != null) {
            grpcOrder.setCancelReason(orderDto.getCancelReason());
        }
        if (orderDto.getCancelReasonId() != null) {
            grpcOrder.setCancelReasonId(orderDto.getCancelReasonId());
        }
        if (orderDto.getCancellationType() != null) {
            grpcOrder.setCancellationType(orderDto.getCancellationType());
        }
        
        // FBS поля - доставка
        if (orderDto.getTrackingNumber() != null) {
            grpcOrder.setTrackingNumber(orderDto.getTrackingNumber());
        }
        if (orderDto.getDeliveryMethodName() != null) {
            grpcOrder.setDeliveryMethodName(orderDto.getDeliveryMethodName());
        }
        if (orderDto.getSubstatus() != null) {
            grpcOrder.setSubstatus(orderDto.getSubstatus());
        }
        if (orderDto.getIsExpress() != null) {
            grpcOrder.setIsExpress(orderDto.getIsExpress());
        }
        
        // Конвертируем items
        if (orderDto.getItems() != null) {
            for (var item : orderDto.getItems()) {
                var grpcItem = ru.dmitartur.common.grpc.OrderItemDto.newBuilder()
                        .setProductId(item.getProductId() != null ? item.getProductId() : 0)
                        .setOfferId(item.getOfferId() != null ? item.getOfferId() : "")
                        .setName(item.getName() != null ? item.getName() : "")
                        .setQuantity(item.getQuantity() != null ? item.getQuantity() : 0)
                        .setSku(""); // SKU не доступен в order-service OrderItemDto
                
                if (item.getPrice() != null) {
                    grpcItem.setPrice(item.getPrice().toString());
                }
                
                grpcOrder.addItems(grpcItem.build());
            }
        }
        
        return grpcOrder.build();
    }
}


