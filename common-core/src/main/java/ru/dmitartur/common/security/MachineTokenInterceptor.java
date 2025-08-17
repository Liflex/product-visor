package ru.dmitartur.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MachineTokenInterceptor implements ClientHttpRequestInterceptor {

    private final MachineTokenService tokenService;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uri = request.getURI().toString();
        if (uri.contains("/internal/")) {
            var headers = request.getHeaders();
            headers.setBearerAuth(tokenService.getBearerToken());
        }
        return execution.execute(request, body);
    }
}

