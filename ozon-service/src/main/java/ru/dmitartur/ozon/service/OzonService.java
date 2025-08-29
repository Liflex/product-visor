package ru.dmitartur.ozon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.common.dto.marketplace.DateRangeDto;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.library.marketplace.service.BaseMarketplaceService;
import ru.dmitartur.ozon.integration.OzonSellerApi;
import ru.dmitartur.ozon.kafka.OrderSyncProducer;
import ru.dmitartur.ozon.mapper.OzonOrderMapper;
import ru.dmitartur.ozon.retry.OzonRetryService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OzonService implements BaseMarketplaceService {
    private final OzonSellerApi api;
    private final OrderSyncProducer orderSyncProducer;
    private final OzonOrderMapper ozonOrderMapper;
    private final OzonRetryService retryService;
    private final ObjectMapper mapper = new ObjectMapper();

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
                    // Отправляем заказы в Kafka как OrderDto без JsonNode
                    orderDtos.forEach(order -> orderSyncProducer.publishOrder(order.getCompanyId().toString(), order));
                    totalUpserted += orderDtos.size();
                    log.info("✅ Published {} FBO orders to Kafka '{}', total published: {}", size, KafkaTopics.ORDER_SYNC_TOPIC, totalUpserted);
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
            // Google Protobuf Timestamp требует формат с миллисекундами и временной зоной
            var filter = req.putObject("filter");
            filter.put("since", formatDateForOzonApi(range.getFrom()));
            filter.put("to", formatDateForOzonApi(range.getTo()));
                
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
                    // Отправляем заказы в Kafka как OrderDto без JsonNode
                    orderDtos.forEach(order -> orderSyncProducer.publishOrder(order.getCompanyId().toString(), order));
                    totalUpserted += orderDtos.size();
                    log.info("✅ Published {} FBS orders to Kafka '{}', total published: {}", size, KafkaTopics.ORDER_SYNC_TOPIC, totalUpserted);
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
     * Комбинированная синхронизация заказов FBO и FBS с Ozon API
     * Сначала синхронизирует FBO, затем FBS заказы
     */
    @Override
    public Integer backfillAllOrders(DateRangeDto range, int pageSize) {
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
            log.info("📦 Step 2: Starting FBS orders synchronization...");
            totalFbsUpserted = backfillFbsOrders(range, pageSize);
            log.info("✅ FBS synchronization completed: {} orders", totalFbsUpserted);
            
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

    // gRPC conversion no longer needed after switching to Kafka
    
    /**
     * Форматирует дату для API Ozon в формате Google Protobuf Timestamp
     * Формат: "2024-01-01T00:00:00.000Z"
     */
    private String formatDateForOzonApi(String dateString) {
        try {
            // Парсим входную дату (предполагаем формат ISO)
            LocalDateTime dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            // Форматируем в формат с миллисекундами и Z (UTC)
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        } catch (Exception e) {
            log.warn("⚠️ Failed to parse date '{}', using as-is: {}", dateString, e.getMessage());
            // Если не удалось распарсить, возвращаем как есть
            return dateString;
        }
    }
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
    
    @Override
    public boolean testConnection() {
        try {
            // Простой тест подключения - попробуем получить список складов
            JsonNode response = api.listWarehouses();
            return response != null && !response.has("error");
        } catch (Exception e) {
            log.error("❌ Connection test failed: {}", e.getMessage());
            return false;
        }
    }
}


