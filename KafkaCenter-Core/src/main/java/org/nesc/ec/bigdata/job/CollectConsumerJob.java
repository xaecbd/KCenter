package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.common.model.PartitionAssignmentState;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.nesc.ec.bigdata.service.AlertService;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.ElasticsearchService;
import org.nesc.ec.bigdata.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
class CollectConsumerJob {

    private static final Logger LOG = LoggerFactory.getLogger(CollectConsumerJob.class);
    @Autowired
    ClusterService clusterService;

    @Autowired
    InitConfig initConfig;

    @Autowired
    AlertService alertService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    AlertaJob alertaJob;

     void collectConsumer(){
        try {
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<ClusterInfo> clusters = clusterService.getTotalData();
            List<AlertGoup> alertGroups = alertService.getAlertGoups();
            for (ClusterInfo clusterInfo : clusters) {

                if (initConfig.isMonitorCollectorIncludeEnable() && !clusterInfo.getLocation()
                        .equalsIgnoreCase(initConfig.getMonitorCollectorIncludelocation())) {
                    continue;
                }
                Map<String, List<PartitionAssignmentState>> consumerGroupsMap = monitorService
                        .describeConsumerGroups(clusterInfo.getId().toString());
                Map<String, List<OffsetInfo>> offsetInfoMap = convertToMap(consumerGroupsMap,
                        clusterInfo.getId());

                Map<String, JSONObject> toEsMap = mergeConsumerGroupsMap(consumerGroupsMap,
                        clusterInfo.getId());

                // 实现对比，查看是否超过设置的alert
                Map<String,AlertGoup> alertGoupMap = generateAlertGroup(alertGroups);
                //解析consumer lag，是否超过alert配置的thesold和alerta的thsold
                watchAlert(alertGoupMap,toEsMap,offsetInfoMap,clusterInfo);
                //将consumer lag信息放入缓存中
                putConsumerLagStatusToMap(toEsMap,offsetInfoMap,clusterInfo);
                // 将监控信息写入ES中
                try {
                    List<JSONObject> consumerOffsets = new ArrayList<>();
                    toEsMap.forEach((key, value) -> consumerOffsets.add(value));
                    String index = elasticsearchService.getMonitorElasticsearchIndexName() + "-"
                            + sFormat.format(new Date());
                    if(elasticsearchService.getESDB()!=null){
                        elasticsearchService.getESDB().batchInsertES(consumerOffsets, index);
                    }

                } catch (IOException e) {
                    LOG.error("monitor lag message to es error.", e);
                }
            }

        } catch (Exception e) {
            LOG.error("monitor lag  error.", e);
        }

    }

    /**
     * 解析获取到的consumerLag信息，做不同的操作
     *
     * @param alertMap      <clusterId|topic|group, AlertGoup>
     * @param toEsMap       <clusterId|topic|group|consumerType, JSONObject>
     * @param offsetInfoMap <clusterId|topic|group|consumerType, List<OffsetInfo>>
     */
    private void watchAlert(Map<String, AlertGoup> alertMap, Map<String, JSONObject> toEsMap,
                                      Map<String, List<OffsetInfo>> offsetInfoMap,ClusterInfo clusterInfo){
        offsetInfoMap.forEach((key,offsetInfo)->{
            JSONObject offsetObj = toEsMap.get(key);
            if(offsetObj!=null && !offsetObj.isEmpty()){
                long lag = offsetObj.getLongValue(TopicConfig.LAG);
                if(alertMap.containsKey(key)){
                    AlertGoup alertGoup = alertMap.get(key);
                    long alertLag = alertGoup.getThreshold();
                    if (lag >= alertLag) {
                        sendToQueue(alertGoup,offsetInfo);
                    }
                }else{
                    alertaJob.judgeAlerta(offsetObj,offsetInfo,clusterInfo);
                }
            }
        });

    }

    /**
     *  consumer lag放入缓存中
     */
    private void putConsumerLagStatusToMap(Map<String, JSONObject> toEsMap,
                                           Map<String, List<OffsetInfo>> offsetInfoMap,ClusterInfo clusterInfo){
        offsetInfoMap.forEach((key,offsetInfoList)->{
            try{
                JSONObject offsetObj = toEsMap.get(key);
                if(offsetObj!=null && !offsetObj.isEmpty()){
                    OffsetInfo offsetInfo = offsetInfoList.get(0);
                    String status = offsetObj.containsKey(Constants.Status.STATUS)?offsetObj.getString(Constants.Status.STATUS):Constants.Status.ACTIVE;
                    String topic = offsetInfo.getTopic();
                    String group = offsetInfo.getGroup();
                    String method = offsetInfo.getConsumerMethod();
                    long lag =  offsetObj.getLongValue(TopicConfig.LAG);
                    long offset = offsetInfoList.stream().mapToLong(OffsetInfo::getOffset).sum();
                    HomeCache.ConsumerLagCache consumerCache = new HomeCache.ConsumerLagCache(group,topic,status,lag,offset,method);
                    String clusterId = clusterInfo.getId().toString();
                    String mapKey =clusterId+Constants.Symbol.Vertical_STR+ topic + Constants.Symbol.Vertical_STR +group+Constants.Symbol.Vertical_STR+ method;
                    HomeCache.consumerLagCacheMap.put(mapKey,consumerCache);
                }
            }catch (Exception e){
                LOG.error("put consumer lag status to cache has error,please check!",e);
            }

        });

    }




    private void sendToQueue(AlertGoup alertGoup,
                             List<OffsetInfo> offsets){
        try {
            offsets.sort(Comparator.comparingInt(OffsetInfo::getPartition));
            MonitorNoticeInfo monitorNoticeInfo = new MonitorNoticeInfo(alertGoup,offsets,Constants.SendType.EMAIL);
            CollectorAndAlertJob.alertQueue.put(monitorNoticeInfo);
        } catch (InterruptedException e) {
            LOG.error("alertQueue put error.", e);
        }

    }
    private Map<String,AlertGoup>  generateAlertGroup(List<AlertGoup> alertGoups){
        Map<String,AlertGoup> alertGoupMap = new HashMap<>();
        alertGoups.forEach(alertGoup -> {
            String consumerAPI = alertGoup.getConsummerApi().toLowerCase();
            if(Constants.KeyStr.ALL.equalsIgnoreCase(consumerAPI)){
                String brokerKey = alertGoup.getCluster().getId() + Constants.Symbol.Vertical_STR + alertGoup.getTopicName() + Constants.Symbol.Vertical_STR
                        + alertGoup.getConsummerGroup()+Constants.Symbol.Vertical_STR+ BrokerConfig.BROKER;
                String zkKey = alertGoup.getCluster().getId() + Constants.Symbol.Vertical_STR + alertGoup.getTopicName() + Constants.Symbol.Vertical_STR
                        + alertGoup.getConsummerGroup()+Constants.Symbol.Vertical_STR+Constants.KeyStr.zk;
                alertGoupMap.put(brokerKey,alertGoup);
                alertGoupMap.put(zkKey,alertGoup);
            }else{
                String key = alertGoup.getCluster().getId() + Constants.Symbol.Vertical_STR + alertGoup.getTopicName() + Constants.Symbol.Vertical_STR
                        + alertGoup.getConsummerGroup()+Constants.Symbol.Vertical_STR+consumerAPI;
                alertGoupMap.put(key,alertGoup);
            }
        });
        return alertGoupMap;

    }

    /**
     * 合并partition offset信息
     *
     * @param consumerGroupsMap  消费组Map
     * @param clusterId  clusterId
     * @return <clusterId|topic|group|consumerType, JSONObject>
     */
    private Map<String, JSONObject> mergeConsumerGroupsMap(
            Map<String, List<PartitionAssignmentState>> consumerGroupsMap, long clusterId) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Map<String, JSONObject> map = new HashMap<>();
        consumerGroupsMap.forEach((method, partitionAssignmentStates) -> partitionAssignmentStates.forEach(partitionAssignmentState -> {
            String key = clusterId + Constants.Symbol.Vertical_STR + partitionAssignmentState.getTopic() + Constants.Symbol.Vertical_STR
                    + partitionAssignmentState.getGroup() + Constants.Symbol.Vertical_STR + method.toLowerCase();
            long offset = partitionAssignmentState.getOffset();

            if (map.containsKey(key)) {
                JSONObject tempData = map.get(key);
                long lag = tempData.getLong(TopicConfig.LAG);
                if (offset < 0) {
                    offset = tempData.getLong(TopicConfig.OFFSET);
                } else {
                    offset = offset + tempData.getLong(TopicConfig.OFFSET);
                    lag = partitionAssignmentState.getLag() + lag;
                }

                long logSize = partitionAssignmentState.getLogEndOffset() + tempData.getLong(TopicConfig.LOGSIZE);
                tempData.put(TopicConfig.OFFSET, offset);
                tempData.put(TopicConfig.LOGSIZE, logSize);
                tempData.put(TopicConfig.LAG, lag);
                if(!tempData.containsKey(Constants.KeyStr.STATE)){
                    if(partitionAssignmentState.getClientId()==null || Constants.KeyStr.NULL.equalsIgnoreCase(partitionAssignmentState.getClientId())){
                        tempData.put(Constants.KeyStr.STATE,Constants.KeyStr.DEAD);
                    }
                }
                map.put(key, tempData);
            } else {
                JSONObject data = generateRecord(cal.getTimeInMillis(),
                        sFormat.format(new Date(cal.getTimeInMillis())), method, partitionAssignmentState);
                data.put("clusterId", clusterId);
                map.put(key, data);
            }

        }));

        return map;
    }

    /**
     * 将consumerGroupsMap重新转换一下，便于不同维度的使用
     *
     * @param consumerGroupsMap <broker/zk, List<PartitionAssignmentState>>
     * @return <clusterId|topic|group|consumerType, List<OffsetInfo>>
     */
    private Map<String, List<OffsetInfo>> convertToMap(Map<String, List<PartitionAssignmentState>> consumerGroupsMap,
                                                       long clusterId) {
        Map<String, List<OffsetInfo>> data = new HashMap<>();
        consumerGroupsMap.forEach((key, value) -> {
            String consumerType = key.toLowerCase();
            value.forEach(state -> {
                String newKey = clusterId + Constants.Symbol.Vertical_STR + state.getTopic() + Constants.Symbol.Vertical_STR + state.getGroup() + Constants.Symbol.Vertical_STR + consumerType;
                OffsetInfo offsetInfo = new OffsetInfo(state.getGroup(), state.getTopic(), state.getPartition(),
                        state.getLag(), consumerType);
                if (data.containsKey(newKey)) {
                    List<OffsetInfo> offsets = data.get(newKey);
                    offsets.add(offsetInfo);
                    data.put(newKey, offsets);
                } else {
                    List<OffsetInfo> offsets = new ArrayList<>();
                    offsets.add(offsetInfo);
                    data.put(newKey, offsets);
                }
            });
        });
        return data;
    }
    private JSONObject generateRecord(long timestamp, String dateStr, String consumerType,
                                      PartitionAssignmentState partitionAssignmentState) {
        JSONObject data = new JSONObject();
        data.put(BrokerConfig.GROUP, partitionAssignmentState.getGroup());
        data.put(BrokerConfig.TOPIC, partitionAssignmentState.getTopic());
        if (partitionAssignmentState.getOffset() >= 0) {
            data.put(TopicConfig.OFFSET, partitionAssignmentState.getOffset());
            data.put(TopicConfig.LAG, partitionAssignmentState.getLag());
        } else {
            data.put(TopicConfig.OFFSET, 0);
            data.put(TopicConfig.LAG, 0);
        }

        data.put(TopicConfig.LOGSIZE, partitionAssignmentState.getLogEndOffset());
        data.put(Constants.KeyStr.DATE, dateStr);
        data.put(Constants.KeyStr.TIMESTAMP, timestamp);

        data.put(Constants.KeyStr.COMSUMBER_TYPE, consumerType);
        return data;
    }

}
