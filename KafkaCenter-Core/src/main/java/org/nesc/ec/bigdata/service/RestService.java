package org.nesc.ec.bigdata.service;

import java.util.Map;

import org.nesc.ec.bigdata.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

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
			LOG.error("Request Remote Location Has Error,",e);
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
}
