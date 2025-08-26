package ru.dmitartur.ozon.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ru.dmitartur.library.marketplace.integration.BaseMarketplaceApi;
import ru.dmitartur.ozon.config.FeignOzonConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ozonSellerApi", url = "${ozon.base-url}", configuration = FeignOzonConfig.class)
public interface OzonSellerApi {
    @PostMapping("/v2/posting/fbo/list")
    JsonNode fboPostingList(@RequestBody JsonNode request);
    
    @PostMapping("/v3/posting/fbs/list")
    JsonNode fbsPostingList(@RequestBody JsonNode request);
    
    @PostMapping("/v3/posting/fbs/get")
    JsonNode fbsPostingGet(@RequestBody JsonNode request);

    // Stocks
    // https://docs.ozon.ru/api/seller/#operation/ProductAPI_UpdateStocksV2
    @PostMapping("/v2/products/stocks")
    JsonNode updateStocks(@RequestBody JsonNode request);

    // Warehouses list (to resolve warehouse_id if needed)
    @PostMapping("/v1/warehouse/list")
    JsonNode listWarehouses();
}


