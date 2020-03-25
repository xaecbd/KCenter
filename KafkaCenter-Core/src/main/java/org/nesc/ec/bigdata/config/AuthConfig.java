package org.nesc.ec.bigdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Reason.H.Duan
 * @version 1.0
 * @date 3/30/2019
 */
@Configuration
public class AuthConfig {

    @Value("${security.oauth2.client.user-authorization-uri:}")
    private String oauthHost;

    public String getOauthHost() {
        return oauthHost;
    }
}
