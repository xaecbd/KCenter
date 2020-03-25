package org.nesc.ec.bigdata.security.auth;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.config.GenericConfig;
import org.nesc.ec.bigdata.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lg99
 */
@Component
public class GenericOauthService{

    @Autowired
    GenericConfig genericConfig;

    @Autowired
    RestTemplate restTemplate;

    public String createAuthorURL(){
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(genericConfig.getAuthUrl());
        builder.queryParam(OAuth2ParameterName.CLIENT_ID,genericConfig.getClientId());
        builder.queryParam(OAuth2ParameterName.CLIENT_SECRET,genericConfig.getClientSecret());
        builder.queryParam(OAuth2ParameterName.RESPONSE_TYPE, OAuth2ParameterName.CODE);
        builder.queryParam(OAuth2ParameterName.REDIRECT_URI,genericConfig.getRedirctUrl());
        return builder.toUriString();
    }


    public UserInfo getUser(String code){
        String accessToken = getAccessToken(code);
        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromHttpUrl(genericConfig.getApiUrl());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(accessToken);
        String object = getRestTemplate(accessToken).getForObject(componentsBuilder.toUriString(),String.class,httpHeaders);
        return fetchData(JSONObject.parseObject(object));
    }

    private ClientHttpRequestInterceptor interceptor(String accessToken) {
        return new OAuth2RequestsInterceptor(accessToken);
    }

    private String getAccessToken(String code){
        TokenGrant tokenGrant = exExpresss(code);
        return tokenGrant.getAccessToken();
    }

    private RestTemplate getRestTemplate(String accessToken){
        List<ClientHttpRequestInterceptor> interceptors = new LinkedList<>();
        interceptors.add(interceptor(accessToken));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private TokenGrant exExpresss(String code){
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(genericConfig.getTokenUrl());
        MultiValueMap<String,Object> map = new LinkedMultiValueMap<>();
        map.add(OAuth2ParameterName.GRANT_TYPE,OAuth2ParameterName.AUTHORIZATION_CODE);
        map.add(OAuth2ParameterName.CODE,code);
        map.add(OAuth2ParameterName.CLIENT_ID,genericConfig.getClientId());
        map.add(OAuth2ParameterName.CLIENT_SECRET,genericConfig.getClientSecret());
        map.add(OAuth2ParameterName.REDIRECT_URI,genericConfig.getRedirctUrl());
        uriComponentsBuilder.queryParam(OAuth2ParameterName.GRANT_TYPE, OAuth2ParameterName.AUTHORIZATION_CODE);
        uriComponentsBuilder.queryParam(OAuth2ParameterName.CODE,code);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, OAuth2ParameterName.FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(map, headers);
        String body =  restTemplate.postForEntity(uriComponentsBuilder.toUriString(),httpEntity,String.class).getBody();
        return JSONObject.parseObject(body, TokenGrant.class);
    }

    private UserInfo fetchData(JSONObject json){
        UserInfo userInfo = new UserInfo();
        String preName = "";
        String[] names = new String[]{"user_name","userName","user","UserName","name"};
        for(String name:names){
            if(json.containsKey(name)){
                preName = json.getString(name);
            }
        }
        userInfo.setName(preName);
        String realName = "";
        String[] realNames = new String[]{"DisplayName","display_name","dis_name","realName","RealName","real_name"};
        for(String name:realNames){
            if(json.containsKey(name)){
                realName = json.getString(name);
            }
        }
        userInfo.setRealName(realName);

        String email = "";
        String[] emails = new String[]{"email","Email"};
        for(String emailAddress:emails){
            if(json.containsKey(emailAddress)){
                email = json.getString(emailAddress);
            }
        }
        userInfo.setEmail(email);

        String picture="";
        String[] pictures = new String[]{"picture","photo","PHOTO","PICTURE","Picture","Photo"};
        for (String photo:pictures){
            if(json.containsKey(photo)){
                picture = json.getString(photo);
            }
        }
        userInfo.setPicture(picture);
        return userInfo;
    }

    public boolean isEnable(){
        return genericConfig.getEnabled();
    }

    public String serviceName(){
        return genericConfig.getName();
    }






}
