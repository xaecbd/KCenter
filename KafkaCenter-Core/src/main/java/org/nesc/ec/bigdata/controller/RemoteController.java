package org.nesc.ec.bigdata.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;

@RestController
@RequestMapping("/remote")
public class RemoteController extends BaseController{

	@Autowired
	RestService restService;
	
	@Autowired
	InitConfig config;
	
	@Autowired
	HttpServletRequest request;
	
	@RequestMapping(value = "/topic/consumer_offsets", method = RequestMethod.POST)
	public RestResponse topicGroup(@RequestBody Map<String, String> queryMap) {
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		JSONArray arrays = new JSONArray();
		remoteHostsMap.forEach((k,v)->{
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH +v+request.getServletPath()
					.replace(Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE, Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR);
			JSONArray array = restService.queryRemoteQuery(url,queryMap);
			if(!array.isEmpty()) {
				arrays.addAll(array);
			}			
		});
		return SUCCESS_DATA(arrays);
	}
	
	@RequestMapping(value = "/topic/consumer_offsets/topic_metric", method = RequestMethod.POST)
	public RestResponse topicMetric(@RequestBody Map<String, String> queryMap) {
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		JSONArray arrays = new JSONArray();
		remoteHostsMap.forEach((k,v)->{
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH +v+request.getServletPath().
					replace(Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE, Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR);
			JSONArray array = restService.queryRemoteQuery(url,queryMap);
			if(!array.isEmpty()) {
				arrays.addAll(array);
			}	
		});
		return SUCCESS_DATA(arrays);
	}
	
	@RequestMapping(value = "/group/detail", method = RequestMethod.POST)
	public RestResponse grouptopic(@RequestBody Map<String, String> queryMap) {
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		JSONArray arrays = new JSONArray();
		remoteHostsMap.forEach((k,v)->{
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+v+request.getServletPath()
					.replace(Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE, Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR);
			JSONArray array = restService.queryRemoteQuery(url,queryMap);
			if(!array.isEmpty()) {
				arrays.addAll(array);
			}	
		});
		return SUCCESS_DATA(arrays);
	}
	
	@PostMapping("/alert/group")
	public RestResponse getGroupByCluster(@RequestBody Map<String, String> queryMap) {
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		JSONArray arrays = new JSONArray();
		remoteHostsMap.forEach((k,v)->{
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+v+request.getServletPath().
					replace(Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE, Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR);
			JSONArray array = restService.queryRemoteQuery(url,queryMap);
			if(!array.isEmpty()) {
				arrays.addAll(array);
			}			
		});
		return SUCCESS_DATA(arrays);
	}
}
