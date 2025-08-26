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
            @RequestParam String password) {

        try {
            log.info("=== LOGIN REQUEST START ===");
            log.info("Username: {}", username);
            log.info("Password: [HIDDEN]");
            log.info("Request received at: {}", java.time.LocalDateTime.now());
            
            // Получаем client_id и client_secret из конфигурации
            String clientId = "oficiant-client"; // Из конфигурации
            String clientSecret = "oficiant-secret-90489bc550923ed2"; // Из конфигурации
            
            log.info("Using client_id: {}", clientId);
            log.info("Using client_secret: [HIDDEN]");
            
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
            log.info("Looking up client with ID: {}", clientId);
            RegisteredClient registeredClient = clientRepository.findByClientId(clientId);
            if (registeredClient == null) {
                log.error("Client not found: {}", clientId);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "message", "Client not found"
                ));
            }
            log.info("Client found: {}", clientId);

            // Проверяем client_secret (сравниваем с хешированным значением)
            String expectedSecret = registeredClient.getClientSecret();
            log.info("Client secret check - Expected hash: {}, Provided: {}", expectedSecret, clientSecret);
            
            if (!passwordEncoder.matches(clientSecret, expectedSecret)) {
                log.error("Invalid client secret. Expected hash: {}, Provided: {}", expectedSecret, clientSecret);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_client",
                    "message", "Invalid client secret"
                ));
            }
            log.info("Client secret validation successful");

            // Создаем JWT токен с расширенными claim'ами (user_id, email)
            log.info("Creating JWT token for user: {}", username);
            var now = Instant.now();
            var user = userService.findByUsername(username);
            log.info("User found in database - ID: {}, Email: {}", user.getId(), user.getEmail());
            
            var claims = JwtClaimsSet.builder()
                .subject(userAuthentication.getName())
                .claim("client_id", clientId)
                .claim("user_id", String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(18000)) // 300 минут
                .build();

            log.info("JWT claims created - Subject: {}, Client: {}, User ID: {}", 
                    userAuthentication.getName(), clientId, user.getId());

            var jwt = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(claims));
            log.info("JWT token generated successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", jwt.getTokenValue());
            response.put("token_type", "Bearer");
            response.put("expires_in", 18000);
            response.put("user", userAuthentication.getName());
            
            log.info("=== LOGIN REQUEST SUCCESSFUL ===");
            log.info("Response prepared for user: {}", username);
            log.info("Token expires in: {} seconds", 18000);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("=== LOGIN REQUEST FAILED ===");
            log.error("Error during authentication for user: {}", username);
            log.error("Exception type: {}", e.getClass().getSimpleName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace:", e);
            
            return ResponseEntity.badRequest().body(Map.of(
                "error", "authentication_failed",
                "message", e.getMessage()
            ));
        }
    }
} 
