package ru.dmitartur.yandex.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.dmitartur.yandex.integration.YandexAuthInterceptor;
import ru.dmitartur.yandex.integration.YandexErrorDecoder;

@Slf4j
@Configuration
public class FeignYandexConfig {

    @Value("${yandex.client-id}")
    private String clientId;

    @Value("${yandex.client-secret}")
    private String clientSecret;

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor yandexAuthInterceptor() {
        return new YandexAuthInterceptor(clientId, clientSecret);
    }

    @Bean
    public ErrorDecoder yandexErrorDecoder() {
        return new YandexErrorDecoder();
    }
}
