package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.cache.ExpireMap;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TopicGroup;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.ConsumerLagApiService;
import org.nesc.ec.bigdata.service.KafkaAdminService;
import org.nesc.ec.bigdata.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/consumer")
public class ConsumerLagApiController extends BaseController {

    @Autowired
    ConsumerLagApiService consumerService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    InitConfig config;

    @Autowired
    HttpServletRequest request;

    @Autowired
    RestService restService;

    @Autowired
    KafkaAdminService kafkaAdminService;


    /** return the consumer group status */
    @GetMapping("/status")
    public RestResponse consumerLagStatus(@RequestParam(value = "cluster_id") String clusterId, @RequestParam(value = "topic") String topic,
                                          @RequestParam(value = "group") String group){
        Map<String, String> remoteHostsMap = config.getRemoteHostsMap();
        ClusterInfo info = clusterService.selectById(Long.parseLong(clusterId));
        if(Objects.isNull(info)){
            return ERROR("cluster is not exit");
        }
        try{
          boolean flag =   kafkaAdminService.getKafkaAdmins(clusterId).checkExists(topic);
          if(!flag){
              return new RestResponse(400,"topic is not exits,please check!");
          }
        }catch (Exception e){
            return ERROR("check topic has error,please check!");
        }
        // 查看是否启用remote，并且配置了clusterID对应的remoteHost
        if (config.isRemoteQueryEnable() && remoteHostsMap.containsKey(info.getLocation().toLowerCase())) {
            return remoteLocationConsumerLagStatus(clusterId,topic,group);
        }
        /**current location get consumer group status*/
        return currentLocationConsumerLagStatus(clusterId,topic,group);

    }

    /**request the remote location get the consumer group status*/
    private RestResponse remoteLocationConsumerLagStatus(String clusterId,String topic,String group){
        String url = request.getScheme()+ Constants.Symbol.COLON+ Constants.Symbol.DOUBLE_SLASH+request.getServerName()+ Constants.Symbol.COLON
                +request.getServerPort()+ Constants.Symbol.SLASH+ Constants.KeyStr.REMOTE+request.getServletPath()
                .replaceAll(Constants.Symbol.SLASH+ Constants.KeyStr.API, Constants.Symbol.EMPTY_STR);
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("cluster_id",clusterId);
        queryMap.put("topic",topic);
        queryMap.put("group",group);
        JSONArray data = restService.queryRemoteQueryByGet(url,queryMap);
        return SUCCESS_DATA(data);
    }

    /**the current location get the consumer group status*/
    private RestResponse currentLocationConsumerLagStatus(String clusterId,String topic,String group){
        String brokerKey = new TopicGroup(clusterId,topic,group,Constants.ConsumerType.BROKER).generateKey();
        String zookeeperKey = new TopicGroup(clusterId,topic,group,Constants.ConsumerType.ZK).generateKey();
        Object brokerValue = ExpireMap.get(brokerKey);
        Object zookeeperValue = ExpireMap.get(zookeeperKey);
        List<Object> result;
        if(Objects.isNull(brokerValue) && Objects.isNull(zookeeperValue)){
            try{
                result =  consumerService.describeConsumerLagStatus(clusterId,topic,group);
                return SUCCESS_DATA(result);
            }catch (Exception e){
                return ERROR("get consumer group lag status has error,please check!");
            }
        }
        result = consumerService.parseResult(brokerValue,zookeeperValue);
        return SUCCESS_DATA(result);
    }




}
