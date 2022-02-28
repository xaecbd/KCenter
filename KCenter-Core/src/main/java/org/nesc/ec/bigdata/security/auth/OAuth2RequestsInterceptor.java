package org.nesc.ec.bigdata.security.auth;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author lg99
 */
public class OAuth2RequestsInterceptor implements ClientHttpRequestInterceptor {

    private final String accessToken;

    public OAuth2RequestsInterceptor(String accessToken) {
        this.accessToken = accessToken;

    }
    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpRequest protectedResourceRequest = new HttpRequestDecorators(request);
        protectedResourceRequest.getHeaders().set("Authorization", "Bearer " + accessToken);
        return execution.execute(protectedResourceRequest, body);
    }

}