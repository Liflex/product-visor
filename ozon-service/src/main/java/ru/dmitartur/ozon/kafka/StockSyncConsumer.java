package ru.dmitartur.ozon.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.common.dto.marketplace.StockItemReqest;
import ru.dmitartur.common.dto.marketplace.StockSyncResponse;
import ru.dmitartur.ozon.service.OzonStockSyncService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockSyncConsumer {
    
    private final OzonStockSyncService ozonStockSyncService;
    private final KafkaTemplate<String, StockSyncResponse> kafkaTemplate;
    
    @KafkaListener(
        topics = KafkaTopics.STOCK_SYNC_TOPIC,
        groupId = "ozon-stock-sync-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStockSyncRequest(
            @Payload StockItemReqest request,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset) {
        
        log.info("📨 Received stock sync request from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            // Проверяем, что запрос предназначен для Ozon через warehouses
            boolean isOzonRequest = request.getWarehouses() != null && 
                    request.getWarehouses().stream()
                            .anyMatch(warehouse -> "OZON".equalsIgnoreCase(warehouse.getMarketplace()));
            
            if (!isOzonRequest) {
                log.debug("⏭️ Skipping request - no Ozon warehouses found in request");
                return;
            }
            
            log.info("🔄 Processing Ozon stock sync request for offerId: {}, quantity: {}, warehouses: {}", 
                    request.getOfferId(), request.getQuantity(), request.getWarehouses().size());
            
            // Выполняем синхронизацию остатков
            StockSyncResponse response = ozonStockSyncService.syncStock(request);
            
            // Отправляем ответ обратно
            kafkaTemplate.send(KafkaTopics.STOCK_SYNC_RESPONSE_TOPIC, "OZON", response)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("❌ Failed to send stock sync response: {}", throwable.getMessage());
                        } else {
                            log.info("✅ Stock sync response sent successfully to partition: {}, offset: {}", 
                                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
            
        } catch (Exception e) {
            log.error("❌ Error processing stock sync request: {}", e.getMessage(), e);
            
            // Отправляем ответ с ошибкой
            StockSyncResponse errorResponse = StockSyncResponse.builder()
                    .marketplace("OZON")
                    .status("FAILED")
                    .processedAt(java.time.LocalDateTime.now())
                    .totalItems(1)
                    .successCount(0)
                    .failedCount(1)
                    .errorMessage(e.getMessage())
                    .build();
            
            kafkaTemplate.send(KafkaTopics.STOCK_SYNC_RESPONSE_TOPIC, "OZON", errorResponse);
        }
    }
}
