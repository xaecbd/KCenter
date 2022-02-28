package org.nesc.ec.bigdata.controller;

import java.util.*;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.MonitorService;
import org.nesc.ec.bigdata.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.service.HomeService;

import javax.servlet.http.HttpServletRequest;

/**
 * @author lg99
 */
@RestController
@RequestMapping("/home")
public class HomeController extends BaseController{
	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);	
	
	@Autowired
	HomeService homeService;

	@Autowired
	MonitorService monitorService;

	@Autowired
	InitConfig initConfig;

	@Autowired
	RestService restService;

	@Autowired
	HttpServletRequest request;
	/**return the clusterInfo for HomePage*/
	@GetMapping("/page/cluster_statis")
	public RestResponse clusterStatistical() {
		try {
			return SUCCESS_DATA(homeService.clusterStatistical());

		} catch (Exception e) {
			LOG.error("Get Home Page Information error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}

	/**return the cluster size,alert size,topic,group size for homePage*/
	@GetMapping("/page/cluster_info")
	public RestResponse clusterInfo() {
		try {
			return SUCCESS_DATA(homeService.clusterInfo());
		} catch (Exception e) {
			LOG.error("Get Home Page Cluster Info error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}

	/**return the homePage cluster chart data for clusterInfo*/
	@PostMapping("/detail/trend")
	public RestResponse getJmxTrend(@RequestBody Map<String, String> queryMap) {
		try {
			long start = Long.parseLong(queryMap.get(Constants.KeyStr.START));
			long end = Long.parseLong(queryMap.get(Constants.KeyStr.END));
			long clientId = Long.parseLong(queryMap.get(Constants.KeyStr.CLIENTID));
			Map<String,JSONArray> result = homeService.trendClusterData(start, end, clientId);
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOG.error("Get Cluster Metric Ac crossing ES error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}

	/**return the broker metric for cluster*/
	@GetMapping("/detail/metric/{clusterId}")
	public RestResponse getMetric(@PathVariable String clusterId) {
		try {
			return SUCCESS_DATA(monitorService.metrics(clusterId));

		} catch (Exception e) {
			LOG.error("Get Cluster Metric error,",e);
	}
		return ERROR("Get Data Error!Please check");
	}

	/**return the cluster summary chart data*/
	@PostMapping("/cluster/trend")
	public RestResponse clusterCountMetric(@RequestBody Map<String, String> queryMap) {
		try {
			long start = Long.parseLong(queryMap.get(Constants.KeyStr.START));
			long end = Long.parseLong(queryMap.get(Constants.KeyStr.END));
			String interval = queryMap.get("interval");
			Map<String, JSONArray> result = homeService.summatTrend(start, end,interval);
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOG.error("Get Cluster Metric Trend error,",e);
		}
		return ERROR("Get Data Error!Please check");
		
	}

	/**return the cluster summary metric data*/
	@PostMapping("/cluster/metric")
	public RestResponse clusterMetric(@RequestBody Map<String, String> queryMap) {
		try {
			long start = Long.parseLong(queryMap.get(Constants.KeyStr.START));
			long end = Long.parseLong(queryMap.get(Constants.KeyStr.END));
			JSONArray result = homeService.summaryData(start, end);
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOG.error("Get Cluster Metric Trend error,",e);
		}
		return ERROR("Get Data Error!Please check");
		
	}

	/**
	 * return the topic file size grouping by teams search by db
	 * different user roles result in different results
	 * */
	@GetMapping("/topic/file/size")
	@ResponseBody
	public RestResponse topicFileSizeByTeams(){
		UserInfo userInfo = getCurrentUser();
		try{
			Map<String,Long> fileSizeMap = homeService.topicFileSizeGroupByTeams(userInfo);
			return SUCCESS_DATA(fileSizeMap);
		}catch (Exception e){
			LOG.error("get file size by teams has error",e);
		}
		return ERROR("Get file Size has error!");
	}

	/**
	 * return the top of 10 topic log size max search from es
	 * different user roles result in different results
	 * */
	@GetMapping("/topic/log/size")
	@ResponseBody
	public RestResponse top10TopicLogSize(@RequestParam("start")long start,@RequestParam("end") long end){
		UserInfo userInfo = getCurrentUser();
		try{
			List<JSONObject> objectList = homeService.top10TopicLogSizeRang7Days(userInfo,start,end);
			return SUCCESS_DATA(objectList);
		}catch (Exception e){
			LOG.error("get topic log size",e);
		}
		return ERROR("get topic log size has error!");
	}

	/**
	 * return the top of 10 topic file size max search from es
	 * different user roles result in different results
	 * */
	@GetMapping("/topic/top/file/size")
	@ResponseBody
	public RestResponse top10TopicFileSize(@RequestParam("start")long start,@RequestParam("end") long end){
		UserInfo userInfo = getCurrentUser();
		try{
			List<JSONObject> objectList = homeService.top10TopicFileSizeRang7Days(userInfo,start,end);
			return SUCCESS_DATA(objectList);
		}catch (Exception e){
			LOG.error("get top 10 topic file size",e);
		}
		return ERROR("get top 10 topic file size has error!");
	}

	/**
	 * return the top of 10 consumer group alert search from home cache
	 * different user roles result in different results
	 * */
	@GetMapping("/topic/consumer/alert")
	public RestResponse top10ConsumerGroupAlert(@RequestParam(value = "id",required = false) String userId,@RequestParam(value = "role",required = false) String role){
		UserInfo userInfo;
		if(Objects.isNull(userId) || Objects.isNull(role)){
			userInfo = getCurrentUser();
		}else{
			userInfo = new UserInfo();
			userInfo.setId(Long.parseLong(userId));
			userInfo.setRole(RoleEnum.get(Integer.parseInt(role)));
		}
		if(Objects.isNull(userInfo)){
			return SUCCESS_DATA(new ArrayList<>());
		}
		try{
			JSONArray array = new JSONArray();
			Map<String, String> remoteHostsMap = initConfig.getRemoteHostsMap();
			if(initConfig.isRemoteQueryEnable() ){
			    for (String location:remoteHostsMap.keySet()){
					String url = "http://" + remoteHostsMap.get(location)+ "/home/topic/consumer/alert";
					Map<String, String> queryMap = new HashMap<>();
					queryMap.put("id",userInfo.getId().toString());
					queryMap.put("role",String.valueOf(userInfo.getRole().getValue()));
//					array = restService.queryRemoteQueryByGet(url,queryMap);
					array.addAll(restService.queryRemoteQueryByGet(url,queryMap));
				}
			}
			List<HomeCache.ConsumerLagCache> consumerLagCacheList = homeService.topic10ConsumerGroupAlert(userInfo,HomeCache.consumerLagCacheMap);
			if(!array.isEmpty()){
				consumerLagCacheList = top10ConsumerLagStates(array,consumerLagCacheList);
			}
			return SUCCESS_DATA(consumerLagCacheList);
		}catch (Exception e){
			LOG.error("get top 10 topic alert",e.getMessage());
		}
		return ERROR("get top 10 topic alert has error!");
	}

	private List<HomeCache.ConsumerLagCache> top10ConsumerLagStates(JSONArray array, List<HomeCache.ConsumerLagCache> consumerLagCacheList){
		List<HomeCache.ConsumerLagCache> consumerLagCaches = new ArrayList<>();
		for (int i = 0;i<array.size();i++){
			HomeCache.ConsumerLagCache consumerLagCache = JSONObject.parseObject(array.getJSONObject(i).toJSONString(), HomeCache.ConsumerLagCache.class);
			consumerLagCaches.add(consumerLagCache);
		}
		consumerLagCaches.addAll(consumerLagCacheList);
		return  homeService.sortedConsumerLag(consumerLagCaches);

	}




}
