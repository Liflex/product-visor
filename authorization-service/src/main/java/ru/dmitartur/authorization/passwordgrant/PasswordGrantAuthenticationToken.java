package ru.dmitartur.authorization.passwordgrant;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;

import java.util.Collections;
import java.util.Map;

public class PasswordGrantAuthenticationToken extends AbstractAuthenticationToken {
    private final String username;
    private final String password;
    private final OAuth2ClientAuthenticationToken clientPrincipal;
    private final Map<String, Object> additionalParameters;

    public PasswordGrantAuthenticationToken(String username,
                                            String password,
                                            OAuth2ClientAuthenticationToken clientPrincipal,
                                            Map<String, Object> additionalParameters) {
        super(Collections.emptyList());
        this.username = username;
        this.password = password;
        this.clientPrincipal = clientPrincipal;
        this.additionalParameters = additionalParameters;
        setAuthenticated(false);
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public OAuth2ClientAuthenticationToken getClientPrincipal() { return clientPrincipal; }
    public Map<String, Object> getAdditionalParameters() { return additionalParameters; }

    @Override
    public Object getCredentials() { return password; }

    @Override
    public Object getPrincipal() { return username; }
}



