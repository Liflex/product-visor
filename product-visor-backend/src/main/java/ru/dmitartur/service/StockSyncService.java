package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.dmitartur.common.kafka.KafkaTopics;
import ru.dmitartur.entity.ProductStock;
import ru.dmitartur.common.dto.marketplace.StockItemReqest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockSyncService {
    
    @Qualifier("jsonKafkaTemplate")
    private final KafkaTemplate<String, Object> kafkaTemplate;


    /**
     * Отправить запрос на синхронизацию остатков для конкретного маркетплейса
     */
    public void syncStocksToMarketplace(List<ProductStock> stock) {

        List<StockItemReqest> items = stock.stream()
                .map(this::convertProductToStockItem)
                .toList();


        items.forEach(item -> {
            kafkaTemplate.send(KafkaTopics.STOCK_SYNC_TOPIC, item)
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            log.error("❌ Failed to send stock productId {} sync request: {}", item.getProductId() ,throwable.getMessage());
                        } else {
                            log.info("✅ Stock sync request sent successfully to product id {} partition: {}, offset: {}",
                                    item.getProductId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                        }
                    });
        });
    }
    
    /**
     * Конвертировать Product в StockItem
     */
    private StockItemReqest convertProductToStockItem(ProductStock stock) {
        return StockItemReqest.builder()
                .offerId(stock.getProduct().getArticle()) // используем article как offerId
                .quantity(stock.getQuantity())
                .productId(stock.getProduct().getId().toString())
                .warehouses(convertWarehouseToSyncInfo(stock))
                .build();
    }
    
    /**
     * Конвертировать Warehouse в WarehouseSyncInfo
     */
    private List<StockItemReqest.WarehouseSyncInfo> convertWarehouseToSyncInfo(ProductStock stock) {
        return stock.getWarehouses().stream().map(warehouse -> StockItemReqest.WarehouseSyncInfo.builder()
                .warehouseId(warehouse.getId().toString())
                .companyId(warehouse.getCompanyId())
                .marketplace(warehouse.getMarketplace().name())
                .warehouseType(warehouse.getWarehouseType().name())
                .build()).toList();
    }
}
