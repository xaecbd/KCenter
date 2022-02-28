package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.nesc.ec.bigdata.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * @author lg99
 */
@Service
public class RestService {
	@Autowired
	RestTemplate restTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(RestService.class);

	public JSONArray queryRemoteQuery(String remote, Map<String, String> queryMap) {
		JSONArray data = new JSONArray();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.APPLICATION_JSON);
			JSONObject obj = tranMapToJson(queryMap);
			HttpEntity<JSONObject> httpEntity = new HttpEntity<>(obj, headers);
			JSONObject responseBody = restTemplate.postForEntity(remote, httpEntity, JSONObject.class).getBody();
			data = responseBody.getJSONArray(Constants.KeyStr.DATA);
		} catch (Exception e) {
			LOG.error("Request Remote Location Has Error,remote:{} queryMap:{}",remote,queryMap.toString(),e);
		}
		return data;
	}

	public JSONArray queryRemoteQueryByGet(String remote,Map<String, String> queryMap){
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remote);
		queryMap.forEach(builder::queryParam);
		JSONObject responseBody = restTemplate.getForEntity(builder.build().encode().toUriString(),JSONObject.class).getBody();
		return responseBody.getJSONArray(Constants.KeyStr.DATA);
	}

	private JSONObject tranMapToJson( Map<String, String> queryMap) {
		JSONObject obj = new JSONObject();
		queryMap.forEach(obj::put);
		return obj;
	}

	public ResponseEntity<String> sendGetOrDeleteRequest(String url, String params,HttpHeaders httpHeaders,HttpMethod httpMethod)throws RestClientException{
		HttpEntity<JSONObject> httpEntity = null;
		ResponseEntity<String> restResponse = null;
		if(Objects.nonNull(httpHeaders)){
			httpEntity = new HttpEntity<>(httpHeaders);
		}
		if(Objects.nonNull(params)){
			restResponse =  restTemplate.exchange(url,httpMethod,httpEntity, String.class,params);
		}else {
			restResponse =  restTemplate.exchange(url,httpMethod,httpEntity, String.class);
		}
		return restResponse;
	}

	public ResponseEntity<String> sendGetRequest(String url, String params,HttpHeaders httpHeaders){
		return sendGetOrDeleteRequest(url,params,httpHeaders,HttpMethod.GET);
	}

	public ResponseEntity<String> sendDeleteRequest(String url, String params,HttpHeaders httpHeaders){
		return sendGetOrDeleteRequest(url,params,httpHeaders,HttpMethod.DELETE);
	}

	public ResponseEntity<String> sendPostOrPutRequest(String url, String params,HttpHeaders httpHeaders,HttpMethod httpMethod){
		ResponseEntity<String> restResponse = null;
		HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSON.parseObject(params), httpHeaders);
		restResponse = restTemplate.exchange(url,httpMethod,httpEntity, String.class,params);
		return restResponse;
	}


	public ResponseEntity<String> sendPostRequest(String url, String params,HttpHeaders httpHeaders){
		return sendPostOrPutRequest(url,params,httpHeaders,HttpMethod.POST);
	}

	public ResponseEntity<String> sendPutRequest(String url, String params,HttpHeaders httpHeaders){
		return sendPostOrPutRequest(url,params,httpHeaders,HttpMethod.PUT);
	}


	public  String generatorUrl(String prefix, String suffix) {
		if (!prefix.startsWith("http:")) {
			prefix = "http://" + prefix;
		}

		if (!prefix.endsWith(suffix)) {
			prefix = prefix + suffix;
		}
		return prefix;
	}
}
