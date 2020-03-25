package org.nesc.ec.bigdata.controller;

import java.util.Map;

import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.service.HomeService;

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
	
	
	@GetMapping("/page/cluster_statis")
	public RestResponse clusterStatistical() {
		try {
			return SUCCESS_DATA(homeService.clusterStatistical());

		} catch (Exception e) {
			LOG.error("Get Home Page Information error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}
	
	@GetMapping("/page/cluster_info")
	public RestResponse clusterInfo() {
		try {
			return SUCCESS_DATA(homeService.clusterInfo());
		} catch (Exception e) {
			LOG.error("Get Home Page Cluster Info error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}
	
	@PostMapping("/detail/trend")
	public RestResponse getJmxTrend(@RequestBody Map<String, String> queryMap) {
		try {
			long start = Long.parseLong(queryMap.get(Constants.KeyStr.START));
			long end = Long.parseLong(queryMap.get(Constants.KeyStr.END));
			long clientId = Long.parseLong(queryMap.get(Constants.KeyStr.CLIENTID));
			Map<String,JSONArray> result = homeService.trendClusterData(start, end, clientId);
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOG.error("Get Cluster Metric Accrossing ES error,",e);
		}
		return ERROR("Get Data Error!Please check");
	}
	
	@GetMapping("/detail/metric/{clusterId}")
	public RestResponse getMetric(@PathVariable String clusterId) {
		try {
			return SUCCESS_DATA(monitorService.metrics(clusterId));

		} catch (Exception e) {
			LOG.error("Get Cluster Metric error,",e);
	}
		return ERROR("Get Data Error!Please check");
	}
	
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

}
