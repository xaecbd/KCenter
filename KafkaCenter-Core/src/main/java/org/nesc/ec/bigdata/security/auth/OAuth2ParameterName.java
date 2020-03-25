package org.nesc.ec.bigdata.security.auth;

/**
 * @author lg99
 */
public interface OAuth2ParameterName {
    String GRANT_TYPE = "grant_type";
    String RESPONSE_TYPE = "response_type";
    String CLIENT_ID = "client_id";
    String CLIENT_SECRET = "client_secret";
    String REDIRECT_URI = "redirect_uri";
    String SCOPE = "scope";
    String STATE = "state";
    String CODE = "code";
    String ACCESS_TOKEN = "access_token";
    String TOKEN_TYPE = "token_type";
    String EXPIRES_IN = "expires_in";
    String REFRESH_TOKEN = "refresh_token";
    String ERROR = "error";
    String ERROR_DESCRIPTION = "error_description";
    String ERROR_URI = "error_uri";
    String REGISTRATION_ID = "registration_id";
    String AUTHORIZATION_CODE = "authorization_code";
    String FORM_URLENCODED= "application/x-www-form-urlencoded";
}
