package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.*;
import org.nesc.ec.bigdata.common.util.JmxCollector;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Truman.P.Du
 * @version 1.0
 * @date 2019年4月10日 下午2:40:35
 */
@Service
public class MonitorService {
    private final static Logger LOG = LoggerFactory.getLogger(MonitorService.class);
    @Autowired
    KafkaAdminService kafkaAdminService;
    @Autowired
    KafkaConsumersService kafkaConsumersService;

    @Autowired
    TopicInfoService topicInfoService;
    @Autowired
    ZKService zkService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    ElasticsearchService eService;

    @Autowired
    ConsumerLagApiService consumerLagApiService;


    public List<MonitorTopic> getTopicList(List<ClusterInfo> clusterInfos, UserInfo user, String type) {
        List<MonitorTopic> monitorTopics = new ArrayList<>();
        List<org.nesc.ec.bigdata.model.Collections> collections = collectionService.list(user.getId(), type);
        Map<Long, Set<String>> xx = this.transfromToMap(collections);
        for (ClusterInfo info : clusterInfos) {
            try {
                Set<String> topicMap = kafkaAdminService.getKafkaAdmins(info.getId().toString()).listTopics();
                topicMap.forEach(topic -> {
                    MonitorTopic monitorTopic = new MonitorTopic();
                    monitorTopic.setClusterID(info.getId());
                    monitorTopic.setClusterName(info.getName());
                    monitorTopic.setTopicName(topic);
                    if (xx.containsKey(info.getId())) {
                        Set<String> str = xx.get(info.getId());
                        if (str.contains(topic)) {
                            monitorTopic.setCollections(true);
                        }
                    }
                    monitorTopics.add(monitorTopic);
                });

            } catch (Exception e) {
                LOG.error("get topicList by user", e);
                MonitorTopic monitorTopic = new MonitorTopic();
                monitorTopics.add(monitorTopic);
            }
        }
        return monitorTopics;
    }

    public JSONObject selectTopicLogSizeAndOffset(String clusterId, String topic) {
        JSONObject object = new JSONObject();
        String group = BrokerConfig.GROUP_NAME;
        String clusterInfo = clusterService.getBrokers(clusterId);
        Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterInfo, group, topic);
        long sumLogSize = logSizeMap.values().stream().mapToLong(l -> l).sum();
        Long fileSize = topicInfoService.selectFileSizeByTopic(topic, Long.parseLong(clusterId));
        object.put(BrokerConfig.LOGSIZE, sumLogSize);
        object.put(BrokerConfig.FILESIZE, fileSize);
        return object;

    }

    /**
     * 获取集群所有topic
     *
     * @param clusterID
     * @return
     */
    public Set<String> getTopicList(String clusterID) {
        Set<String> monitorTopics = null;
        try {
            monitorTopics = kafkaAdminService.getKafkaAdmins(clusterID).listTopics();
        } catch (Exception e) {
            LOG.error("get topicList by clusterId", e);
        }

        return monitorTopics;
    }

    /**
     * 获取topic 消费offset情况详情
     *
     * @param topic
     * @param clusterInfo
     * @return
     */
    public List<TopicConsumerGroupState> describeConsumerGroups(String topic, ClusterInfo clusterInfo) {
        List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
        // broker消费 方式offset获取
        List<TopicConsumerGroupState> brokerTopicConsumerGroupStates = this.getBrokerConsumerOffsets(clusterInfo, topic);
        topicConsumerGroupStates.addAll(brokerTopicConsumerGroupStates);
        // zk消费 方式offset获取
        List<TopicConsumerGroupState> zkTopicConsumerGroupStates = new ArrayList<>();
        zkTopicConsumerGroupStates = this.getZKConsumerOffsets(clusterInfo, topic);
        topicConsumerGroupStates.addAll(zkTopicConsumerGroupStates);
        return topicConsumerGroupStates;
    }

    /**
     * 获取集群所有topic 消费offset情况详情
     *
     * @param clusterInfo
     * @return
     */
    public Map<String, List<GroupTopicConsumerState>> describeConsumerGroupsByCluster(ClusterInfo clusterInfo) {
        Map<String, List<GroupTopicConsumerState>> topicsOffsetsMap = new HashMap<>();

        List<GroupTopicConsumerState> brokerGroupTopicConsumerState = new ArrayList<>();

        List<GroupTopicConsumerState> zkGroupTopicConsumerState = new ArrayList<>();
        try {
            // 获取broker消费方式消费消息
            brokerGroupTopicConsumerState = this.getBrokerConsumerOffsetsGroupByCluster(clusterInfo);
            // 获取zk消费方式消费消息
            zkGroupTopicConsumerState = this.getZKConsumerOffsetsGroupByCluster(clusterInfo);
        } catch (Exception e) {
            LOG.error("describeConsumerGroups has error.", e);
        }
        topicsOffsetsMap.put("broker", brokerGroupTopicConsumerState);
        topicsOffsetsMap.put("zk", zkGroupTopicConsumerState);
        return topicsOffsetsMap;
    }

    /**
     * 根据指定集群查询集群所有group，topic的消费延迟情况 获取所有group 所有topic， 消费情况 优化查询速度，减少网络交互
     * 根据group去查询offset，针对不存在group的topic不做收集， 理论上能提高速度 //第一步获取集群所有group
     * //第二部根据group获取ConsumerGroupOffsets //第三部通过Consumer获取客户端可见的LogSize
     * <p>
     * <p>
     * 该方法仅供收集job使用，未包含member等信息
     *
     * @param clusterInfo
     * @return
     */
    public List<GroupTopicConsumerState> getBrokerConsumerOffsetsGroupByCluster(ClusterInfo clusterInfo) {
        List<GroupTopicConsumerState> groupTopicConsumerStatesList = new ArrayList<>();
        KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterInfo.getId().toString());
        try {
            // 第一步获取集群所有group
            Set<String> groupIds = kafkaAdmins.listConsumerGroups();
            // 第二部根据group获取ConsumerGroupOffsets
            groupIds.forEach(groupId -> {
                List<GroupTopicConsumerState> groupTopicConsumerStates = getBrokerConsumerOffsetsByGroup(clusterInfo, groupId);
                if (!groupTopicConsumerStates.isEmpty()) {
                    groupTopicConsumerStatesList.addAll(groupTopicConsumerStates);
                }

            });
        } catch (Exception e) {
            LOG.error("cluster:{} collector consumer lag job error.", clusterInfo.toString(), e);
        }
        return groupTopicConsumerStatesList;
    }

    /**
     * 指定集群查询zk消费方式的lag信息
     *
     * @param clusterInfo
     * @return
     */
    private List<GroupTopicConsumerState> getZKConsumerOffsetsGroupByCluster(ClusterInfo clusterInfo) {
        List<GroupTopicConsumerState> groupTopicConsumerStatesList = new ArrayList<>();
        String clusterId = clusterInfo.getId().toString();
        Map<String, Map<String, Map<String, String>>> zkConsumerOffsets = zkService.getZK(clusterId).getZKConsumerOffsets();
        if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
            return groupTopicConsumerStatesList;
        }
        zkConsumerOffsets.forEach((group, topicOffsets) -> {
            if (topicOffsets == null || topicOffsets.isEmpty()) {
                return;
            }
            topicOffsets.forEach((topic, partitionsMap) -> {
                if (partitionsMap == null) {
                    return;
                }
                GroupTopicConsumerState groupConsumerState = new GroupTopicConsumerState(group, topic, Constants.ConsumerType.ZK);

                List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();
                Map<Integer, Long> logSizeMap = this.getPartitionLogSizeMap(clusterInfo, group, topic);
                partitionsMap.forEach((partition, offsetStr) -> {
                    Long logEndOffset = logSizeMap.get(partition);
                    if (logEndOffset == null) {
                        return;
                    }
                    long offset = Long.parseLong(offsetStr);
                    PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
                    partitionAssignmentState.setLogEndOffset(logEndOffset);
                    partitionAssignmentState.setTopic(topic);
                    partitionAssignmentState.setOffset(offset);
                    partitionAssignmentState.setPartition(Integer.parseInt(partition));
                    partitionAssignmentState.setLag(getLag(offset, logEndOffset));

                    partitionAssignmentStates.add(partitionAssignmentState);
                });
                groupConsumerState.setPartitionAssignmentStates(partitionAssignmentStates);
                groupConsumerState.setKafkaCenterGroupState(judgeStateByGroupTopicConsumerState(groupConsumerState, clusterInfo));
                groupTopicConsumerStatesList.add(groupConsumerState);
            });
        });
        return groupTopicConsumerStatesList;
    }

    /**
     * 根据topic获取group,包括broker消费方式与zk消费方式
     *
     * @param topic
     * @param clusterID
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public Set<String> listGroups(String topic, String clusterID)
            throws InterruptedException, ExecutionException, TimeoutException {
        Set<String> listGroups = kafkaAdminService.getKafkaAdmins(clusterID).listConsumerGroups(topic);
        Set<String> zkGroups = zkService.getZK(clusterID).listTopicGroups(topic);
        listGroups.addAll(zkGroups);
        return listGroups;
    }

    /**
     * broker消费 方式offset获取
     *
     * @param clusterInfo
     * @param topic
     * @return
     */
    private List<TopicConsumerGroupState> getBrokerConsumerOffsets(ClusterInfo clusterInfo, String topic) {
        List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();

        try {
            topicConsumerGroupStates = kafkaAdminService.getKafkaAdmins(clusterInfo.getId().toString()).describeConsumerGroups(topic);
            if (topicConsumerGroupStates == null || topicConsumerGroupStates.isEmpty()) {
                return topicConsumerGroupStates;
            }

            Set<String> groups = new HashSet<>();
            // 填充lag/logEndOffset
            topicConsumerGroupStates.forEach(topicConsumerGroupState -> {
                String groupId = topicConsumerGroupState.getGroupId();
                groups.add(groupId);
                List<PartitionAssignmentState> partitionAssignmentStates = topicConsumerGroupState
                        .getPartitionAssignmentStates();
                partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));

                TopicGroup topicGroup = new TopicGroup(clusterInfo.getId().toString(), topic, groupId, Constants.ConsumerType.BROKER);
                KafkaCenterGroupState state = judgeConsumerGroupState(topicGroup, topicConsumerGroupState);
                topicConsumerGroupState.setKafkaCenterGroupState(state);

                Map<Integer, Long> logSizeMap = this.getPartitionLogSizeMap(clusterInfo, groupId, topic);
                for (PartitionAssignmentState partitionAssignmentState : partitionAssignmentStates) {
                    int partition = partitionAssignmentState.getPartition();
                    long logEndOffset = logSizeMap.get(partition);
                    partitionAssignmentState.setLogEndOffset(logEndOffset);
                    partitionAssignmentState.setLag(getLag(partitionAssignmentState.getOffset(), logEndOffset));
                }
            });
        } catch (Exception e) {
            LOG.error("getBrokerConsumerOffsets has error：", e);
        }
        return topicConsumerGroupStates;
    }

    /**
     * zk消费 方式offset获取
     *
     * @param clusterInfo
     * @param topic
     * @return
     */
    private List<TopicConsumerGroupState> getZKConsumerOffsets(ClusterInfo clusterInfo, String topic) {
        final List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
        Set<String> zkGroups = zkService.getZK(clusterInfo.getId().toString()).listTopicGroups(topic);
        if (zkGroups == null || zkGroups.isEmpty()) {
            return topicConsumerGroupStates;
        }
        zkGroups.forEach(group -> {
            Map<String, Map<String, String>> zkConsumerOffsets = zkService.getZK(clusterInfo.getId().toString()).getZKConsumerOffsets(group,
                    topic);
            if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
                return;
            }
            Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterInfo.getBroker(), group, topic);
            if (logSizeMap == null || logSizeMap.isEmpty()) {
                return;
            }

            TopicConsumerGroupState topicConsumerGroupState = new TopicConsumerGroupState(group, Constants.ConsumerType.ZK);

            List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();

            Map<String, Long> logSizeTempMap = new HashMap<>(logSizeMap.size());
            logSizeMap.forEach((k, v) -> logSizeTempMap.put(k.partition() + Constants.Symbol.EMPTY_STR, v));

            zkConsumerOffsets.forEach((patition, topicDesribe) -> {
                Long logEndOffset = logSizeTempMap.get(patition);
                if (logEndOffset == null) {
                    return;
                }
                String owner = topicDesribe.get(BrokerConfig.OWNER);
                long offset = Long.parseLong(topicDesribe.get(TopicConfig.OFFSET));
                PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
                partitionAssignmentState.setClientId(owner);
                partitionAssignmentState.setGroup(group);
                partitionAssignmentState.setLogEndOffset(logEndOffset);
                partitionAssignmentState.setTopic(topic);
                partitionAssignmentState.setOffset(offset);
                partitionAssignmentState.setPartition(Integer.parseInt(patition));
                partitionAssignmentState.setLag(getLag(offset, logEndOffset));

                partitionAssignmentStates.add(partitionAssignmentState);
            });

            partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));

            topicConsumerGroupState.setPartitionAssignmentStates(partitionAssignmentStates);

            TopicGroup topicGroup = new TopicGroup(clusterInfo.getId().toString(), topic, group, topicConsumerGroupState.getConsumerMethod());
            KafkaCenterGroupState state = judgeConsumerGroupState(topicGroup, topicConsumerGroupState);
            topicConsumerGroupState.setKafkaCenterGroupState(state);

            topicConsumerGroupStates.add(topicConsumerGroupState);
        });
        return topicConsumerGroupStates;
    }

    /**
     * group粒度，获取zk、broker两种消费方式的GroupTopicConsumerState
     *
     * @param consumerGroup
     * @param clusterInfo
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public List<GroupTopicConsumerState> describeConsumerGroupByGroup(String consumerGroup, ClusterInfo clusterInfo) throws InterruptedException, ExecutionException, TimeoutException {
        List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
        // broker消费 方式offset获取
        List<GroupTopicConsumerState> brokerTopicConsumerGroupStates = this.getBrokerConsumerOffsetsByGroup(clusterInfo, consumerGroup);
        if (brokerTopicConsumerGroupStates != null) {
            groupConsumerStates.addAll(brokerTopicConsumerGroupStates);
        }
        // zk消费方式offset获取
        List<GroupTopicConsumerState> zkTopicConsumerGroupStates = this.getZKConsumerOffsetsByGroup(clusterInfo, consumerGroup);
        if (zkTopicConsumerGroupStates != null) {
            groupConsumerStates.addAll(zkTopicConsumerGroupStates);
        }
        return groupConsumerStates;
    }

    /**
     * group粒度获取zk消费方式的GroupTopicConsumerState
     *
     * @param clusterInfo
     * @param consumerGroup
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    List<GroupTopicConsumerState> getZKConsumerOffsetsByGroup(ClusterInfo clusterInfo, String consumerGroup) throws InterruptedException, ExecutionException, TimeoutException {

        Set<String> topics = zkService.getZK(clusterInfo.getId().toString()).listTopicsByGroup(consumerGroup);
        if (topics == null || topics.isEmpty()) {
            return null;
        }
        final List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
        for (String topic : topics) {
            Map<String, Map<String, String>> zkConsumerOffsets = zkService.getZK(clusterInfo.getId().toString()).getZKConsumerOffsets(consumerGroup, topic);
            if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
                return null;
            }
            GroupTopicConsumerState groupConsumerState = new GroupTopicConsumerState(consumerGroup, topic, Constants.ConsumerType.ZK);

            Map<Integer, Long> logSizeMap = this.getPartitionLogSizeMap(clusterInfo, consumerGroup, topic);
            List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();

            zkConsumerOffsets.forEach((partition, topicDesribe) -> {
                Long logEndOffset = logSizeMap.get(partition);
                if (logEndOffset == null) {
                    return;
                }
                String owner = topicDesribe.get(BrokerConfig.OWNER);
                long offset = Long.parseLong(topicDesribe.get(TopicConfig.OFFSET));
                PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
                partitionAssignmentState.setClientId(owner);
                partitionAssignmentState.setLogEndOffset(logEndOffset);
                partitionAssignmentState.setTopic(topic);
                partitionAssignmentState.setOffset(offset);
                partitionAssignmentState.setPartition(Integer.parseInt(partition));
                partitionAssignmentState.setLag(getLag(offset, logEndOffset));

                partitionAssignmentStates.add(partitionAssignmentState);
            });
            partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
            groupConsumerState.setPartitionAssignmentStates(partitionAssignmentStates);
            groupConsumerState.setKafkaCenterGroupState(judgeStateByGroupTopicConsumerState(groupConsumerState, clusterInfo));
            groupConsumerStates.add(groupConsumerState);
        }
        return groupConsumerStates;
    }

    /**
     * group粒度获取broker消费方式的GroupTopicConsumerState
     *
     * @param clusterInfo
     * @param consumerGroup
     * @return
     */
    List<GroupTopicConsumerState> getBrokerConsumerOffsetsByGroup(ClusterInfo clusterInfo, String consumerGroup) {
        List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();

        try {
            groupConsumerStates = kafkaAdminService.getKafkaAdmins(clusterInfo.getId().toString()).describeConsumerGroupsByGroup(consumerGroup);
            if (groupConsumerStates == null || groupConsumerStates.isEmpty() || groupConsumerStates.get(0).getTopic() == null) {
                return groupConsumerStates;
            }
            // 填充lag/logEndOffset
            groupConsumerStates.forEach(groupConsumerState -> {
                List<PartitionAssignmentState> partitionAssignmentStates = groupConsumerState
                        .getPartitionAssignmentStates();
                partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
                String topic = groupConsumerState.getTopic();
                //填充lag/logEndOffset
                Map<Integer, Long> partitionLogSizeMap = getPartitionLogSizeMap(clusterInfo, consumerGroup, topic);
                for (PartitionAssignmentState partitionAssignmentState : partitionAssignmentStates) {
                    long logEndOffset = partitionLogSizeMap.get(partitionAssignmentState.getPartition());
                    partitionAssignmentState.setLogEndOffset(logEndOffset);
                    partitionAssignmentState.setLag(getLag(partitionAssignmentState.getOffset(), logEndOffset));
                }
                KafkaCenterGroupState state = judgeStateByGroupTopicConsumerState(groupConsumerState, clusterInfo);
                groupConsumerState.setKafkaCenterGroupState(state);
            });
        } catch (Exception e) {
            LOG.error("getBrokerConsumerOffsetsByGroup has error", e);
        }

        return groupConsumerStates;
    }


    public Set<MeterMetric> getBrokerMetric(String clusterID, String topic) {
        Set<MeterMetric> result = new HashSet<>();
        try {
            List<BrokerInfo> zkBrokers = zkService.getZK(clusterID).getBrokers();
            List<BrokerInfo> brokers = getReplicaBroker(clusterID, topic, zkBrokers);
            Map<String, Set<MeterMetric>> metricEveryBroker = JmxCollector.getInstance().metricEveryBrokerTopic(brokers, topic);
            Map<String, MeterMetric> mergeBrokersMetric = JmxCollector.getInstance().mergeBrokersMetric(metricEveryBroker);
            result = mergeBrokerMetricRest(mergeBrokersMetric, clusterID);
        } catch (Exception e) {
            LOG.error("getBrokerMetric has error", e);
        }
        return result;
    }

    private List<BrokerInfo> getReplicaBroker(String clientId, String topic, List<BrokerInfo> brokerInfos) {
        Map<Integer, BrokerInfo> result = new HashMap<>();
        TopicDescription topicDescription = kafkaAdminService.getKafkaAdmins(clientId).descTopics(Collections.singletonList(topic)).get(topic);
        List<TopicPartitionInfo> topicPartitionInfos = Objects.isNull(topicDescription) ? new ArrayList<>() : topicDescription.partitions();
        for (TopicPartitionInfo topicPartitionInfo : topicPartitionInfos) {
            Node leader = topicPartitionInfo.leader();
            if (!result.containsKey(leader.id())) {
                BrokerInfo brokerInfo = new BrokerInfo();
                brokerInfo.setBid(leader.id());
                brokerInfo.setHost(leader.host());
                brokerInfo.setPort(leader.port());
                result.put(leader.id(), brokerInfo);
            }
        }
        brokerInfos.forEach(brokerInfo -> {
            if (result.containsKey(brokerInfo.getBid())) {
                BrokerInfo broker = result.get(brokerInfo.getBid());
                broker.setJmxPort(brokerInfo.getJmxPort());
                result.put(brokerInfo.getBid(), broker);
            }
        });
        return new ArrayList<>(result.values());
    }

    public Map<String, Object> metrics(String clusterID) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<BrokerInfo> brokers = zkService.getZK(clusterID).getBrokers();
            Map<String, Set<MeterMetric>> metricEveryBroker = JmxCollector.getInstance().metricEveryBroker(brokers);
            Map<String, MeterMetric> mergeBrokersMetric = JmxCollector.getInstance().mergeBrokersMetric(metricEveryBroker);
            Set<HomeService.MetricVo> metricVos = brokerMetricRest(metricEveryBroker);
            Set<MeterMetric> metricSet = mergeBrokerMetricRest(mergeBrokersMetric, clusterID);
            map.put(Constants.KeyStr.SINGLE, metricVos);
            map.put(Constants.KeyStr.COUNT, metricSet);
        } catch (Exception e) {
            LOG.error("Get metricData Failed!,", e);
        }
        return map;
    }

    private Set<MeterMetric> mergeBrokerMetricRest(Map<String, MeterMetric> meterMetricMap, String clusterId) {
        Set<MeterMetric> metricSet = new TreeSet<>(Comparator.comparingInt(o -> o.getMetricName().length()));
        meterMetricMap.forEach((metricName, metricObj) -> {
            metricObj.setMetricName(metricName);
            metricObj.setClusterID(clusterId);
            metricSet.add(metricObj);
        });


        return metricSet;
    }

    private Set<HomeService.MetricVo> brokerMetricRest(Map<String, Set<MeterMetric>> metricBroker) {
        Set<HomeService.MetricVo> metricVos = new HashSet<>();
        metricBroker.forEach((host, meterObjs) -> {
            HomeService.MetricVo metricVo = new HomeService.MetricVo();
            metricVo.setBroker(host);
            meterObjs.forEach(meterMetric -> {
                metricVo.setJmxPort(meterMetric.getJmxPort());
                metricVo.setPort(meterMetric.getPort());
                switch (meterMetric.getMetricName()) {
                    case BrokerConfig.BYTES_IN_PER_SEC: {
                        metricVo.setByteIn(meterMetric.getMetricName());
                        metricVo.setByteInOneMin(meterMetric.getOneMinuteRate());
                    }
                    break;
                    case BrokerConfig.BYTES_OUT_PER_SEC: {
                        metricVo.setByteOut(meterMetric.getMetricName());
                        metricVo.setByteOutOneMin(meterMetric.getOneMinuteRate());
                    }
                    break;
                    case BrokerConfig.MESSAGES_IN_PER_SEC: {
                        metricVo.setMessageIn(meterMetric.getMetricName());
                        metricVo.setMsgInOneMin(String.valueOf(meterMetric.getOneMinuteRate()));
                    }
                    break;
                    default:
                        break;
                }
            });
            metricVos.add(metricVo);
        });
        return metricVos;
    }


    public JSONArray getGroupLag(Long clusterId, String clusterName) {
        JSONArray results = new JSONArray();
        List<OffsetStat> list = eService.getRequestBody(clusterId.toString());
        Map<String, JSONObject> map = new HashMap<>();
        list.forEach(offset -> {
            if (offset.getLag() != null && offset.getLag() > 0) {
                String key = clusterName + Constants.Symbol.VERTICAL_STR + offset.getClusterId() + Constants.Symbol.VERTICAL_STR + offset.getTopic() + Constants.Symbol.VERTICAL_STR + offset.getGroup();
                if (!map.containsKey(key)) {
                    JSONObject json = new JSONObject();
                    json.put(TopicConfig.LAG, offset.getLag());
                    json.put(BrokerConfig.GROUP, offset.getGroup());
                    json.put(BrokerConfig.TOPIC, offset.getTopic());
                    json.put(Constants.KeyStr.CLUSTERID, offset.getClusterId());
                    json.put(Constants.KeyStr.CLUSTER_NAME, clusterName);
                    results.add(json);
                    map.put(key, json);
                }

            }

        });

        return results;
    }

    /**
     * 根据集群信息查询所有group
     *
     * @param clusters
     * @return
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<ClusterGroup> listGroupsByCluster(List<ClusterInfo> clusters, boolean removeSameGroup) throws
            InterruptedException, ExecutionException, TimeoutException {
        List<ClusterGroup> clusterGroups = new ArrayList<>();
        Set<String> brokerGroups;
        Set<String> zkGroups;
        for (ClusterInfo cluster : clusters) {
            // BROKER
            brokerGroups = kafkaAdminService.getKafkaAdmins(String.valueOf(cluster.getId())).listConsumerGroups();
            // ZK
            zkGroups = zkService.getZK(String.valueOf(cluster.getId())).listConsumerGroups();
            // 将groupList中空字符串剔除
            brokerGroups.removeAll(Collections.singleton(""));
            zkGroups.removeAll(Collections.singleton(""));
            for (String group : brokerGroups) {
                ClusterGroup clusterGroup = new ClusterGroup();
                clusterGroup.setConsummerGroup(group);
                clusterGroup.setClusterID(cluster.getId());
                clusterGroup.setClusterName(cluster.getName());
                clusterGroup.setConsumereApi(Constants.ConsumerType.BROKER);
                clusterGroups.add(clusterGroup);
            }
            for (String group : zkGroups) {
                if (removeSameGroup && brokerGroups.contains(group)) {
                    continue;
                }
                ClusterGroup clusterGroup = new ClusterGroup();
                clusterGroup.setConsummerGroup(group);
                clusterGroup.setClusterID(cluster.getId());
                clusterGroup.setClusterName(cluster.getName());
                clusterGroup.setConsumereApi(Constants.ConsumerType.ZK);
                clusterGroups.add(clusterGroup);

            }
        }
        return clusterGroups;
    }

    /**
     * @param user
     * @param type
     * @return
     */
    public List<MonitorTopic> listUserFavorite(UserInfo user, String type) {
        List<MonitorTopic> monitorTopics = new ArrayList<>();
        List<org.nesc.ec.bigdata.model.Collections> list = collectionService.list(user.getId(), type);
        if (!list.isEmpty()) {
            list.forEach(collection -> {
                MonitorTopic monitorTopic = new MonitorTopic();
                monitorTopic.setClusterID(collection.getCluster().getId());
                monitorTopic.setClusterName(collection.getCluster().getName());
                monitorTopic.setTopicName(collection.getName());
                monitorTopic.setCollections(true);
                monitorTopics.add(monitorTopic);
            });
        }

        return monitorTopics;
    }


    public KafkaCenterGroupState judgeStateByGroupTopicConsumerState(GroupTopicConsumerState groupTopicConsumerState, ClusterInfo clusterInfo) {
        TopicConsumerGroupState topicConsumerGroupState = new TopicConsumerGroupState(groupTopicConsumerState.getGroup(), groupTopicConsumerState.getConsumerMethod());
        topicConsumerGroupState.setConsumerGroupState(groupTopicConsumerState.getConsumerGroupState());
        topicConsumerGroupState.setSimpleConsumerGroup(groupTopicConsumerState.isSimpleConsumerGroup());
        topicConsumerGroupState.setPartitionAssignmentStates(groupTopicConsumerState.getPartitionAssignmentStates());
        TopicGroup topicGroup = new TopicGroup(clusterInfo.getId().toString(), groupTopicConsumerState.getTopic(), groupTopicConsumerState.getGroup(), groupTopicConsumerState.getConsumerMethod());
        KafkaCenterGroupState state = judgeConsumerGroupState(topicGroup, topicConsumerGroupState);
        return state;
    }


    /**
     * 统计判断consumer消费状态
     *
     * @return KafkaCenterGroupState[active, unknown, dead]
     */
    public KafkaCenterGroupState judgeConsumerGroupState(TopicGroup topicGroup, TopicConsumerGroupState topicConsumerGroupState) {

        String key = topicGroup.generateKey();
        boolean isSimpleConsumerGroup = topicConsumerGroupState.isSimpleConsumerGroup();
        ConsumerGroupState consumerGroupState = topicConsumerGroupState.getConsumerGroupState();
        List<PartitionAssignmentState> partitionAssignmentStates = topicConsumerGroupState.getPartitionAssignmentStates();
        KafkaCenterGroupState status = KafkaCenterGroupState.DEAD;
        String clientId = !CollectionUtils.isEmpty(partitionAssignmentStates) ? partitionAssignmentStates.get(0).getClientId() : null;

        if (Constants.ConsumerType.ZK.equalsIgnoreCase(topicGroup.getConsumerType())) {
            if (clientIdNonNull(clientId)) {
                status = KafkaCenterGroupState.ACTIVE;
            }
        } else {
            if (isSimpleConsumerGroup) {
                HomeCache.ConsumerLagCache consumerLagCache = HomeCache.consumerLagCacheMap.getOrDefault(key, null);
                long offset = partitionAssignmentStates.stream().mapToLong(PartitionAssignmentState::getOffset).sum();
                if (!Objects.isNull(consumerLagCache)) {
                    long cacheOffset = consumerLagCache.getOffset();
                    if (offset != cacheOffset) {
                        status = KafkaCenterGroupState.ACTIVE;
                    }
                } else {
                    status = KafkaCenterGroupState.UNKNOWN;
                }

            } else {
                if (consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.STABLE.name())) {
                    status = clientIdNonNull(clientId) ? KafkaCenterGroupState.ACTIVE : KafkaCenterGroupState.DEAD;
                } else if (consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.DEAD.name()) ||
                        consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.EMPTY.name())) {
                    status = KafkaCenterGroupState.DEAD;
                } else {
                    status = clientIdNonNull(clientId) ? KafkaCenterGroupState.ACTIVE : KafkaCenterGroupState.UNKNOWN;
                }
            }
        }
        return status;
    }

    /**
     * 获取topic 每个partition对应的LogSize
     *
     * @param clusterInfo
     * @param groupId
     * @param topic
     * @return
     */
    private Map<Integer, Long> getPartitionLogSizeMap(ClusterInfo clusterInfo, String groupId, String topic) {
        Map<Integer, Long> logSizeTempMap = new HashMap<>();
        Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterInfo.getBroker(), groupId, topic);
        if (logSizeMap == null || logSizeMap.isEmpty()) {
            return logSizeTempMap;
        }
        logSizeMap.forEach((k, v) -> {
            logSizeTempMap.put(k.partition(), v);
        });
        return logSizeTempMap;
    }

    private boolean clientIdNonNull(String str) {
        return (!Objects.isNull(str) && !Objects.equals("", str));
    }

    private Map<Long, Set<String>> transfromToMap(List<org.nesc.ec.bigdata.model.Collections> collections) {
        Map<Long, Set<String>> xxx = new HashMap<>();
        Set<String> xx;
        for (org.nesc.ec.bigdata.model.Collections collect : collections) {
            Long clusterId = collect.getCluster().getId();
            if (xxx.containsKey(clusterId)) {
                xx = xxx.get(clusterId);
            } else {
                xx = new HashSet<>();
            }
            xx.add(collect.getName());
            xxx.put(clusterId, xx);
        }
        return xxx;
    }

    private long getLag(Long offset, Long leo) {

        // 如果offset为-1,则Lag计算值同样为-1
        if (offset < 0) {
            return -1L;
        }
        long lag = leo - offset;
        return lag < 0 ? 0 : lag;
    }

    private PartitionAssignmentState generatorPartitionAssignmentState(
            Entry<TopicPartition, OffsetAndMetadata> consumerPartitionOffset, String topic, String groupId) {
        TopicPartition topicPartition = consumerPartitionOffset.getKey();
        OffsetAndMetadata offsetAndMetadata = consumerPartitionOffset.getValue();
        PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
        partitionAssignmentState.setPartition(topicPartition.partition());
        partitionAssignmentState.setTopic(topic);
        partitionAssignmentState.setGroup(groupId);
        partitionAssignmentState.setOffset(Optional.ofNullable(offsetAndMetadata.offset()).orElse(-1L));
        return partitionAssignmentState;
    }

}
