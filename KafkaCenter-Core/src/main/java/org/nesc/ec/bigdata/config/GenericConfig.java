package org.nesc.ec.bigdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author lg99
 */
@Configuration
public class GenericConfig {
    @Value("${generic.auth_url:}")
    private String authUrl;
    @Value("${generic.token_url:}")
    private String tokenUrl;
    @Value("${generic.api_url:}")
    private String apiUrl;
    @Value("${generic.name:generic}")
    private String name;
    @Value("${generic.enabled:false}")
    private Boolean enabled;
    @Value("${generic.client_id:}")
    private String clientId;
    @Value("${generic.client_secret:}")
    private String clientSecret;
    @Value("${generic.scopes:}")
    private String scopes;
    @Value("${generic.redirect_utl:}")
    private String redirectUrl;

    public String getScopes() {
        return scopes;
    }
    public String getAuthUrl() {
        return authUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

}
