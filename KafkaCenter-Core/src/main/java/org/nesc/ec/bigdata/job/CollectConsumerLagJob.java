package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.GroupTopicConsumerState;
import org.nesc.ec.bigdata.common.model.KafkaCenterGroupState;
import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.common.model.PartitionAssignmentState;
import org.nesc.ec.bigdata.config.AlertaConfig;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.AlertConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
class CollectConsumerLagJob {

    private static final Logger LOG = LoggerFactory.getLogger(CollectConsumerLagJob.class);
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
    NoticeJob noticeJob;

    @Autowired
    AlertaService alertaService;
    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    AlertaConfig alertaConfig;

    void collectConsumerLag() {
        try {
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<ClusterInfo> clusters = clusterService.getTotalData();
            List<AlertGoup> alertGroups = alertService.getEnableAlertGroups();
            Map<Long, String> alertAlarmGroupMap = alertService.getAlertAlarmGroupMap();

            for (ClusterInfo clusterInfo : clusters) {
                if (initConfig.isMonitorCollectorIncludeEnable() && !clusterInfo.getLocation()
                        .equalsIgnoreCase(initConfig.getMonitorCollectorIncludelocation())) {
                    continue;
                }
                LOG.debug("{},collection consumer lag job has start",clusterInfo.toString());
                long start = System.currentTimeMillis();
                Map<String, List<GroupTopicConsumerState>> consumerGroupMap = monitorService.
                        describeConsumerGroupsByCluster(clusterInfo);

                Map<String, List<OffsetInfo>> offsetInfoMap = convertToMap(consumerGroupMap,
                        clusterInfo.getId());

                Map<String, TopicConsumerLag> toEsMap = mergeConsumerGroupsMap(consumerGroupMap,
                        clusterInfo);

                // 实现对比，查看是否超过设置的alert
                Map<String, AlertGoup> alertGroupMap = alertService.generateAlertGroup(alertGroups);
                //解析consumer lag，是否超过alert配置的thesold和alerta的thsold
                watchAlert(alertGroupMap, toEsMap, offsetInfoMap, clusterInfo, alertAlarmGroupMap);
                //将consumer lag信息放入缓存中
                putConsumerLagStatusToMap(toEsMap,clusterInfo);
                // 将监控信息写入ES中
                try {
                    List<JSONObject> consumerOffsets = new ArrayList<>();
                    toEsMap.forEach((key, value) -> {
                        JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(value));
                        object.put("type","consumerLag");
                        consumerOffsets.add(object);
                    });
                    String index = elasticsearchService.getMonitorElasticsearchIndexName() + "-"
                            + sFormat.format(new Date());
                    if (elasticsearchService.getESDB() != null) {
                        elasticsearchService.getESDB().batchInsertES(consumerOffsets, index);
                    }

                } catch (IOException e) {
                    LOG.error("monitor lag message to es error.", e);
                }
                LOG.debug("{},collection consumer lag job end,cost time is {}",clusterInfo.toString(),(System.currentTimeMillis()-start));
            }
        } catch (Exception e) {
            LOG.error("monitor lag  error.", e);
        }

    }

    /**
     * 解析获取到的consumerLag信息，做不同的操作
     *
     * @param alertMap      <clusterId|topic|group|consumerType, AlertGoup>
     * @param toEsMap       <clusterId|topic|group|consumerType, JSONObject>
     * @param offsetInfoMap <clusterId|topic|group|consumerType, List<OffsetInfo>>
     */
    private void watchAlert(Map<String, AlertGoup> alertMap, Map<String, TopicConsumerLag> toEsMap, Map<String, List<OffsetInfo>> offsetInfoMap, ClusterInfo clusterInfo, Map<Long, String> alertAlarmGroupMap) {
        Map<String, String> topicMap = new HashMap<>();

        Map<String, String> alarmGroupMap = alertaService.getAlarmGroupMap();
        if (alertaConfig.isAlterEnable()) {
            List<TopicInfo> topics = topicInfoService.selectTopicsByClusterId(clusterInfo.getId().toString());
            topics.forEach(topicInfo -> {
                if (topicInfo.getOwner() != null) {
                    topicMap.put(topicInfo.getTopicName(), topicInfo.getOwner().getEmail());
                }
            });
        }


        offsetInfoMap.forEach((key, offsetInfo) -> {
            TopicConsumerLag topicConsumerLag = toEsMap.get(key);
            if (topicConsumerLag != null) {
                long lag = topicConsumerLag.getLag();
                if (alertMap.containsKey(key)) {
                    AlertGoup alertGoup = alertMap.get(key);
                    long alertLag = alertGoup.getThreshold();
                    if (lag >= alertLag) {
                        sendToQueue(alertGoup, alarmGroupMap, alertAlarmGroupMap, offsetInfo);
                    }
                } else {
                    if (alertaConfig.isAlterEnable()) {
                        judgeAlerta(topicMap, topicConsumerLag, offsetInfo, clusterInfo);
                    }
                }
            }
        });

    }

    /**
     * @param alertGoup
     * @param alarmGroupMap      alarmGroup和详细信息对应关系；例如：
     *                           XAECBigdata->{"Name":"XAECBigdata","Department":"EC","Status":"Active","EnableWeChat":true,"RocketChat":{"Name":null,"Token":null},"Members":["jh93","mz72","td20","rd87","kz37"]}
     * @param alertAlarmGroupMap alert id与alarm group对照关系
     * @param offsets
     */
    private void sendToQueue(AlertGoup alertGoup, Map<String, String> alarmGroupMap, Map<Long, String> alertAlarmGroupMap, List<OffsetInfo> offsets) {
        try {
            offsets.sort(Comparator.comparingInt(OffsetInfo::getPartition));
            MonitorNoticeInfo monitorNoticeInfo = new MonitorNoticeInfo(alertGoup, offsets, Constants.SendType.ALL);
            if (alertGoup.getOwner()==null) {
                // 管理员设置为默认值
                JSONObject obj = new JSONObject();
                obj.put(AlertConfig.DEPARTMENT, alertaConfig.getAlterEvn());
                obj.put(AlertConfig.NAME, AlertConfig.CONSUMER_GROUP);
                monitorNoticeInfo.setAlertaOwnerGroups(obj.toJSONString());
            } else {
                //根据人所在team设置alarmGroup,根据alarmGroup获取alarmGroup的详细信息
                Long id = alertGoup.getId();
                String alarmGroup = alertAlarmGroupMap.getOrDefault(id, "").toLowerCase();
                String obj = alarmGroupMap.getOrDefault(alarmGroup, "");
                monitorNoticeInfo.setAlertaOwnerGroups(obj);
            }
            NoticeJob.alertQueue.put(monitorNoticeInfo);
        } catch (InterruptedException e) {
            LOG.error("alertQueue put error.", e);
        }

    }


    private void judgeAlerta(Map<String, String> topicMap, TopicConsumerLag topicConsumerLag, List<OffsetInfo> offsetInfo, ClusterInfo clusterInfo) {
        try {
            if (!topicConsumerLag.getStatus().equalsIgnoreCase(KafkaCenterGroupState.DEAD.name())) {
                long lag = topicConsumerLag.getLag();
                if (lag >= Long.parseLong(alertaConfig.getAlertDispause())) {
                    String topicName, consumerGroup;
                    topicName = topicConsumerLag.getTopic();
                    consumerGroup = topicConsumerLag.getGroup();
                    AlertGoup alertGoup = new AlertGoup();
                    alertGoup.setTopicName(topicName);
                    alertGoup.setConsummerGroup(consumerGroup);
                    alertGoup.setCluster(clusterInfo);
                    alertGoup.setConsummerApi(topicConsumerLag.getConsumerType());
                    alertGoup.setThreshold(Integer.parseInt(alertaConfig.getAlertDispause()));
                    if (topicMap.containsKey(topicName)) {
                        alertGoup.setMailTo(topicMap.get(topicName));
                    }
                    offsetInfo.sort(Comparator.comparingInt(OffsetInfo::getPartition));
                    MonitorNoticeInfo monitorNoticeInfo = new MonitorNoticeInfo(alertGoup, offsetInfo, Constants.SendType.ALERTA);
                    NoticeJob.alertQueue.put(monitorNoticeInfo);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("put alert data to queue", e);
        }

    }

    /**
     * consumer lag放入缓存中
     */
    private void putConsumerLagStatusToMap(Map<String, TopicConsumerLag> toEsMap,
                                           ClusterInfo clusterInfo) {
        long time = System.currentTimeMillis();
        toEsMap.forEach((key, topicConsumerLag) -> {
            try {
                if (topicConsumerLag != null) {
                    String status = topicConsumerLag.getStatus();
                    String topic = topicConsumerLag.getTopic();
                    String group = topicConsumerLag.getGroup();
                    String method = topicConsumerLag.getConsumerType();
                    long lag = topicConsumerLag.getLag();
                    long offset =topicConsumerLag.getOffset();
                    HomeCache.ConsumerLagCache consumerCache = new HomeCache.ConsumerLagCache(group, topic, status, lag, offset, method);
                    consumerCache.setCurrentTime(time);
                    String clusterId = clusterInfo.getId().toString();
                    consumerCache.setClusterName(clusterInfo.getName());
                    consumerCache.setClusterId(clusterInfo.getId().toString());
                    String mapKey = clusterId + Constants.Symbol.VERTICAL_STR + topic + Constants.Symbol.VERTICAL_STR + group + Constants.Symbol.VERTICAL_STR + method;
                    HomeCache.consumerLagCacheMap.put(mapKey,consumerCache);
                }
            } catch (Exception e) {
                LOG.error("put consumer lag status to cache has error,please check!", e);
            }

        });
    }


    /**
     * 合并partition offset信息
     *
     * @param consumerGroupsMap 消费组Map
     * @param clusterInfo       clusterInfo
     * @return <clusterId|topic|group|consumerType, JSONObject>
     */
    private Map<String, TopicConsumerLag> mergeConsumerGroupsMap(
            Map<String, List<GroupTopicConsumerState>> consumerGroupsMap, ClusterInfo clusterInfo) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        Map<String, TopicConsumerLag> map = new HashMap<>(2<<2);
        consumerGroupsMap.forEach((method,groupTopicConsumerState)-> groupTopicConsumerState.stream().filter(Objects::nonNull).forEach(state->{
            String key = clusterInfo.getId() + Constants.Symbol.VERTICAL_STR + state.getTopic() + Constants.Symbol.VERTICAL_STR
                    + state.getGroup() + Constants.Symbol.VERTICAL_STR + method.toLowerCase();
            long offset = state.getPartitionAssignmentStates().stream().mapToLong(PartitionAssignmentState::getOffset).sum();
            long lag = state.getPartitionAssignmentStates().stream().mapToLong(PartitionAssignmentState::getLag).sum();
            long logEndOffset = state.getPartitionAssignmentStates().stream().mapToLong(PartitionAssignmentState::getLogEndOffset).sum();
            TopicConsumerLag topicConsumerLag = new TopicConsumerLag();
            topicConsumerLag.setClusterId(clusterInfo.getId().toString());
            topicConsumerLag.setClusterName(clusterInfo.getName());
            topicConsumerLag.setOffset(offset);
            topicConsumerLag.setLag(lag);
            topicConsumerLag.setTopic(state.getTopic());
            topicConsumerLag.setGroup(state.getGroup());
            topicConsumerLag.setStatus(state.getKafkaCenterGroupState().name());
            topicConsumerLag.setLogSize(logEndOffset);
            topicConsumerLag.setConsumerType(method.toLowerCase());
            topicConsumerLag.setDate( sFormat.format(new Date(cal.getTimeInMillis())));
            topicConsumerLag.setTimestamp(cal.getTimeInMillis());
            map.put(key,topicConsumerLag);
        }));
        return map;
    }

    /**
     * 将consumerGroupsMap重新转换一下，便于不同维度的使用
     *
     * @param consumerGroupsMap <broker/zk, List<PartitionAssignmentState>>
     * @return <clusterId|topic|group|consumerType, List<OffsetInfo>>
     */
    private Map<String, List<OffsetInfo>> convertToMap(Map<String, List<GroupTopicConsumerState>> consumerGroupsMap,
                                                       long clusterId) {
        Map<String, List<OffsetInfo>> data = new HashMap<>(2<<2);
        consumerGroupsMap.forEach((key, value) -> {
            String consumerType = key.toLowerCase();
            value.forEach(state -> state.getPartitionAssignmentStates().forEach(partitionAssignmentState -> {
                String newKey = clusterId + Constants.Symbol.VERTICAL_STR + state.getTopic() + Constants.Symbol.VERTICAL_STR + state.getGroup() + Constants.Symbol.VERTICAL_STR + consumerType;
                OffsetInfo offsetInfo = new OffsetInfo(state.getGroup(), state.getTopic(), partitionAssignmentState.getPartition(),
                        partitionAssignmentState.getLag(), consumerType);
                List<OffsetInfo> offsets;
                if (data.containsKey(newKey)) {
                    offsets = data.get(newKey);
                } else {
                    offsets = new ArrayList<>();
                }
                offsets.add(offsetInfo);
                data.put(newKey, offsets);
            }));
        });
        return data;
    }

//    private JSONObject generateRecord(long timestamp, String dateStr, String consumerType,
//                                      PartitionAssignmentState partitionAssignmentState) {
//        JSONObject data = new JSONObject();
//        data.put(BrokerConfig.GROUP, partitionAssignmentState.getGroup());
//        data.put(BrokerConfig.TOPIC, partitionAssignmentState.getTopic());
//        if (partitionAssignmentState.getOffset() >= 0) {
//            data.put(TopicConfig.OFFSET, partitionAssignmentState.getOffset());
//            data.put(TopicConfig.LAG, partitionAssignmentState.getLag());
//        } else {
//            data.put(TopicConfig.OFFSET, 0);
//            data.put(TopicConfig.LAG, 0);
//        }
//
//        data.put(TopicConfig.LOGSIZE, partitionAssignmentState.getLogEndOffset());
//        data.put(Constants.KeyStr.DATE, dateStr);
//        data.put(Constants.KeyStr.TIMESTAMP, timestamp);
//
//        data.put(Constants.KeyStr.COMSUMBER_TYPE, consumerType);
//        return data;
//    }

    /**
     * write to es topic consumer lag entity
     * **/
    static class TopicConsumerLag{
        private String topic;
        private String group;
        private long offset;
        private long lag;
        private long logSize;
        private String status;
        private String clusterId;
        private String clusterName;
        private String date;
        private long timestamp;
        private String consumerType;

        public String getConsumerType() {
            return consumerType;
        }

        public void setConsumerType(String consumerType) {
            this.consumerType = consumerType;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public long getLag() {
            return lag;
        }

        public void setLag(long lag) {
            this.lag = lag;
        }

        public long getLogSize() {
            return logSize;
        }

        public void setLogSize(long logSize) {
            this.logSize = logSize;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
