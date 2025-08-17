package ru.dmitartur.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeignMachineTokenInterceptor implements RequestInterceptor {

    private final MachineTokenService machineTokenService;

    @Override
    public void apply(RequestTemplate template) {
        String url = template.url();
        if (url.contains("/internal/")) {
            String token = machineTokenService.getBearerToken();
            template.header("Authorization", "Bearer " + token);
        }
    }
}

