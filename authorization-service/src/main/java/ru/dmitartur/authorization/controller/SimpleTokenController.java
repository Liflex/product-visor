package ru.dmitartur.authorization.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.authorization.service.RateLimitService;
import ru.dmitartur.authorization.service.UserService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class SimpleTokenController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final RegisteredClientRepository clientRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testUser(@RequestParam String username) {
        try {
            var user = userService.findByUsername(username);
            boolean passwordMatches = passwordEncoder.matches("password", user.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("password_matches", passwordMatches);
            response.put("stored_password", user.getPassword());
            response.put("enabled", user.isEnabled());
            response.put("correct_hash", passwordEncoder.encode("password"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestParam String username, @RequestParam String newPassword) {
        try {
            var user = userService.findByUsername(username);
            user.setPassword(passwordEncoder.encode(newPassword));
            userService.createUser(user); // Это создаст нового пользователя, но мы можем использовать другой метод
            
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String client_id,
            @RequestParam String client_secret) {

        try {
            log.info("Attempting authentication for user: {}", username);
            
            // Проверяем rate limit
            if (!rateLimitService.checkRateLimit(username)) {
                return ResponseEntity.status(429).body(Map.of(
                    "error", "rate_limit_exceeded",
                    "message", "Too many requests. Please try again later."
                ));
            }
            
            // Аутентификация пользователя
            UsernamePasswordAuthenticationToken userAuth = new UsernamePasswordAuthenticationToken(username, password);
            Authentication userAuthentication = authenticationManager.authenticate(userAuth);

            if (!userAuthentication.isAuthenticated()) {
                log.error("Authentication failed for user: {}", username);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_credentials",
                    "message", "Invalid username or password"
                ));
            }
            
                        log.info("Authentication successful for user: {}", username);
            
            // Проверяем клиента
            RegisteredClient registeredClient = clientRepository.findByClientId(client_id);
            if (registeredClient == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "message", "Client not found"
                ));
            }

            // Проверяем client_secret (сравниваем с хешированным значением)
            String expectedSecret = registeredClient.getClientSecret();
            if (!passwordEncoder.matches(client_secret, expectedSecret)) {
                log.error("Invalid client secret. Expected: {}, Provided: {}", expectedSecret, client_secret);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "message", "Invalid client secret"
                ));
            }

            // Создаем JWT токен
            var now = Instant.now();
            var claims = JwtClaimsSet.builder()
                .subject(userAuthentication.getName())
                .claim("client_id", client_id)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(18000)) // 300 минут
                .build();

            var jwt = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims));

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", jwt.getTokenValue());
            response.put("token_type", "Bearer");
            response.put("expires_in", 18000);
            response.put("user", userAuthentication.getName());
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "authentication_failed",
                "message", e.getMessage()
            ));
        }
    }
} 
