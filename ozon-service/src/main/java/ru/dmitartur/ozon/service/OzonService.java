package ru.dmitartur.ozon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dmitartur.ozon.integration.OzonSellerApi;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import ru.dmitartur.common.grpc.UpsertOrdersRequest;
import ru.dmitartur.common.grpc.UpsertOrdersResponse;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.ozon.dto.DateRangeDto;
import ru.dmitartur.ozon.mapper.OzonOrderMapper;
import ru.dmitartur.ozon.retry.OzonRetryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonService {
    private final OzonSellerApi api;
    private final OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderStub;
    private final OzonOrderMapper ozonOrderMapper;
    private final OzonRetryService retryService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ozon.default-warehouse-id}")
    private String defaultWarehouseId;

    public JsonNode fboPostingList(JsonNode req) {
        return retryService.executeWithRetry(
            () -> api.fboPostingList(req), 
            "fboPostingList"
        );
    }

    public JsonNode fbsPostingList(JsonNode req) {
        return retryService.executeWithRetry(
            () -> api.fbsPostingList(req), 
            "fbsPostingList"
        );
    }

    public JsonNode fbsPostingGet(JsonNode req) {
        return retryService.executeWithRetry(
            () -> api.fbsPostingGet(req), 
            "fbsPostingGet"
        );
    }

    /**
     * Update OZON stock for a given offer (article). If warehouseId is null/empty, OZON may demand it for FBS.
     */
    public JsonNode updateStock(String offerId, int newQuantity, String warehouseId) {
        if (offerId == null || offerId.isEmpty()) {
            throw new IllegalArgumentException("offerId is required");
        }

        if(warehouseId == null || warehouseId.isEmpty()) {
            warehouseId = defaultWarehouseId;
        }

        ObjectNode root = mapper.createObjectNode();
        ArrayNode stocks = mapper.createArrayNode();
        ObjectNode item = mapper.createObjectNode();
        item.put("offer_id", offerId);
        item.put("stock", Math.max(0, newQuantity));
        if (warehouseId != null && !warehouseId.isEmpty()) {
            item.put("warehouse_id", warehouseId);
        }
        stocks.add(item);
        root.set("stocks", stocks);

        // Используем Spring Retry для обработки ошибок API
        JsonNode response = retryService.executeUpdateStocksWithRetry(
            () -> api.updateStocks(root), 
            offerId
        );
        
        log.info("OZON updateStocks: offerId={}, qty={}, warehouseId={}, response={}", 
            offerId, newQuantity, warehouseId, response.toString());
        return response;
    }

    /**
     * Синхронизация заказов FBO с Ozon API
     */
    public int backfillFboOrders(DateRangeDto range, int pageSize) {
        int totalUpserted = 0;
        int offset = 0;
        
        log.info("🔄 Starting FBO orders backfill from {} to {}", range.getFrom(), range.getTo());
        
        while (true) {
            try {
                // Создаем запрос к Ozon API
                var req = mapper.createObjectNode();
                req.putObject("createdAt").put("from", range.getFrom()).put("to", range.getTo());
                req.put("limit", pageSize);
                req.put("offset", offset);
                
                // Получаем данные от Ozon API
                JsonNode page = fboPostingList(req);
                
                // Проверяем структуру ответа
                if (!page.has("result") || !page.get("result").isArray()) {
                    log.warn("⚠️ Unexpected response structure from Ozon API: {}", page.toPrettyString());
                    break;
                }
                
                JsonNode result = page.get("result");
                int size = result.size();
                
                if (size == 0) {
                    log.info("✅ No more FBO orders to process, breaking loop");
                    break;
                }
                
                log.info("📦 Processing {} FBO orders from offset {}", size, offset);
                
                // Преобразуем заказы в OrderDto
                List<OrderDto> orderDtos = ozonOrderMapper.mapOzonOrdersToDtoList(result);
                
                if (!orderDtos.isEmpty()) {
                    // Отправляем заказы через новый gRPC метод
                    UpsertOrdersRequest request = UpsertOrdersRequest.newBuilder()
                            .addAllOrders(orderDtos.stream()
                                    .map(this::convertOrderDtoToGrpc)
                                    .toList())
                            .build();
                    
                    UpsertOrdersResponse response = orderStub.upsertOrdersDto(request);
                    totalUpserted += response.getProcessedCount();
                    
                    log.info("✅ Processed {} FBO orders, total upserted: {}", size, totalUpserted);
                }
                
                // Проверяем, есть ли еще данные
                if (size < pageSize) {
                    log.info("✅ Reached end of FBO data, breaking loop");
                    break;
                }
                
                offset += pageSize;
                
            } catch (Exception e) {
                log.error("❌ Error processing FBO orders at offset {}: {}", offset, e.getMessage(), e);
                break;
            }
        }
        
        log.info("✅ FBO backfill finished, total upserted: {} orders", totalUpserted);
        return totalUpserted;
    }

    /**
     * Синхронизация заказов FBS с Ozon API
     */
    public int backfillFbsOrders(DateRangeDto range, int pageSize) {
        int totalUpserted = 0;
        int offset = 0;
        
        log.info("🔄 Starting FBS orders backfill from {} to {}", range.getFrom(), range.getTo());
        
        while (true) {
            try {
                            // Создаем запрос к Ozon API для FBS согласно документации
            var req = mapper.createObjectNode();
            req.put("limit", pageSize);
            req.put("offset", offset);
            
            // Фильтр по дате создания - правильный формат согласно документации
            var filter = req.putObject("filter");
            filter.put("since", range.getFrom());
            filter.put("to", range.getTo());
                
                // Получаем данные от Ozon API
                log.debug("📤 Sending FBS request: {}", req.toPrettyString());
                JsonNode page = fbsPostingList(req);
                log.debug("📥 Received FBS response: {}", page.toPrettyString());
                
                // Проверяем на ошибки API
                if (page.has("error")) {
                    log.error("❌ FBS API error: {}", page.get("error").toPrettyString());
                    break;
                }
                
                // Проверяем структуру ответа FBS согласно документации
                if (!page.has("result")) {
                    log.warn("⚠️ FBS response missing 'result' field: {}", page.toPrettyString());
                    break;
                }
                
                JsonNode resultNode = page.get("result");
                JsonNode result;
                
                // Согласно документации, FBS API возвращает postings в result
                if (resultNode.has("postings") && resultNode.get("postings").isArray()) {
                    result = resultNode.get("postings");
                } else {
                    log.warn("⚠️ FBS response missing 'postings' array: {}", page.toPrettyString());
                    break;
                }
                int size = result.size();
                
                if (size == 0) {
                    log.info("✅ No more FBS orders to process, breaking loop");
                    break;
                }
                
                log.info("📦 Processing {} FBS orders from offset {}", size, offset);
                
                // Преобразуем заказы в OrderDto
                List<OrderDto> orderDtos = ozonOrderMapper.mapOzonFbsOrdersToDtoList(result);
                
                if (!orderDtos.isEmpty()) {
                    // Отправляем заказы через gRPC метод
                    UpsertOrdersRequest request = UpsertOrdersRequest.newBuilder()
                            .addAllOrders(orderDtos.stream()
                                    .map(this::convertOrderDtoToGrpc)
                                    .toList())
                            .build();
                    
                    UpsertOrdersResponse response = orderStub.upsertOrdersDto(request);
                    totalUpserted += response.getProcessedCount();
                    
                    log.info("✅ Processed {} FBS orders, total upserted: {}", size, totalUpserted);
                }
                
                // Проверяем, есть ли еще данные
                if (size < pageSize) {
                    log.info("✅ Reached end of FBS data, breaking loop");
                    break;
                }
                
                offset += pageSize;
                
            } catch (Exception e) {
                log.error("❌ Error processing FBS orders at offset {}: {}", offset, e.getMessage(), e);
                break;
            }
        }
        
        log.info("✅ FBS backfill finished, total upserted: {} orders", totalUpserted);
        return totalUpserted;
    }

    /**
     * Проверить доступность FBS API
     */
    public boolean isFbsApiAvailable() {
        try {
            var mapper = new ObjectMapper();
            var req = mapper.createObjectNode();
            req.put("limit", 1);
            req.put("offset", 0);
            
            var filter = req.putObject("filter");
            filter.put("since", "2024-08-01T00:00:00Z");
            filter.put("to", "2024-08-02T23:59:59Z");
            
            JsonNode response = fbsPostingList(req);
            return !response.has("error");
        } catch (Exception e) {
            log.warn("⚠️ FBS API not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Комбинированная синхронизация заказов FBO и FBS с Ozon API
     * Сначала синхронизирует FBO, затем FBS заказы
     */
    public int backfillAllOrders(DateRangeDto range, int pageSize) {
        log.info("🚀 Starting combined FBO + FBS orders backfill from {} to {}", range.getFrom(), range.getTo());
        
        int totalFboUpserted = 0;
        int totalFbsUpserted = 0;
        
        try {
            // 1. Синхронизация FBO заказов
            log.info("📦 Step 1: Starting FBO orders synchronization...");
            totalFboUpserted = backfillFboOrders(range, pageSize);
            log.info("✅ FBO synchronization completed: {} orders", totalFboUpserted);
            
            // Небольшая пауза между синхронизациями
            Thread.sleep(1000);
            
            // 2. Синхронизация FBS заказов (если API доступен)
            log.info("📦 Step 2: Checking FBS API availability...");
            if (isFbsApiAvailable()) {
                log.info("📦 Step 2: Starting FBS orders synchronization...");
                totalFbsUpserted = backfillFbsOrders(range, pageSize);
                log.info("✅ FBS synchronization completed: {} orders", totalFbsUpserted);
            } else {
                log.warn("⚠️ FBS API not available, skipping FBS synchronization");
            }
            
        } catch (InterruptedException e) {
            log.error("❌ Synchronization interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ Error during combined synchronization: {}", e.getMessage(), e);
        }
        
        int totalUpserted = totalFboUpserted + totalFbsUpserted;
        log.info("🎯 Combined synchronization finished: FBO={}, FBS={}, Total={} orders", 
                totalFboUpserted, totalFbsUpserted, totalUpserted);
        
        return totalUpserted;
    }

    /**
     * Конвертировать OrderDto в gRPC формат
     */
    private ru.dmitartur.common.grpc.OrderDto convertOrderDtoToGrpc(OrderDto orderDto) {
        var builder = ru.dmitartur.common.grpc.OrderDto.newBuilder()
                .setPostingNumber(orderDto.getPostingNumber())
                .setSource(orderDto.getSource())
                .setMarket(ru.dmitartur.common.grpc.Market.valueOf(orderDto.getMarket().name()))
                .setStatus(ru.dmitartur.common.grpc.OrderStatus.valueOf(orderDto.getStatus().name()))
                .setCreatedAt(orderDto.getCreatedAt() != null ? orderDto.getCreatedAt().toString() : "")
                .setUpdatedAt(orderDto.getUpdatedAt() != null ? orderDto.getUpdatedAt().toString() : "")
                .setOzonCreatedAt(orderDto.getOzonCreatedAt() != null ? orderDto.getOzonCreatedAt().toString() : "")
                .setCustomerName(orderDto.getCustomerName() != null ? orderDto.getCustomerName() : "")
                .setCustomerPhone(orderDto.getCustomerPhone() != null ? orderDto.getCustomerPhone() : "")
                .setAddress(orderDto.getAddress() != null ? orderDto.getAddress() : "")
                .setTotalPrice(orderDto.getTotalPrice() != null ? orderDto.getTotalPrice().toString() : "0")
                .addAllItems(orderDto.getItems().stream()
                        .map(this::convertOrderItemDtoToGrpc)
                        .toList());
        
        // FBS поля - даты
        if (orderDto.getInProcessAt() != null) {
            builder.setInProcessAt(orderDto.getInProcessAt().toString());
        }
        if (orderDto.getShipmentDate() != null) {
            builder.setShipmentDate(orderDto.getShipmentDate().toString());
        }
        if (orderDto.getDeliveringDate() != null) {
            builder.setDeliveringDate(orderDto.getDeliveringDate().toString());
        }
        
        // FBS поля - отмена
        if (orderDto.getCancelReason() != null) {
            builder.setCancelReason(orderDto.getCancelReason());
        }
        if (orderDto.getCancelReasonId() != null) {
            builder.setCancelReasonId(orderDto.getCancelReasonId());
        }
        if (orderDto.getCancellationType() != null) {
            builder.setCancellationType(orderDto.getCancellationType());
        }
        
        // FBS поля - доставка
        if (orderDto.getTrackingNumber() != null) {
            builder.setTrackingNumber(orderDto.getTrackingNumber());
        }
        if (orderDto.getDeliveryMethodName() != null) {
            builder.setDeliveryMethodName(orderDto.getDeliveryMethodName());
        }
        if (orderDto.getSubstatus() != null) {
            builder.setSubstatus(orderDto.getSubstatus());
        }
        if (orderDto.getIsExpress() != null) {
            builder.setIsExpress(orderDto.getIsExpress());
        }
        
        // Вычисляемые поля
        if (orderDto.getDaysInTransit() != null) {
            builder.setDaysInTransit(orderDto.getDaysInTransit());
        }
        if (orderDto.getDaysInProcessing() != null) {
            builder.setDaysInProcessing(orderDto.getDaysInProcessing());
        }
        
        return builder.build();
    }

    /**
     * Конвертировать OrderItemDto в gRPC формат
     */
    private ru.dmitartur.common.grpc.OrderItemDto convertOrderItemDtoToGrpc(ru.dmitartur.common.dto.OrderItemDto itemDto) {
        return ru.dmitartur.common.grpc.OrderItemDto.newBuilder()
                .setProductId(itemDto.getProductId() != null ? itemDto.getProductId() : 0)
                .setOfferId(itemDto.getOfferId() != null ? itemDto.getOfferId() : "")
                .setName(itemDto.getName() != null ? itemDto.getName() : "")
                .setQuantity(itemDto.getQuantity() != null ? itemDto.getQuantity() : 0)
                .setPrice(itemDto.getPrice() != null ? itemDto.getPrice().toString() : "0")
                .setSku(itemDto.getSku() != null ? itemDto.getSku() : "")
                .build();
    }
}


