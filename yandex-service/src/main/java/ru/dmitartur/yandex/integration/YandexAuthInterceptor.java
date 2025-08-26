package ru.dmitartur.yandex.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

@Slf4j
@Component
public class YandexAuthInterceptor implements ClientHttpRequestInterceptor {
    
    private String accessToken;
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        log.debug("🔑 Yandex access token set");
    }
    
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) 
            throws IOException {
        
        if (accessToken != null && !accessToken.isEmpty()) {
            request.getHeaders().setBearerAuth(accessToken);
            log.debug("🔑 Added Bearer token to request: {}", request.getURI());
        } else {
            log.warn("⚠️ No access token available for Yandex request: {}", request.getURI());
        }
        
        return execution.execute(request, body);
    }
}
