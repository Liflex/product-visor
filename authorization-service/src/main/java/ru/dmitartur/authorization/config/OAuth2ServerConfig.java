package ru.dmitartur.authorization.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

/**
 * Конфигурация OAuth2 Authorization Server.
 *
 * Содержит три цепочки безопасности (SecurityFilterChain):
 * 1) authorizationServerSecurityFilterChain — включает стандартную защиту Spring Authorization Server,
 *    настраивает OIDC, точку выдачи токенов и подключает кастомный password-grant конвертер/провайдер.
 * 2) apiSecurityFilterChain — политика безопасности для внутренних REST-эндпоинтов сервиса (URI, начинающиеся с /api/...).
 * 3) defaultSecurityFilterChain — политика по умолчанию для прочих эндпоинтов.
 *
 * Также определяет ключевые бины: PasswordEncoder, RegisteredClientRepository (in-memory для dev),
 * JWK/JWT (encoder/decoder), настройки сервера авторизации и AuthenticationManager.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Primary
public class OAuth2ServerConfig {

    @Value("${rsa.private-key}")
    private String privateKeyPem;

    @Value("${rsa.public-key}")
    private String publicKeyPem;

    @Value("${oauth2.client.id}")
    private String clientId;

    @Value("${oauth2.client.secret}")
    private String clientSecret;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults()); // Enable OpenID Connect 1.0

        http
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2)
    /**
     * Цепочка безопасности для внутренних REST API (`/api/**`).
     *
     * - Отключает CSRF для REST
     * - Запрещает formLogin/httpBasic
     * - Требует аутентификацию JWT для всех запросов, кроме явно разрешённых (`/api/auth/**`).
     */
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/api/auth/**").permitAll()
                                .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    @Order(3)
    /**
     * Базовая цепочка безопасности по умолчанию для прочих запросов.
     * Оставлена для совместимости и простоты отладки (formLogin включён по умолчанию).
     */
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    /**
     * Алгоритм хеширования паролей пользователей и клиентских секретов.
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    /**
     * Репозиторий клиентов OAuth2 (In-Memory, подходит для dev и тестов).
     * Для продакшена рекомендуется JDBC-вариант (см. `OAuth2JdbcConfig`).
     *
     * Здесь же задаются:
     * - поддерживаемые grant types (authorization_code, refresh_token, client_credentials, password)
     * - redirect URI для интерактивного клиента
     * - scopes (включая offline_access для получения refresh_token)
     * - TTL токенов
     */
    public RegisteredClientRepository registeredClientRepository() {
        System.out.println("=== OAuth2 Client Configuration ===");
        System.out.println("Client ID: " + clientId);
        System.out.println("Client Secret Length: " + clientSecret.length());
        System.out.println("================================");
        
        // Хешируем пароль клиента
        String hashedSecret = passwordEncoder().encode(clientSecret);
        
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                .clientSecret(hashedSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .authorizationGrantType(AuthorizationGrantType.PASSWORD)

                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
                .redirectUri("http://localhost:8080/login/oauth2/code/oidc-client")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .redirectUri("http://localhost:8080/authorized")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read")
                .scope("write")
                .scope("internal")
                .scope("offline_access")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)
                        .requireProofKey(false)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(30))
                        .refreshTokenTimeToLive(Duration.ofDays(1))
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    @Bean
    /**
     * Источник JWK на основе RSA-ключей из application.yml.
     * Используется для подписи (encoder) и валидации (decoder) JWT.
     */
    public JWKSource<SecurityContext> jwkSource() {
        try {
            // Очищаем ключи от форматирования
            String cleanPrivateKey = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            
            String cleanPublicKey = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Декодируем ключи
            byte[] privateKeyBytes = Base64.getDecoder().decode(cleanPrivateKey);
            byte[] publicKeyBytes = Base64.getDecoder().decode(cleanPublicKey);

            // Создаем KeyFactory
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // Создаем приватный ключ
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            // Создаем публичный ключ
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            // Создаем RSAKey для JWK
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build();

            JWKSet jwkSet = new JWKSet(rsaKey);
            return new ImmutableJWKSet<>(jwkSet);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания JWKSource", e);
        }
    }

    @Bean
    /**
     * Декодер JWT, использующий JWK.
     */
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    /**
     * Энкодер JWT, использующий JWK.
     */
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    /**
     * Настройки Authorization Server (issuer, endpoints).
     * Оставлены по умолчанию; при необходимости можно задать issuer.
     */
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    /**
     * Достаёт AuthenticationManager, настроенный Spring Security (DAO провайдер и пр.).
     * Нужен для password grant — чтобы провалидировать user credentials.
     */
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
} 