package ru.dmitartur.ozon.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ru.dmitartur.ozon.config.FeignOzonConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ozonSellerApi", url = "${ozon.base-url}", configuration = FeignOzonConfig.class)
public interface OzonSellerApi {
    @PostMapping("/v2/posting/fbo/list")
    JsonNode fboPostingList(@RequestBody JsonNode request);
}


