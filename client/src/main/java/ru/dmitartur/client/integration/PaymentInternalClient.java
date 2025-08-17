package ru.dmitartur.client.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PaymentInternalClient {

    private final RestTemplate restTemplate;

    @Value("${oficiant.services.payment.base-url:http://localhost:8084}")
    private String paymentBaseUrl;

    /**
     * Инициировать оформление подписки через payment-service (HTTP, internal)
     * Возвращает true, если запрос принят (HTTP 2xx)
     */
    public boolean subscribe(long chatId, int days) {
        String url = paymentBaseUrl + "/internal/payment/subscribe/" + chatId + "?days=" + days;
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(new HttpHeaders()), String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}

