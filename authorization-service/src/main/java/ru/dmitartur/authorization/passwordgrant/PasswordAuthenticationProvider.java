package ru.dmitartur.authorization.passwordgrant;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Set;

@RequiredArgsConstructor
public class PasswordAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {
    private final AuthenticationManager authenticationManager;
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2RefreshToken> tokenGenerator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordGrantAuthenticationToken passwordToken = (PasswordGrantAuthenticationToken) authentication;
        Authentication userAuth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(passwordToken.getUsername(), passwordToken.getPassword())
        );

        RegisteredClient registeredClient = passwordToken.getClientPrincipal().getRegisteredClient();
        Set<String> authorizedScopes = registeredClient.getScopes();

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userAuth.getName())
                .authorizationGrantType(new AuthorizationGrantType("password"));

        OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(userAuth)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizedScopes(authorizedScopes)
                .build();

        OAuth2Token accessToken = tokenGenerator.generate(accessTokenContext);
        if (!(accessToken instanceof OAuth2AccessToken oAuth2AccessToken)) {
            throw new IllegalStateException("Failed to generate access token");
        }
        authorizationBuilder.accessToken(oAuth2AccessToken);

        // Optionally issue refresh token if client supports it
        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().stream().anyMatch(gt -> gt.getValue().equals("refresh_token"))) {
            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(userAuth)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .authorizedScopes(authorizedScopes)
                    .build();
            refreshToken = tokenGenerator.generate(refreshTokenContext);
            if (refreshToken == null) {
                throw new IllegalStateException("Failed to generate refresh token");
            }
            authorizationBuilder.refreshToken(refreshToken);
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        authorizationService.save(authorization);

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, passwordToken.getClientPrincipal(), (OAuth2AccessToken) accessToken, refreshToken, null
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}


