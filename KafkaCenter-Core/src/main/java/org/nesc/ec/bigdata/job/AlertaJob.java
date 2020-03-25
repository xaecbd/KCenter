package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.constant.AlertConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.service.TopicInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @author lg99
 */
@Component
public class AlertaJob {

    private static final Logger LOG = LoggerFactory.getLogger(AlertaJob.class);
    @Value("${public.url:localhost:8080}")
    private String url;

    @Value("${alert.dispause:''}")
    private String alertDispause;
    @Value("${alert.service:''}")
    private String alertService;
    @Value("${alert.threshold:''}")
    private String alertThreshold;
    @Value("${alter.env:''}")
    private String alterEvn;
    @Value("${alert.enable:false}")
    private boolean alterEnable;

    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    RestTemplate restTemplate;

    void judgeAlerta(JSONObject offsetObj, List<OffsetInfo> offsetInfo, ClusterInfo clusterInfo) {
        try{
            if(alterEnable){
                List<TopicInfo> topics = topicInfoService.selectTopicsByClusterId(clusterInfo.getId().toString());
                Map<String,String> topicMap = new HashMap<>();
                topics.forEach(topicInfo -> {
                    if(topicInfo.getOwner()!=null){
                        topicMap.put(topicInfo.getTopicName(),topicInfo.getOwner().getEmail());
                    }
                });
                if(!offsetObj.containsKey(Constants.KeyStr.STATE)){
                    long lag = offsetObj.getLongValue(TopicConfig.LAG);
                    if(lag>=Long.parseLong(alertThreshold)){
                        String topicName,consumerGroup;
                        topicName = offsetInfo.get(0).getTopic();
                        consumerGroup = offsetInfo.get(0).getGroup();
                        AlertGoup alertGoup = new AlertGoup();
                        alertGoup.setTopicName(topicName);
                        alertGoup.setConsummerGroup(consumerGroup);
                        alertGoup.setCluster(clusterInfo);
                        alertGoup.setConsummerApi(offsetInfo.get(0).getConsumerMethod());
                        alertGoup.setThreshold(Integer.parseInt(alertThreshold));
                        if(topicMap.containsKey(topicName)){
                            alertGoup.setMailTo(topicMap.get(topicName));
                        }
                        offsetInfo.sort(Comparator.comparingInt(OffsetInfo::getPartition));
                        MonitorNoticeInfo monitorNoticeInfo = new MonitorNoticeInfo(alertGoup,offsetInfo,Constants.SendType.ALERTA);
                        CollectorAndAlertJob.alertQueue.put(monitorNoticeInfo);
                    }
                }
            }
        }catch (InterruptedException e){
            LOG.error("put alerte data to queue",e);
        }

    }

    void sendToAlerta(MonitorNoticeInfo monitorNoticeInfo,Long now,Long lastTime){

        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        if(alterEnable && !alertGoup.isDisableAlerta()){
            long disTime = Long.parseLong(alertDispause);
            if(((now - lastTime)>=disTime*Constants.Time.SIXTY*Constants.Time.THOUSAND)){
                generateSendToAlterService(monitorNoticeInfo);
            }
        }
    }

    void sendToAlerta(MonitorNoticeInfo monitorNoticeInfo){
        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        if(alterEnable && !alertGoup.isDisableAlerta()){
            generateSendToAlterService(monitorNoticeInfo);
        }
    }

    private  void generateSendToAlterService(MonitorNoticeInfo monitorNoticeInfo){
        String topic,group,method;
        AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();
        topic = alertGoup.getTopicName();
        group = alertGoup.getConsummerGroup();
        method = alertGoup.getConsummerApi();
        String concont = alertGoup.getCluster().getName()+Constants.Symbol.Vertical_STR+alertGoup.getTopicName()+
                Constants.Symbol.Vertical_STR+alertGoup.getConsummerGroup()+Constants.Symbol.Vertical_STR+alertGoup.getConsummerApi();
        try {
            List<OffsetInfo> offsetInfos = monitorNoticeInfo.getOffsetInfos();
            long value = 0L;
            for (OffsetInfo offsetInfo:offsetInfos){
                value = value+offsetInfo.getLag();
            }
            String key = alertGoup.getCluster().getId() + Constants.Symbol.Vertical_STR + topic + Constants.Symbol.Vertical_STR
                    + group;

            String email = Optional.ofNullable(alertGoup.getMailTo()).orElse("");
            String href = "<a href=" + url + "/#/monitor/topic/consumer_offsets/chart/"+alertGoup.getCluster().getId()+"/"+topic+"/"+group+"/"+method+">" +group + "</a>";
            String threshold = " "+group+" consumer lag > "+alertGoup.getThreshold();
            String clusterName = monitorNoticeInfo.getAlertGoup().getCluster().getName();
            JSONObject attriute = new JSONObject();
            attriute.put(Constants.KeyStr.OWNER_EMAIL,email);
            attriute.put(Constants.KeyStr.MORE_INFO,href);
            attriute.put(Constants.KeyStr.THRESHOLDINFO,threshold);
            JSONObject object = new JSONObject();
            object.put(AlertConfig.RESOURCE,group);
            object.put(AlertConfig.EVENT,TopicConfig.LAG);
            object.put(AlertConfig.ENVIRONMENT,alterEvn);
            object.put(AlertConfig.SEVERITY,AlertConfig.CONSUMER_SEVERITY);
            object.put(AlertConfig.CORRELATE,new JSONArray());
            List<String> service = new ArrayList<>();
            service.add(topic);
            object.put(AlertConfig.SERVICE,service);
            object.put(AlertConfig.GROUP,AlertConfig.CONSUMER_GROUP);
            object.put(AlertConfig.VALUE,value);
            object.put(AlertConfig.TEXT,clusterName);
            object.put(AlertConfig.TAGS,new JSONArray());
            object.put(AlertConfig.ATTRIBUTES,attriute);
            object.put(AlertConfig.ORIGIN,AlertConfig.CONSUMER_ORIGIN);
            object.put(AlertConfig.TYPE,AlertConfig.CONSUMER_TYPE);
            object.put(AlertConfig.CREATETIME,new Date());
            object.put(AlertConfig.TIMEOUT,null);
            object.put(AlertConfig.RAWDATA,null);
            object.put(AlertConfig.CUSTOMER,null);
            HttpHeaders headers = new HttpHeaders();
            headers.add(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(object.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(alertService, httpEntity, String.class);
            JSONObject respObj = JSONObject.parseObject(Objects.requireNonNull(response.getBody()).toString());
            if(respObj.containsKey(AlertConfig.STATUS) &&
                    respObj.getString(AlertConfig.STATUS).equalsIgnoreCase(Constants.Status.OK)){
                CollectorAndAlertJob.cachedLastSendTime.put(key, System.currentTimeMillis());
            }
        }catch (Exception e){
            LOG.error("generateToAlertaError "+concont,e);
        }
    }
}
