package ru.dmitartur.yandex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.grpc.OrderInternalServiceGrpc;
import ru.dmitartur.common.grpc.UpsertOrdersRequest;
import ru.dmitartur.common.grpc.UpsertOrdersResponse;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.security.CompanyContextHolder;
import ru.dmitartur.library.marketplace.service.BaseMarketplaceService;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
import ru.dmitartur.yandex.integration.YandexApi;
import ru.dmitartur.yandex.mapper.YandexOrderMapper;
import ru.dmitartur.yandex.retry.YandexRetryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexService implements BaseMarketplaceService {
    
    private final YandexApi yandexApi;
    private final OrderInternalServiceGrpc.OrderInternalServiceBlockingStub orderStub;
    private final YandexOrderMapper yandexOrderMapper;
    private final YandexRetryService retryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${yandex.default-warehouse-id}")
    private String defaultWarehouseId;

    @Override
    public JsonNode fboPostingList(JsonNode req) {
        return retryService.executeWithRetry(
            () -> yandexApi.getFboOrders(req), 
            "fboPostingList"
        );
    }

    @Override
    public JsonNode fbsPostingList(JsonNode req) {
        return retryService.executeWithRetry(
            () -> yandexApi.getFbsOrders(req), 
            "fbsPostingList"
        );
    }

    @Override
    public JsonNode fbsPostingGet(JsonNode req) {
        return retryService.executeWithRetry(
            () -> yandexApi.getFbsOrder(req), 
            "fbsPostingGet"
        );
    }

    @Override
    public JsonNode updateStock(String offerId, int newQuantity, String warehouseId) {
        if (offerId == null || offerId.isEmpty()) {
            throw new IllegalArgumentException("offerId is required");
        }

        if (warehouseId == null || warehouseId.isEmpty()) {
            warehouseId = defaultWarehouseId;
        }

        log.info("🔄 Updating Yandex stock: offerId={}, qty={}, warehouseId={}", 
                offerId, newQuantity, warehouseId);

        JsonNode response = retryService.executeUpdateStocksWithRetry(
            () -> yandexApi.updateStock(offerId, newQuantity, warehouseId), 
            offerId
        );
        
        log.info("✅ Yandex stock updated: offerId={}, response={}", offerId, response.toString());
        return response;
    }

    @Override
    public Integer backfillAllOrders(DateRangeDto range, int pageSize) {
        int totalUpserted = 0;
        String pageToken = "";
        
        log.info("🔄 Starting Yandex orders backfill from {} to {}", range.getFrom(), range.getTo());
        
        while (true) {
            try {
                // Создаем запрос для получения заказов
                ObjectNode request = objectMapper.createObjectNode();
                request.put("pageSize", pageSize);
                request.put("pageToken", pageToken);
                
                // Добавляем фильтр по датам
                ObjectNode filter = request.putObject("filter");
                filter.put("dateFrom", range.getFrom());
                filter.put("dateTo", range.getTo());
                
                JsonNode response = yandexApi.getFboOrders(request);
                
                if (response == null || !response.has("orders")) {
                    log.warn("⚠️ No orders found in response");
                    break;
                }
                
                JsonNode orders = response.get("orders");
                if (orders.size() == 0) {
                    log.info("✅ No more orders to process");
                    break;
                }
                
                // Преобразуем заказы в DTO
                List<OrderDto> orderDtos = yandexOrderMapper.mapOrdersToDto(orders);
                
                if (!orderDtos.isEmpty()) {
                    // Отправляем в gRPC сервис
                    UpsertOrdersRequest.Builder requestBuilder = UpsertOrdersRequest.newBuilder()
                            .addAllOrders(orderDtos.stream()
                                    .map(this::convertOrderDtoToGrpc)
                                    .toList())
                            .setCompanyId(CompanyContextHolder.getCompanyId() != null ? 
                                    CompanyContextHolder.getCompanyId() : "");
                    
                    UpsertOrdersResponse responseGrpc = orderStub.upsertOrders(requestBuilder.build());
                    
                    int upserted = responseGrpc.getUpsertedCount();
                    totalUpserted += upserted;
                    
                    log.info("📦 Processed {} orders, upserted: {}", orders.size(), upserted);
                }
                
                // Проверяем, есть ли следующая страница
                if (response.has("pager") && response.get("pager").has("nextPageToken")) {
                    pageToken = response.get("pager").get("nextPageToken").asText();
                    if (pageToken.isEmpty()) {
                        break;
                    }
                } else {
                    break;
                }
                
            } catch (Exception e) {
                log.error("❌ Error during orders backfill: {}", e.getMessage(), e);
                break;
            }
        }
        
        log.info("🎯 Yandex orders backfill finished: {} orders processed", totalUpserted);
        return totalUpserted;
    }

    @Override
    public String getMarketplaceName() {
        return "Yandex";
    }

    @Override
    public boolean testConnection() {
        try {
            JsonNode response = yandexApi.listWarehouses();
            return response != null && !response.has("error");
        } catch (Exception e) {
            log.error("❌ Connection test failed: {}", e.getMessage());
            return false;
        }
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
