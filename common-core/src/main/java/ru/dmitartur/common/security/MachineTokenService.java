package ru.dmitartur.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.dmitartur.common.config.OAuth2ClientCredentialsProperties;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class MachineTokenService {

    private final OAuth2ClientCredentialsProperties props;

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private volatile Instant expiresAt = Instant.EPOCH;

    public synchronized String getBearerToken() {
        if (Instant.now().isBefore(expiresAt.minusSeconds(30)) && cachedToken.get() != null) {
            return cachedToken.get();
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("scope", props.getScope());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(props.getClientId(), props.getClientSecret());

        var req = new org.springframework.http.HttpEntity<>(form, headers);
        @SuppressWarnings("unchecked")
        Map<String, Object> resp = restTemplate.postForObject(props.getTokenUri(), req, Map.class);
        if (resp == null || !resp.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain machine token");
        }
        String token = (String) resp.get("access_token");
        int expiresIn = ((Number) resp.getOrDefault("expires_in", 300)).intValue();
        this.cachedToken.set(token);
        this.expiresAt = Instant.now().plusSeconds(expiresIn);
        return token;
    }
}
