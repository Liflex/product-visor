package ru.dmitartur.ozon.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignOzonConfig {
    @Bean
    public RequestInterceptor ozonAuthHeadersInterceptor(OzonProperties properties) {
        return tpl -> {
            tpl.header("Client-Id", properties.getClientId());
            tpl.header("Api-Key", properties.getApiKey());
            tpl.header("Content-Type", "application/json");
        };
    }
}


