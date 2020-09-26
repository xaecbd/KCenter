package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.model.GroupTopicConsumerState;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.nesc.ec.bigdata.common.model.TopicConsumerGroupState;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterGroup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorTopic;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午2:39:42
 * @version 1.0 monitor 控制层
 */
@RestController
@RequestMapping("/monitor")
public class MonitorController extends BaseController {	
	private static final Logger LOG = LoggerFactory.getLogger(MonitorController.class);
	@Autowired
	MonitorService monitorService;
	@Autowired
	ClusterService clusterService;
	@Autowired
	InitConfig config;
	@Autowired
	RestService restService;
	@Autowired
	TopicInfoService topicInfoService;

	@Autowired
	CollectionService collectionService;
	@Autowired
	ElasticsearchService elasticsearchService;
	@Autowired
	HttpServletRequest request;


	/**
	 * Monitor模块下producer和consumer获取所有的topic,并且填充该topic是否被收藏。
	 * 该方法返回数据为实时从集群获取
	 * @param clusterId
	 * @return
	 */
	@RequestMapping(value = "/topic", method = RequestMethod.GET)
	public List<MonitorTopic> getTopicList(@RequestParam("cluster") String clusterId) {
		List<MonitorTopic> monitorTopics = new ArrayList<>();
		List<ClusterInfo> clusterInfos = "-1".equalsIgnoreCase(clusterId)?clusterService.getTotalData():
				new ArrayList<ClusterInfo>() {{
					add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));
				}};
		UserInfo user = getCurrentUser();
		monitorTopics = monitorService.getTopicList(clusterInfos,user, Constants.KeyStr.MONITOR_TOPIC);
		return monitorTopics;
	}

	/**
	 * 收藏topic
	 * @param type
	 * @param clusterId
	 * @return
	 */
	@RequestMapping(value = "/favorite", method = RequestMethod.GET)
	@ResponseBody
	public RestResponse getCollectionList(@PathParam(value= Constants.JsonObject.TYPE) String type,@RequestParam("cluster") String clusterId) {
		UserInfo user = getCurrentUser();
		if(Constants.Role.ADMIN.equalsIgnoreCase(user.getName())){
			user.setId(-1L);
		}
		List<MonitorTopic> monitorTopics = "-1".equalsIgnoreCase(clusterId)?monitorService.listUserFavorite(user,type):
					monitorService.listUserFavorite(user,type).stream().filter(topic->{
						return topic.getClusterID().toString().equalsIgnoreCase(clusterId);
					}).collect(Collectors.toList());
		return SUCCESS_DATA(monitorTopics);
	}

	/**
	 * 获取收藏列表
	 * @param name
	 * @param collection
	 * @param clusterId
	 * @param type
	 * @return
	 */
	@GetMapping("/topic/collection")
	@ResponseBody
	public RestResponse collections(@PathParam(value = Constants.JsonObject.NAME) String name, @PathParam(value= Constants.KeyStr.COLLECTION) String collection
			, @PathParam(value= Constants.KeyStr.LOWER_CLUSTER_ID) String clusterId, @PathParam(value=Constants.JsonObject.TYPE) String type) {
		try {
			UserInfo user = getCurrentUser();
			if(Constants.Role.ADMIN.equalsIgnoreCase(user.getName())){
				user.setId(0L);
			}
			boolean isCollection = Boolean.parseBoolean(collection);
			if(isCollection) {
				Map<String,Object> map = new HashMap<>();
				map.put(Constants.JsonObject.NAME, name);
				map.put(Constants.KeyStr.CLUSTER_ID, clusterId);
				map.put(Constants.JsonObject.TYPE, type);
				map.put(Constants.KeyStr.USER_ID, user.getId());
				if(collectionService.deleteByMap(map)) {
					return SUCCESS("Collection  SUCCESS!");
				}			
			}else {
				org.nesc.ec.bigdata.model.Collections xxxx = new org.nesc.ec.bigdata.model.Collections();
				xxxx.setClusterId(Long.parseLong(clusterId));
				xxxx.setUserId(user.getId());
				xxxx.setName(name);
				xxxx.setType(type);
				if(collectionService.insert(xxxx)) {
					return SUCCESS("Collection  SUCCESS!");
				}
			}} catch (Exception e) {
				LOG.error("Collection Topic Failed,message:",e);
				return ERROR("Collection Topic Failed!");
			}
		return ERROR("Collection Topic Failed!");
	}



	@RequestMapping(value = "/topic/consumer_offsets", method = RequestMethod.POST)
	public RestResponse topicGroup(@RequestBody Map<String, String> queryMap) {
		String topic = queryMap.get(BrokerConfig.TOPIC);
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);

		if (StringUtils.isBlank(clusterID) || StringUtils.isBlank(topic)) {
			return ERROR("clusterID and topic must not blank.");
		}
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		ClusterInfo info = clusterService.selectById(Long.parseLong(clusterID));
		// 查看是否启用remote，并且配置了clusterID对应的remoteHost
		if (config.isRemoteQueryEnable() && remoteHostsMap.containsKey(info.getLocation().toLowerCase())) {
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+request.getServerName()+ Constants.Symbol.COLON
						+request.getServerPort()+ Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE+request.getServletPath()
					.replaceAll(Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR, Constants.Symbol.EMPTY_STR);
			JSONArray data = restService.queryRemoteQuery(url, queryMap);
			return SUCCESS_DATA(data);
		} else {
			List<TopicConsumerGroupState> topicGroups = monitorService.describeConsumerGroups(topic, info);
			topicGroups.sort((o1, o2) -> o1.getGroupId().compareToIgnoreCase(o2.getGroupId()));
			return SUCCESS_DATA(topicGroups);
		}

	}


	@RequestMapping(value = "/topic/consumer_offsets/topic_metric", method = RequestMethod.POST)
	public RestResponse topicMetric(@RequestBody Map<String, String> queryMap) {
		String topic = queryMap.get(BrokerConfig.TOPIC);
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);
		if (StringUtils.isBlank(clusterID) || StringUtils.isBlank(topic)) {
			return ERROR("clusterID and topic must not blank.");
		}
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		ClusterInfo info = clusterService.selectById(Long.parseLong(clusterID));
		// 查看是否启用remote，并且配置了clusterID对应的remoteHost
		if (config.isRemoteQueryEnable() && remoteHostsMap.containsKey(info.getLocation().toLowerCase())) {
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+request.getServerName()+ Constants.Symbol.COLON+request.getServerPort()
							+request.getServletPath().replaceAll(Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR, Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE);
			JSONArray data = restService.queryRemoteQuery(url, queryMap);
			return SUCCESS_DATA(data);
		} else {
			Set<MeterMetric> result = monitorService.getBrokerMetric(clusterID,topic);
			if (result == null) {
				return SUCCESS("please confirm your jmx enable already open!");
			}
			return SUCCESS_DATA(result);
		}

	}

	/**return the topic metric chart data*/
	@GetMapping("/topic/metric")
	public RestResponse topicMetricTrend(@RequestParam(value = "clusterId")String clusterId,@RequestParam(value = "topic") String topic,@RequestParam(value = "metric")String metricName,
										 @RequestParam(value = "start")String start,@RequestParam(value = "end")String end){
		List<JSONObject> result = elasticsearchService.getTopicMetric(Long.parseLong(start),Long.parseLong(end),clusterId,topic,metricName);
		return  SUCCESS_DATA(result);

	}

	/**return the topic fileSize and logSize*/
	@GetMapping("/topic/offset")
	public RestResponse  topicOffsetAndLogSize(@RequestParam(value = "clusterId")String clusterId,@RequestParam(value = "topic") String topic){
	   return SUCCESS_DATA(monitorService.selectTopicLogSizeAndOffset(clusterId,topic));
	}

	/**
	 * 获取group历史消费指标
	 */
	@PostMapping("/topic/consumer_offsets/chart")
	public RestResponse getOffsetStats(@RequestBody Map<String, String> queryMap) {
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);
		String topic = queryMap.get(BrokerConfig.TOPIC);
		String group = queryMap.get(BrokerConfig.GROUP);
		String type = queryMap.get(Constants.JsonObject.TYPE);
		if (StringUtils.isBlank(clusterID) || StringUtils.isBlank(topic) || StringUtils.isBlank(group)
				|| StringUtils.isBlank(type)) {
			return ERROR("clusterID topic group and type must not blank.");
		}
		String start = queryMap.get("start");
		String end = queryMap.get("end");
		return SUCCESS_DATA(elasticsearchService.queryOffset(clusterID, topic, group, type, start, end));
	}

	/**
	 * 获取group历史消费指标,指定时间窗口
	 */
	@PostMapping("/topic/consumer_offsets/chart/interval")
	public RestResponse getOffsetStatsInterval(@RequestBody Map<String, String> queryMap) {
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);
		String topic = queryMap.get(BrokerConfig.TOPIC);
		String group = queryMap.get(BrokerConfig.GROUP);
		String type = queryMap.get(Constants.JsonObject.TYPE);
		if (StringUtils.isBlank(clusterID) || StringUtils.isBlank(topic) || StringUtils.isBlank(group)
				|| StringUtils.isBlank(type)) {
			return ERROR("clusterID topic group and type must not blank.");
		}
		String start = queryMap.get(Constants.KeyStr.START);
		String end = queryMap.get(Constants.KeyStr.END);
		String interval = queryMap.get(Constants.KeyStr.INTERVAL);
		return SUCCESS_DATA(
				elasticsearchService.queryDateIntervalOffset(clusterID, topic, group, type, start, end, interval));
	}

	@GetMapping("/lag")
	public RestResponse getGroupLag(@RequestParam("cluster") String clusterId) {
		List<ClusterInfo> clusters = "-1".equalsIgnoreCase(clusterId)? clusterService.getTotalData():
				new ArrayList<ClusterInfo>(){{ add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));}};
		JSONArray res = new JSONArray();
		clusters.forEach(cluster->{
			JSONArray list = monitorService.getGroupLag(cluster.getId(), cluster.getName());
			if(!list.isEmpty()) {
				res.addAll(list);
			}			

		});
		List<JSONObject> list = JSONArray.parseArray(res.toJSONString(), JSONObject.class);
		list.sort((o1, o2) -> Long.compare(o2.getLongValue(TopicConfig.LAG), o1.getLongValue(TopicConfig.LAG)));
		return SUCCESS_DATA(list);
	}


	/**
	 * 查询所有集群的所有group信息
 	 */
	@RequestMapping(value = "/group", method = RequestMethod.GET)
	public RestResponse getClusterAllGroup(@RequestParam("cluster") String clusterId) {
		List<ClusterInfo> clusters = "-1".equalsIgnoreCase(clusterId)?clusterService.getTotalData():
				new ArrayList<ClusterInfo>(){{ add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));}};
		List<ClusterGroup> clusterGroups;
		try {
			clusterGroups = monitorService.listGroupsByCluster(clusters, true);

		} catch (Exception e) {
			LOG.error("List Group Error,message:",e);
			return ERROR("get all cluster group has error");
		} 
		return SUCCESS_DATA(clusterGroups);
	}
	
	/**
	 * 
	 * 根据group查询相关topic消费情况
	 * 
	 * @param queryMap
	 * @return
	 */
	@RequestMapping(value = "/group/detail", method = RequestMethod.POST)
	public RestResponse groupTopic(@RequestBody Map<String, String> queryMap) {
		String consummerGroup = queryMap.get(TopicConfig.CONSUMMERGROUP);
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);
		
		if (StringUtils.isBlank(clusterID) || StringUtils.isBlank(consummerGroup)) {
			return ERROR("clusterID and consumerGroup must not blank.");
		}
		
		List<GroupTopicConsumerState> topicGroups = new ArrayList<>();
		Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
		ClusterInfo info = clusterService.selectById(Long.parseLong(clusterID));		
		// 查看是否启用remote，并且配置了clusterID对应的remoteHost
		if (config.isRemoteQueryEnable() && remoteHostsMap.containsKey(info.getLocation().toLowerCase())) {
			String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+request.getServerName()+ Constants.Symbol.COLON+request.getServerPort()
							+request.getServletPath().replaceAll(Constants.Symbol.SLASH+ Constants.KeyStr.MONITOR, Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE);
			JSONArray data = restService.queryRemoteQuery(url, queryMap);
			return SUCCESS_DATA(data);
		} else {
			try {
				topicGroups = monitorService.describeConsumerGroupByGroup(consummerGroup,info);
			} catch (Exception e) {
				LOG.error("Get topic consumer By Group Error,msg:",e);
				return ERROR("get topic consumer status by group has error.");
			} 
			return SUCCESS_DATA(topicGroups);
		}
	}
}
