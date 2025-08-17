package ru.dmitartur.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientMachineTokenFilter {

    private final MachineTokenService machineTokenService;

    @Bean
    public ExchangeFilterFunction machineTokenExchangeFilter() {
        return (request, next) -> {
            String url = request.url().toString();
            if (url.contains("/internal/")) {
                String token = machineTokenService.getBearerToken();
                return next.exchange(
                        ClientRequest.from(request)
                                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                                .build()
                );
            }
            return next.exchange(request);
        };
    }

    @Bean
    public WebClient webClient(ExchangeFilterFunction machineTokenExchangeFilter) {
        return WebClient.builder()
                .filter(machineTokenExchangeFilter)
                .build();
    }
}

