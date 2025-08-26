package ru.dmitartur.yandex.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.library.marketplace.dto.*;
import ru.dmitartur.yandex.service.YandexOrderSyncService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSyncConsumer {
    
    private final YandexOrderSyncService yandexOrderSyncService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @KafkaListener(
        topics = KafkaTopics.ORDER_SYNC_TOPIC,
        groupId = "yandex-order-sync-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderSyncRequest(
            @Payload Object request,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {
        
        log.info("📨 Received order sync request from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            // Проверяем, что запрос предназначен для Yandex
            if (!isRequestForYandex(request)) {
                log.debug("⏭️ Skipping request for marketplace: {}", getMarketplaceFromRequest(request));
                return;
            }
            
            // Определяем тип операции по содержимому запроса
            String operationType = getOperationTypeFromRequest(request);
            
            log.info("🔄 Processing Yandex order {} operation for {} items", 
                    operationType, getItemsCountFromRequest(request));
            
            // Выполняем синхронизацию заказов
            Object response = yandexOrderSyncService.syncOrders(operationType, request);
            
            // Отправляем ответ обратно
            kafkaTemplate.send(KafkaTopics.ORDER_SYNC_RESPONSE_TOPIC, "YANDEX", response)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("❌ Failed to send order {} operation response: {}", 
                                    operationType, throwable.getMessage());
                        } else {
                            log.info("✅ Order {} operation response sent successfully to partition: {}, offset: {}", 
                                    operationType, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
            
        } catch (Exception e) {
            log.error("❌ Error processing order sync request: {}", e.getMessage(), e);
            
            // Отправляем ответ с ошибкой
            Object errorResponse = createErrorResponse(request);
            kafkaTemplate.send(KafkaTopics.ORDER_SYNC_RESPONSE_TOPIC, "YANDEX", errorResponse);
        }
    }
    
    private String getOperationTypeFromRequest(Object request) {
        // Определяем тип операции по содержимому запроса
        if (request instanceof OrderSyncRequest) {
            return "ORDER_SYNC";
        }
        if (request instanceof OrderStatusSyncRequest) {
            return "ORDER_STATUS_SYNC";
        }
        if (request instanceof OrderItemStatusSyncRequest) {
            return "ORDER_ITEM_STATUS_SYNC";
        }
        if (request instanceof OrderItemShipmentSyncRequest) {
            return "ORDER_ITEM_SHIPMENT_SYNC";
        }
        if (request instanceof OrderItemReturnSyncRequest) {
            return "ORDER_ITEM_RETURN_SYNC";
        }
        if (request instanceof OrderItemCancellationSyncRequest) {
            return "ORDER_ITEM_CANCELLATION_SYNC";
        }
        if (request instanceof OrderItemRefundSyncRequest) {
            return "ORDER_ITEM_REFUND_SYNC";
        }
        if (request instanceof OrderItemReplacementSyncRequest) {
            return "ORDER_ITEM_REPLACEMENT_SYNC";
        }
        if (request instanceof OrderItemDisputeSyncRequest) {
            return "ORDER_ITEM_DISPUTE_SYNC";
        }
        if (request instanceof OrderItemDisputeResolutionSyncRequest) {
            return "ORDER_ITEM_DISPUTE_RESOLUTION_SYNC";
        }
        if (request instanceof OrderItemDisputeAppealSyncRequest) {
            return "ORDER_ITEM_DISPUTE_APPEAL_SYNC";
        }
        return "UNKNOWN";
    }
    
    private boolean isRequestForYandex(Object request) {
        String marketplace = getMarketplaceFromRequest(request);
        return "YANDEX".equalsIgnoreCase(marketplace);
    }
    
    private String getMarketplaceFromRequest(Object request) {
        if (request instanceof OrderSyncRequest) {
            return ((OrderSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderStatusSyncRequest) {
            return ((OrderStatusSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemStatusSyncRequest) {
            return ((OrderItemStatusSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemShipmentSyncRequest) {
            return ((OrderItemShipmentSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemReturnSyncRequest) {
            return ((OrderItemReturnSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemCancellationSyncRequest) {
            return ((OrderItemCancellationSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemRefundSyncRequest) {
            return ((OrderItemRefundSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemReplacementSyncRequest) {
            return ((OrderItemReplacementSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemDisputeSyncRequest) {
            return ((OrderItemDisputeSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemDisputeResolutionSyncRequest) {
            return ((OrderItemDisputeResolutionSyncRequest) request).getMarketplace();
        }
        if (request instanceof OrderItemDisputeAppealSyncRequest) {
            return ((OrderItemDisputeAppealSyncRequest) request).getMarketplace();
        }
        return "UNKNOWN";
    }
    
    private int getItemsCountFromRequest(Object request) {
        if (request instanceof OrderSyncRequest) {
            return ((OrderSyncRequest) request).getOrders().size();
        }
        if (request instanceof OrderStatusSyncRequest) {
            return ((OrderStatusSyncRequest) request).getOrders().size();
        }
        if (request instanceof OrderItemStatusSyncRequest) {
            return ((OrderItemStatusSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemShipmentSyncRequest) {
            return ((OrderItemShipmentSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemReturnSyncRequest) {
            return ((OrderItemReturnSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemCancellationSyncRequest) {
            return ((OrderItemCancellationSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemRefundSyncRequest) {
            return ((OrderItemRefundSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemReplacementSyncRequest) {
            return ((OrderItemReplacementSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemDisputeSyncRequest) {
            return ((OrderItemDisputeSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemDisputeResolutionSyncRequest) {
            return ((OrderItemDisputeResolutionSyncRequest) request).getItems().size();
        }
        if (request instanceof OrderItemDisputeAppealSyncRequest) {
            return ((OrderItemDisputeAppealSyncRequest) request).getItems().size();
        }
        return 0;
    }
    
    private Object createErrorResponse(Object request) {
        int itemsCount = getItemsCountFromRequest(request);
        String operationType = getOperationTypeFromRequest(request);
        
        // Создаем базовый ответ с ошибкой
        return OrderSyncResponse.builder()
                .marketplace("YANDEX")
                .status("FAILED")
                .processedAt(java.time.LocalDateTime.now())
                .totalOrders(itemsCount)
                .successCount(0)
                .failedCount(itemsCount)
                .errorMessage("Operation failed: " + operationType)
                .build();
    }
}
