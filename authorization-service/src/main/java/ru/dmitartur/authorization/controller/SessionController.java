package ru.dmitartur.authorization.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.lang.Nullable;
import ru.dmitartur.authorization.service.RedisOAuth2AuthorizationService;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {
    private final OAuth2AuthorizationService primaryAuthService;
    @Nullable
    private final RedisOAuth2AuthorizationService redisAuthService;

    @GetMapping
    public ResponseEntity<?> me(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(auth.getTokenAttributes());
    }

    @DeleteMapping
    public ResponseEntity<Void> logout(JwtAuthenticationToken auth) {
        String tokenValue = auth.getToken().getTokenValue();
        OAuth2Authorization a = primaryAuthService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
        if (a != null) primaryAuthService.remove(a);
        if (redisAuthService != null) {
            OAuth2Authorization cached = redisAuthService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
            if (cached != null) redisAuthService.remove(cached);
        }
        return ResponseEntity.noContent().build();
    }
}


