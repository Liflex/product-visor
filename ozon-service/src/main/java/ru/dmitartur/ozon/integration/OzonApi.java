package ru.dmitartur.ozon.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.dmitartur.library.marketplace.integration.BaseMarketplaceApi;

@Slf4j
@Component
@RequiredArgsConstructor
public class OzonApi implements BaseMarketplaceApi {
    
    private final OzonSellerApi ozonSellerApi;
    
    @Override
    public JsonNode listWarehouses() {
        return ozonSellerApi.listWarehouses();
    }
    
    @Override
    public JsonNode getFboOrders(JsonNode request) {
        return ozonSellerApi.fboPostingList(request);
    }
    
    @Override
    public JsonNode getFbsOrders(JsonNode request) {
        return ozonSellerApi.fbsPostingList(request);
    }
    
    @Override
    public JsonNode getFbsOrder(JsonNode request) {
        return ozonSellerApi.fbsPostingGet(request);
    }
    
    public JsonNode updateStock(String offerId, int quantity, String warehouseId) {
        // Создаем запрос для обновления остатков
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var root = mapper.createObjectNode();
        var stocks = mapper.createArrayNode();
        var item = mapper.createObjectNode();
        item.put("offer_id", offerId);
        item.put("stock", Math.max(0, quantity));
        if (warehouseId != null && !warehouseId.isEmpty()) {
            item.put("warehouse_id", warehouseId);
        }
        stocks.add(item);
        root.set("stocks", stocks);
        
        return ozonSellerApi.updateStocks(root);
    }
    
    @Override
    public boolean testConnection() {
        try {
            JsonNode response = listWarehouses();
            return response != null && !response.has("error");
        } catch (Exception e) {
            log.error("❌ Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getMarketplaceName() {
        return "Ozon";
    }
}

