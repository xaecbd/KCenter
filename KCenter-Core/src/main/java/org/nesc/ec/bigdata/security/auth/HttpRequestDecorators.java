package org.nesc.ec.bigdata.security.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * @author lg99
 */
public class HttpRequestDecorators extends HttpRequestWrapper {
    private HttpHeaders httpHeaders;

    private boolean existingHeadersAdded;

    HttpRequestDecorators(HttpRequest request) {
        super(request);
    }
    @Override
    public HttpHeaders getHeaders() {
        if (!existingHeadersAdded) {
            this.httpHeaders = new HttpHeaders();
            httpHeaders.putAll(getRequest().getHeaders());
            existingHeadersAdded = true;
        }
        return httpHeaders;
    }

}
