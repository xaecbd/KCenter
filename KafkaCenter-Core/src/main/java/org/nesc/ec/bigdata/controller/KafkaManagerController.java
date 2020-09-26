package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterGroup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.KafkaManagerBroker;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manager")
public class KafkaManagerController extends BaseController{

    private static final Logger LOG = LoggerFactory.getLogger(KafkaManagerController.class);

    @Autowired
    TopicInfoService topicInfoService;
    @Autowired
    KafkaManagerService kafkaManagerService;
    @Autowired
    MonitorService monitorService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    AlertService alertService;

    @Autowired
    InitConfig config;

    @Autowired
    HttpServletRequest request;

    @Autowired
    RestService restService;

    /**return the topic list according to clusterId*/
    @GetMapping("/topic/list")
    public RestResponse topicList(@RequestParam("cluster") String clusterId) {
        try {
            return SUCCESS_DATA(kafkaManagerService.topicList(clusterId));
        } catch (Exception e) {
            LOG.error("Get Topic Config Error,",e);
        }
        return ERROR("Get Topic Config Error!Please check");
    }
    /**return the topic config according to clusterId and topicName*/
    @PostMapping("/topic/config")
    public RestResponse topicConfig(@RequestBody Map<String,String> queryMap) {
        try {
            Map<String,JSONArray> map = new HashMap<>();
            String clusterId = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = queryMap.get(Constants.KeyStr.TOPICNAME);
            map.put("config",kafkaManagerService.topicConfig(clusterId, topicName));
            return SUCCESS_DATA(map);
        } catch (Exception e) {
            LOG.error("Get Topic Config Error,",e);
        }
        return ERROR("Get Topic Config Error!Please check");
    }

    /**return the topic information summary information such as partitions,broker and some config information
     * through the clusterId
     * and topicName
     * */
    @PostMapping("/topic/summary")
    public RestResponse topicSummary(@RequestBody Map<String,String> queryMap) {
        try {
            String clusterId = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = queryMap.get(Constants.KeyStr.TOPICNAME);
            return SUCCESS_DATA(kafkaManagerService.topicAndPartition(clusterId, topicName));
        } catch (Exception e) {
            LOG.error("Get Topic Summary Error,",e);
        }
        return ERROR("Get Topic Summary Error!Please check");
    }

    /**delete the topic through the clusterId and topicName*/
    @PostMapping("/delete/topic")
    public RestResponse deleteTopic(@RequestBody Map<String,String> queryMap) {
        try {
            String clusterId = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = queryMap.get(Constants.KeyStr.TOPICNAME);
            if(kafkaManagerService.deleteTopic(clusterId, topicName)) {
                return SUCCESS("Delete Topic Success");
            }
        } catch (Exception e) {
            LOG.error("Delete Topic failed,",e);
        }
        return ERROR("Delete Topic failed!");
    }

    /**add the partitions to specified topic */
    @PostMapping("/topic/partition")
    public RestResponse addPartition(@RequestBody Map<String,String> queryMap) {
        try {
            String clusterId = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = queryMap.get(Constants.KeyStr.TOPICNAME);
            String partition = queryMap.get(TopicConfig.PARTITION);
            String partitions = queryMap.get(TopicConfig.PARTITIONS);
            String oldPartition = queryMap.get(TopicConfig.OLDPARTITIONS);
            if(kafkaManagerService.addPartition(clusterId, topicName, Integer.parseInt(partition),partitions,Integer.parseInt(oldPartition))) {
                return SUCCESS("Add Partitions Success");
            }
        } catch (Exception e) {
            LOG.error("Add Partitions failed,",e);
        }
        return ERROR("Add Partitions failed!");
    }

    /**return the topic config to specified topic */
    @PostMapping("/topic/desconfig")
    public RestResponse descrConfig(@RequestBody Map<String,String> queryMap) {
        try {
            String clusterId = queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = queryMap.get(Constants.KeyStr.TOPICNAME);
            return SUCCESS_DATA(kafkaManagerService.descConfig(clusterId, topicName));
        } catch (Exception e) {
            LOG.error("Describe Topic Config failed,",e);
        }
        return ERROR("Describe Topic Config failed!");
    }

    /**update the topic config to specified topic */
    @PostMapping("/topic/desconfig/update")
    public RestResponse updateConfig(@RequestBody Map<String,Object> queryMap) {
        try {
            String clusterId = (String) queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            String topicName = (String) queryMap.get(Constants.KeyStr.TOPICNAME);
            @SuppressWarnings("unchecked")
            Map<String,Object> map = (Map<String, Object>) queryMap.get(Constants.KeyStr.ENTRY);
            JSONObject obj = new JSONObject();
            map.forEach(obj::put);
            boolean flag = kafkaManagerService.updateConfig(topicName, clusterId, obj);
            if(flag) {
                return SUCCESS("Update Config Success");
            }
        } catch (Exception e) {
            LOG.error("Update Config failed,",e) ;
        }
        return ERROR("Update Config failed!");
    }

    /**return the kafka cluster broker id and  host*/
    @PostMapping("/topic/broker_list")
    public RestResponse descBrokerList(@RequestBody Map<String,Object> queryMap) {
        try {
            String clusterId = (String) queryMap.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            JSONArray array  = kafkaManagerService.brokersList(clusterId);
            return SUCCESS_DATA(array);
        } catch (Exception e) {
            LOG.error("Desc Partition By Broker failed,",e) ;
        }
        return ERROR("Desc Partition By Broker failed");
    }

    /**return the consumer group list according to clusterId*/
    @GetMapping(value = "/group")
    public RestResponse getClusterAllGroup(@RequestParam("cluster") String clusterId) {
        List<ClusterInfo> clusters = "-1".equalsIgnoreCase(clusterId)?clusterService.getTotalData():
                new ArrayList<ClusterInfo>() {{
                    add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));
                }};
        List<ClusterGroup> clusterGroups;
        try {
            clusterGroups = monitorService.listGroupsByCluster(clusters, false);
        } catch (Exception e) {
            LOG.error("List Group Error,message:", e);
            return ERROR("error");
        }
        return SUCCESS_DATA(clusterGroups);
    }

   /**delete the consumser groyp
    * 1. delete the group from kafka cluster
    * 2. delete the alert message associated with the Group
    * */
    @PostMapping(value = "/delete/group")
    public RestResponse deleteGroup(@RequestBody ClusterGroup clusterGroup) {
        try {
            Long clusterId = clusterGroup.getClusterID();
            String consumerGroup = clusterGroup.getConsummerGroup();
            String consumerApi = clusterGroup.getConsumereApi();
            kafkaManagerService.deleteGroup(String.valueOf(clusterId), consumerGroup, consumerApi);
            alertService.deleteAlertByClusterGroup(clusterGroup);
        } catch (Exception e) {
            LOG.error("delete group error" ,e);
            return ERROR("delete Group is error, please contact maintenance staff!");
        }
        return SUCCESS("delete group success");
    }

    /**return the broker config information according the clusterId*/
    @GetMapping("broker")
    public RestResponse getAllClusterBrokers(@RequestParam("cluster") String clusterId){
        List<KafkaManagerBroker> kafkaManagerBrokers;
        try {
            kafkaManagerBrokers = kafkaManagerService.getAllClusterBrokers(clusterId);
        } catch (Exception e) {
            LOG.error("getAllClusterBrokers Error,message:", e);
            return ERROR("error");
        }
        return SUCCESS_DATA(kafkaManagerBrokers);
    }
    /** rest the consumer group offset*/
    @PostMapping("/group/rest/offset")
    public RestResponse restOffset(@RequestBody Map<String,String> map){
        try{
            String topic =  map.get(BrokerConfig.TOPIC);
            String group = map.get(BrokerConfig.GROUP);
            String clusterId = map.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            if(kafkaManagerService.resetOffset(clusterId,group,topic)){
                return SUCCESS("group rest offset success");
            }
        }catch (Exception e){
            LOG.error("group rest offset failed:", e);
            return ERROR("group rest offset failed");
        }
        return ERROR("group rest offset failed");
    }
    /**return the topic list by clusterId and topicName*/
    @PostMapping("/group/topic")
    public RestResponse getTopicByGroup(@RequestBody Map<String,String> map){
        try {
            String group = map.get(BrokerConfig.GROUP);
            String clusterId = map.get(Constants.KeyStr.LOWER_CLUSTER_ID);
            return SUCCESS_DATA(kafkaManagerService.getTopicByGroup(clusterId,group));
        }catch (Exception e){
            LOG.error("getTopicByGroup failed:", e);
            return ERROR("getTopicByGroup failed");
        }
    }

    /**
     * return the topic,group status according to HomeCache
     * */
    @GetMapping(value = "/group/status")
    public RestResponse clusterGroupStatus(@RequestParam("cluster") String clusterId) {
        List<HomeCache.ConsumerLagCache> consumerLagCacheList;
        Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
        ClusterInfo clusterInfo = clusterService.selectById(Long.parseLong(clusterId));
        try {
            if(config.isRemoteQueryEnable() && remoteHostsMap.containsKey(clusterInfo.getLocation().toLowerCase())){
                String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+request.getServerName()+ Constants.Symbol.COLON
                        +request.getServerPort()+ Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE+request.getServletPath()
                        .replaceAll(Constants.Symbol.SLASH+ Constants.KeyStr.MANAGER, Constants.Symbol.EMPTY_STR);
                Map<String, String> queryMap = new HashMap<>();
                queryMap.put("cluster",clusterId);
                JSONArray array =  restService.queryRemoteQueryByGet(url,queryMap);
                return SUCCESS_DATA(array);
            }
            consumerLagCacheList =  !"-1".equalsIgnoreCase(clusterId)?HomeCache.consumerLagCacheMap.values().
                    stream().filter(consumerLagCache -> consumerLagCache!=null && consumerLagCache.getClusterId().
                    equalsIgnoreCase(clusterId)).collect(Collectors.toList()): new ArrayList<>(HomeCache.consumerLagCacheMap.values());
            consumerLagCacheList.sort((o1, o2) -> {
                if(o1.getStatus().equalsIgnoreCase(Constants.KeyStr.DEAD) || o2.getStatus().equalsIgnoreCase(Constants.KeyStr.DEAD)){
                    return 1;
                }
                return 0;
            });
            return SUCCESS_DATA(consumerLagCacheList);
        } catch (Exception e) {
            LOG.error("List Group Topic Status Error,message:", e);
            return ERROR("error");
        }

    }
}
