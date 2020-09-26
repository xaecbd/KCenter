package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.security.auth.GenericOauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lg99
 */
@RestController
@RequestMapping("/config")
public class ConfigController extends BaseController{

	@Autowired
	InitConfig initConfig;

	@Autowired
	GenericOauthService genericOauthService;
	/** return the basic information to UI*/
	@GetMapping("")
	@ResponseBody
	public RestResponse getConfigInfo(){
		String[] locationArr = initConfig.getRemoteLocations().split(Constants.Symbol.COMMA);
		Map<String,Object> map = new HashMap<>();
		JSONArray result = new JSONArray();
		result.addAll(Arrays.asList(locationArr));
		map.put(TopicConfig.TTL, initConfig.getTtl());
		map.put(Constants.KeyStr.REMOTELOCATIONS, result);
		map.put(Constants.KeyStr.CONNECTION_URL, initConfig.getConnectUrl());
		return SUCCESS_DATA(map);
	}

	/** return the oauth2 config*/
	@GetMapping("/oauth2")
	@ResponseBody
	public Object oauth2Config(){
		Map<String,Object> map = new HashMap<>();
		map.put("enable",genericOauthService.isEnable());
		map.put("name",genericOauthService.serviceName());
		return map;
	}
}
