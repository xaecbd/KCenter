package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.*;
import org.nesc.ec.bigdata.common.util.JmxCollector;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterGroup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorTopic;
import org.nesc.ec.bigdata.model.UserInfo;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午2:40:35
 * @version 1.0
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
	AlertService alertService;

	@Autowired
	CollectionService collectionService;

	@Autowired
	ElasticsearchService eService;

	public Map<Long,Set<String>> transfromToMap(List<org.nesc.ec.bigdata.model.Collections> collections){
		Map<Long,Set<String>> xxx = new HashMap<>();
		Set<String> xx;
		for(org.nesc.ec.bigdata.model.Collections collect:collections) {
			Long clusterId = collect.getCluster().getId();
			if(xxx.containsKey(clusterId)) {
				xx = xxx.get(clusterId);
			}else {
				xx = new HashSet<>();
			}
			xx.add(collect.getName());
			xxx.put(clusterId, xx);
		}
		return xxx;
	}

	public List<MonitorTopic> getTopicList(List<ClusterInfo> clusterInfos, UserInfo user, String type) {
		List<MonitorTopic> monitorTopics = new ArrayList<>();
		List<org.nesc.ec.bigdata.model.Collections> collections = collectionService.list(user.getId(), type);
		Map<Long,Set<String>> xx = this.transfromToMap(collections);
		for (ClusterInfo info : clusterInfos) {
			try {
				Set<String> topicMap = kafkaAdminService.getKafkaAdmins(info.getId().toString()).listTopics();
				topicMap.forEach(topic -> {
					MonitorTopic monitorTopic = new MonitorTopic();
					monitorTopic.setClusterID(info.getId());
					monitorTopic.setClusterName(info.getName());
					monitorTopic.setTopicName(topic);
					if(xx.containsKey(info.getId())) {
						Set<String> str = xx.get(info.getId());
						if(str.contains(topic)) {
							monitorTopic.setCollections(true);
						}
					}
					monitorTopics.add(monitorTopic);
				});

			} catch (Exception e) {
				LOG.error("get topicList by user",e);
				MonitorTopic monitorTopic = new MonitorTopic();
				monitorTopics.add(monitorTopic);
			}
		}
		return monitorTopics;
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
			LOG.error("get topicList by clusterId",e);
		}

		return monitorTopics;
	}

	/**
	 * 获取topic 消费offset情况详情
	 *
	 * @param topic
	 * @param clusterID
	 * @return
	 */
	public List<TopicConsumerGroupState> describeConsumerGroups(String topic, String clusterID) {
		List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		// broker消费 方式offset获取
		List<TopicConsumerGroupState> brokerTopicConsumerGroupStates = this.getBrokerConsumerOffsets(clusterID, topic);

		topicConsumerGroupStates.addAll(brokerTopicConsumerGroupStates);
		// zk消费 方式offset获取
		List<TopicConsumerGroupState> zkTopicConsumerGroupStates = new ArrayList<>();
		zkTopicConsumerGroupStates = this.getZKConsumerOffsets(clusterID, topic);
		topicConsumerGroupStates.addAll(zkTopicConsumerGroupStates);
		return topicConsumerGroupStates;
	}


	public List<TopicConsumerGroupState> describeConsumerGroupsBroker(String topic, String clusterID) {
		List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		// broker消费 方式offset获取
		List<TopicConsumerGroupState> brokerTopicConsumerGroupStates = this.getBrokerConsumerOffsets(clusterID, topic);

		topicConsumerGroupStates.addAll(brokerTopicConsumerGroupStates);
		return topicConsumerGroupStates;
	}

	public List<TopicConsumerGroupState> describeConsumerGroupsZK(String topic, String clusterID) {
		List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		// zk消费 方式offset获取
		List<TopicConsumerGroupState> zkTopicConsumerGroupStates = this.getZKConsumerOffsets(clusterID, topic);
		topicConsumerGroupStates.addAll(zkTopicConsumerGroupStates);
		return topicConsumerGroupStates;
	}

	/**
	 * 获取集群所有topic 消费offset情况详情
	 *
	 * @param clusterID
	 * @return
	 */
	public Map<String, List<PartitionAssignmentState>> describeConsumerGroups(String clusterID) {
		Map<String, List<PartitionAssignmentState>> topicsOffsetsMap = new HashMap<>();

		List<PartitionAssignmentState> brokerPartitionAssignmentState = new ArrayList<>();

		List<PartitionAssignmentState> zkPartitionAssignmentState = new ArrayList<>();
		try {
			// 获取broker消费方式消费消息
			brokerPartitionAssignmentState = this.getBrokerConsumerOffsetsByClusterID(clusterID);
			// 获取zk消费方式消费消息
			zkPartitionAssignmentState = this.getZKConsumerOffsetsByClusterID(clusterID);
		} catch (Exception e) {
			LOG.error("describeConsumerGroups has error.", e);
		}

		topicsOffsetsMap.put("broker", brokerPartitionAssignmentState);
		topicsOffsetsMap.put("zk", zkPartitionAssignmentState);
		return topicsOffsetsMap;
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
	 * @param clusterID
	 * @param topic
	 * @return
	 */
	private List<TopicConsumerGroupState> getBrokerConsumerOffsets(String clusterID, String topic) {
		List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		try {
			topicConsumerGroupStates = kafkaAdminService.getKafkaAdmins(clusterID).describeConsumerGroups(topic);
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
				Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterID, groupId, topic);
				if (logSizeMap == null || logSizeMap.isEmpty()) {
					return;
				}
				for (Entry<TopicPartition, Long> entry : logSizeMap.entrySet()) {
					if (entry == null) {
						continue;
					}
					long logEndOffset = entry.getValue();
					for (PartitionAssignmentState partitionAssignmentState : partitionAssignmentStates) {
						if (partitionAssignmentState.getPartition() == entry.getKey().partition()) {
							partitionAssignmentState.setLogEndOffset(logEndOffset);
							partitionAssignmentState.setLag(getLag(partitionAssignmentState.getOffset(), logEndOffset));
						}
					}
				}
			});

		} catch (Exception e) {
			LOG.error("getBrokerConsumerOffsets has error：",e);
		}
		return topicConsumerGroupStates;
	}

	/**
	 * 根据指定集群ID查询集群所有group，topic的消费延迟情况 获取所有group 所有topic， 消费情况 优化查询速度，减少网络交互
	 * 根据group去查询offset，针对不存在group的topic不做收集， 理论上能提高速度 //第一步获取集群所有group
	 * //第二部根据group获取ConsumerGroupOffsets //第三部通过Consumer获取客户端可见的LogSize
	 *
	 *
	 * 该方法仅供收集job使用，未包含member等信息
	 *
	 * @param clusterId
	 * @return
	 */
	public List<PartitionAssignmentState> getBrokerConsumerOffsetsByClusterID(String clusterId) {

		List<PartitionAssignmentState> result = new ArrayList<>();

		KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);

		try {
			// 第一步获取集群所有group
			Set<String> groupIds = kafkaAdmins.listConsumerGroups();
			Map<String,Boolean> map = new HashMap<>();
			Map<String, ConsumerGroupDescription> groupDetails = kafkaAdmins.getAdminClient().describeConsumerGroups(groupIds).all()
					.get(30, TimeUnit.SECONDS);
			groupDetails.forEach((key, description) -> {
				if (description.state().equals(ConsumerGroupState.STABLE) || description.state().equals(ConsumerGroupState.UNKNOWN)) {
					if (!description.members().isEmpty()) {
						map.put(key, true);
					}
				} else {
					map.put(key, false);
				}
			});
			// 第二部根据group获取ConsumerGroupOffsets
			groupIds.forEach(groupId -> {
				try {
					Map<TopicPartition, OffsetAndMetadata> partitionsToOffsetAndMetadata = kafkaAdmins.getAdminClient()
							.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata()
							.get(30, TimeUnit.SECONDS);
					if (partitionsToOffsetAndMetadata == null || partitionsToOffsetAndMetadata.isEmpty()) {
						return;
					}
					Map<String, List<PartitionAssignmentState>> consumerPatitionOffsetMap = new HashMap<>();

					partitionsToOffsetAndMetadata.entrySet().forEach(entry -> {
						String topic = entry.getKey().topic();
						List<PartitionAssignmentState> partitionAssignmentStates;
						if (consumerPatitionOffsetMap.containsKey(topic)) {
							partitionAssignmentStates = consumerPatitionOffsetMap.get(topic);
						} else {
							partitionAssignmentStates = new ArrayList<>();
						}
						PartitionAssignmentState partitionAssignmentState = this
								.generatorPartitionAssignmentState(entry, topic, groupId);
						partitionAssignmentStates.add(partitionAssignmentState);

						consumerPatitionOffsetMap.put(topic, partitionAssignmentStates);
					});

					consumerPatitionOffsetMap.forEach((topic, partitionAssignmentStates) -> {

						if (partitionAssignmentStates == null || partitionAssignmentStates.isEmpty()) {
							return;
						}
						// 第三部通过Consumer获取客户端可见的LogSize
						Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterId, groupId,
								topic);
						if (logSizeMap == null || logSizeMap.isEmpty()) {
							return;
						}
						Map<Integer, Long> logSizeTempMap = new HashMap<>(logSizeMap.size());
						logSizeMap.forEach((k, v) -> {
							logSizeTempMap.put(k.partition(), v);
						});
						for (PartitionAssignmentState partitionAssignmentState : partitionAssignmentStates) {
							Long logEndOffset = logSizeTempMap.get(partitionAssignmentState.getPartition());
							if (logEndOffset == null) {
								continue;
							}
							partitionAssignmentState.setClientId(map.get(groupId)?"0":null);
							partitionAssignmentState.setLogEndOffset(logEndOffset);
							partitionAssignmentState.setLag(getLag(partitionAssignmentState.getOffset(), logEndOffset));
							result.add(partitionAssignmentState);
						}
					});

				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					LOG.error("collector consumer lag job error.", e);
				}
			});
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOG.error("collector consumer lag job error.", e);
		}
		result.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
		return result;
	}

	private PartitionAssignmentState generatorPartitionAssignmentState(
			Entry<TopicPartition, OffsetAndMetadata> consumerPatitionOffset, String topic, String groupId) {
		TopicPartition topicPartition = consumerPatitionOffset.getKey();
		OffsetAndMetadata offsetAndMetadata = consumerPatitionOffset.getValue();
		PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
		partitionAssignmentState.setPartition(topicPartition.partition());
		partitionAssignmentState.setTopic(topic);
		partitionAssignmentState.setGroup(groupId);
		partitionAssignmentState.setOffset(Optional.ofNullable(offsetAndMetadata.offset()).orElse(-1L));
		return partitionAssignmentState;
	}

	/**
	 * zk消费 方式offset获取
	 *
	 * @param clusterID
	 * @param topic
	 * @return
	 */
	private List<TopicConsumerGroupState> getZKConsumerOffsets(String clusterID, String topic) {
		final List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		Set<String> zkGroups = zkService.getZK(clusterID).listTopicGroups(topic);
		if (zkGroups == null || zkGroups.isEmpty()) {
			return topicConsumerGroupStates;
		}
		zkGroups.forEach(group -> {
			Map<String, Map<String, String>> zkConsumerOffsets = zkService.getZK(clusterID).getZKConsumerOffsets(group,
					topic);
			if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
				return;
			}
			Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterID, group, topic);
			if (logSizeMap == null || logSizeMap.isEmpty()) {
				return;
			}

			TopicConsumerGroupState topicConsumerGroupState = new TopicConsumerGroupState();
			topicConsumerGroupState.setGroupId(group);
			topicConsumerGroupState.setConsumerMethod(Constants.KeyStr.zk);

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
			topicConsumerGroupStates.add(topicConsumerGroupState);
		});
		return topicConsumerGroupStates;
	}

	/**
	 * 指定集群查询zk消费方式的lag信息
	 *
	 * @param clusterID
	 * @return
	 */
	private List<PartitionAssignmentState> getZKConsumerOffsetsByClusterID(String clusterID) {
		List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();

		Map<String, Map<String, Map<String, String>>> zkConsumerOffsets = zkService.getZK(clusterID)
				.getZKConsumerOffsets();
		if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
			return partitionAssignmentStates;
		}
		zkConsumerOffsets.forEach((group, topicOffsets) -> {
			if (topicOffsets == null || topicOffsets.isEmpty()) {
				return;
			}
			topicOffsets.forEach((topic, patitionsMap) -> {

				Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterID, group, topic);
				if (logSizeMap == null || logSizeMap.isEmpty()) {
					return;
				}

				try {
					logSizeMap.forEach((topicPartition, logEndOffset) -> {
						if (logEndOffset == null) {
							return;
						}
						PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
						partitionAssignmentState.setGroup(group);
						partitionAssignmentState.setLogEndOffset(logEndOffset);
						partitionAssignmentState.setTopic(topic);
						int partition = topicPartition.partition();
						String[] states = null;
						if(patitionsMap.containsKey(partition + Constants.Symbol.EMPTY_STR)){
							states = patitionsMap.get(partition + Constants.Symbol.EMPTY_STR).split("\\|");
						}
						String offsetStr = states==null||states.length==0?null:states[0];
						long offset = Long.parseLong(java.util.Optional.ofNullable(offsetStr).orElse("-1"));
						String client = states==null||states.length<1?null:states[1];
						partitionAssignmentState.setClientId(client);
						partitionAssignmentState.setOffset(offset);
						partitionAssignmentState.setPartition(partition);
						partitionAssignmentState.setLag(getLag(offset, logEndOffset));
						partitionAssignmentStates.add(partitionAssignmentState);
					});
				} catch (Exception e) {
					LOG.error("topic:{} group:{} patitionsMap:{} getZKConsumerOffsets", topic, group,
							patitionsMap.toString(), e);
				}
			});
		});
		partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
		return partitionAssignmentStates;
	}

	private long getLag(Long offset, Long leo) {

		// 如果offset为-1,则Lag计算值同样为-1
		if (offset < 0) {
			return -1L;
		}
		long lag = leo - offset;
		return lag < 0 ? 0 : lag;
	}

	public Set<MeterMetric> getBrokerMetric(String clusterID, String topic) {
		Set<MeterMetric> result = new HashSet<>();
		try {
			List<BrokerInfo> zkBrokes = zkService.getZK(clusterID).getBrokers();
			List<BrokerInfo> brokers = getReplicaBroker(clusterID,topic,zkBrokes);
			Map<String,Set<MeterMetric>> metricEveryBroker= JmxCollector.getInstance().metricEveryBrokerTopic(brokers,topic);
			Map<String,MeterMetric> mergeBrokersMetric = JmxCollector.getInstance().mergeBrokersMetric(metricEveryBroker);
			result = mergeBrokerMetricRest(mergeBrokersMetric,clusterID);
		} catch (Exception e) {
			LOG.error("getBrokerMetric has error",e);
		}
		return result;
	}

	private List<BrokerInfo> getReplicaBroker(String clientId, String topic,List<BrokerInfo> brokerInfos){
		Map<Integer,BrokerInfo> result = new HashMap<>();
		TopicDescription topicDescription  =  kafkaAdminService.getKafkaAdmins(clientId).descTopics(Collections.singletonList(topic)).get(topic);
		List<TopicPartitionInfo> topicPartitionInfos = topicDescription.partitions();
		for (TopicPartitionInfo topicPartitionInfo:topicPartitionInfos){
			Node leader = topicPartitionInfo.leader();
			if(!result.containsKey(leader.id())){
				BrokerInfo brokerInfo = new BrokerInfo();
				brokerInfo.setBid(leader.id());
				brokerInfo.setHost(leader.host());
				brokerInfo.setPort(leader.port());
				result.put(leader.id(),brokerInfo);
			}
		}
		brokerInfos.forEach(brokerInfo -> {
			if(result.containsKey(brokerInfo.getBid())){
				BrokerInfo broker = result.get(brokerInfo.getBid());
				broker.setJmxPort(brokerInfo.getJmxPort());
				result.put(brokerInfo.getBid(),broker);
			}
		});
		return new ArrayList<>(result.values());
	}

	public Map<String,Object> metrics(String clusterID) {
		Map<String,Object> map = new HashMap<>();
		try {
			List<BrokerInfo> brokers = zkService.getZK(clusterID).getBrokers();
			Map<String,Set<MeterMetric>> metricEveryBroker= JmxCollector.getInstance().metricEveryBroker(brokers);
			Map<String,MeterMetric> mergeBrokersMetric = JmxCollector.getInstance().mergeBrokersMetric(metricEveryBroker);
			Set<HomeService.MetricVo> metricVos = brokerMetricRest(metricEveryBroker);
			Set<MeterMetric> metricSet = mergeBrokerMetricRest(mergeBrokersMetric,clusterID);
			map.put(Constants.KeyStr.SINGLE,metricVos);
			map.put(Constants.KeyStr.COUNT,metricSet);
		} catch (Exception e) {
			LOG.error("Get metricData Faild!,",e);
		}
		return map;
	}

	private Set<MeterMetric> mergeBrokerMetricRest(Map<String,MeterMetric> meterMetricMap,String clusterId){
		Set<MeterMetric> metricSet = new HashSet<>();
		meterMetricMap.forEach((metricName,metricObj)->{
			metricObj.setMetricName(metricName);
			metricObj.setClusterID(clusterId);
			metricSet.add(metricObj);
		});
		return metricSet;
	}

	private  Set<HomeService.MetricVo> brokerMetricRest(Map<String,Set<MeterMetric>> metricBroker){
		Set<HomeService.MetricVo> metricVos = new HashSet<>();
		metricBroker.forEach((host,meterObjs)->{
			HomeService.MetricVo metricVo = new HomeService.MetricVo();
			metricVo.setBroker(host);
			meterObjs.forEach(meterMetric -> {
				metricVo.setJmxPort(meterMetric.getJmxPort());
				metricVo.setPort(meterMetric.getPort());
				switch (meterMetric.getMetricName()){
					case BrokerConfig.BYTES_IN_PER_SEC:{
						metricVo.setByteIn(meterMetric.getMetricName());
						metricVo.setByteInOneMin(meterMetric.getOneMinuteRate());
					}break;
					case BrokerConfig.BYTES_OUT_PER_SEC:{
						metricVo.setByteOut(meterMetric.getMetricName());
						metricVo.setByteOutOneMin(meterMetric.getOneMinuteRate());
					}break;
					case BrokerConfig.MESSAGES_IN_PER_SEC:{
						metricVo.setMessageIn(meterMetric.getMetricName());
						metricVo.setMsgInOneMin(String.valueOf(meterMetric.getOneMinuteRate()));
					}break;
					default:break;
				}
			});
			metricVos.add(metricVo);
		});
		return metricVos;
	}



	public JSONArray getGroupLag(Long clusterId, String clusterName) {
		JSONArray results = new JSONArray();
		List<OffsetStat> list = eService.getRequestBody(clusterId.toString());
		Map<String,JSONObject> map = new HashMap<>();
		list.forEach(offset -> {
			if (offset.getLag() > 0) {
				String key = clusterName+ Constants.Symbol.Vertical_STR+offset.getClusterId()+ Constants.Symbol.Vertical_STR+offset.getTopic()+ Constants.Symbol.Vertical_STR+offset.getGroup();
				if(!map.containsKey(key)) {
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
		for(ClusterInfo cluster : clusters) {
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
				clusterGroup.setConsumereApi(Constants.KeyStr.BROKER);
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
				clusterGroup.setConsumereApi(Constants.KeyStr.ZK);
				clusterGroups.add(clusterGroup);

			}
		}
		return clusterGroups;
	}
	/**
	 *
	 * @param user
	 * @param type
	 * @return
	 */
	public List<MonitorTopic> listUserFavorite(UserInfo user,String type) {
		List<MonitorTopic> monitorTopics = new ArrayList<>();
		List<org.nesc.ec.bigdata.model.Collections> list = collectionService.list(user.getId(), type);
		if(!list.isEmpty()) {
			list.forEach(collection->{
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

	/**
	 * group粒度，获取zk、broker两种消费方式的GroupTopicConsumerState
	 *
	 * @param consummerGroup
	 * @param clusterID
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public List<GroupTopicConsumerState> describeConsumerGroupByGroup(String consummerGroup, String clusterID) throws InterruptedException, ExecutionException, TimeoutException {
		List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
		// broker消费 方式offset获取
		List<GroupTopicConsumerState> brokerTopicConsumerGroupStates = this.getBrokerConsumerOffsetsByGroup(clusterID, consummerGroup);
		if(brokerTopicConsumerGroupStates != null) {
			groupConsumerStates.addAll(brokerTopicConsumerGroupStates);
		}
		// zk消费方式offset获取
		List<GroupTopicConsumerState> zkTopicConsumerGroupStates = this.getZKConsumerOffsetsByGroup(clusterID, consummerGroup);
		if(zkTopicConsumerGroupStates != null) {
			groupConsumerStates.addAll(zkTopicConsumerGroupStates);
		}
		return groupConsumerStates;
	}

	/**
	 * group粒度获取zk消费方式的GroupTopicConsumerState
	 *
	 * @param clusterID
	 * @param consummerGroup
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private List<GroupTopicConsumerState> getZKConsumerOffsetsByGroup(String clusterID, String consummerGroup) throws InterruptedException, ExecutionException, TimeoutException {
		Set<String> topics = zkService.getZK(clusterID).listTopicsByGroup(consummerGroup);
		if(topics == null || topics.isEmpty()) {
			return null;
		}
		final List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
		for(String topic : topics) {
			Map<String, Map<String, String>> zkConsumerOffsets = zkService.getZK(clusterID).getZKConsumerOffsets(consummerGroup,topic);
			if (zkConsumerOffsets == null || zkConsumerOffsets.isEmpty()) {
				return null;
			}
			Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterID, consummerGroup, topic);
			if (logSizeMap == null || logSizeMap.isEmpty()) {
				return null;
			}

			GroupTopicConsumerState groupConsumerState = new GroupTopicConsumerState();
			groupConsumerState.setTopic(topic);
			groupConsumerState.setConsumerMethod(Constants.KeyStr.zk);

			List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();

			Map<String, Long> logSizeTempMap = new HashMap<>(logSizeMap.size());
			logSizeMap.forEach((k, v) -> logSizeTempMap.put(k.partition() + "", v));

			zkConsumerOffsets.forEach((patition, topicDesribe) -> {
				Long logEndOffset = logSizeTempMap.get(patition);
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
				partitionAssignmentState.setPartition(Integer.parseInt(patition));
				partitionAssignmentState.setLag(getLag(offset, logEndOffset));

				partitionAssignmentStates.add(partitionAssignmentState);
			});
			partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
			groupConsumerState.setPartitionAssignmentStates(partitionAssignmentStates);
			groupConsumerStates.add(groupConsumerState);
		}
		return groupConsumerStates;
	}

	/**
	 * group粒度获取broker消费方式的GroupTopicConsumerState
	 *
	 * @param clusterID
	 * @param consummerGroup
	 * @return
	 */
	private List<GroupTopicConsumerState> getBrokerConsumerOffsetsByGroup(String clusterID, String consummerGroup) {
		List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
		try {
			groupConsumerStates = kafkaAdminService.getKafkaAdmins(clusterID).describeConsumerGroupsByGroup(consummerGroup);
			if (groupConsumerStates == null || groupConsumerStates.isEmpty() || groupConsumerStates.get(0).getTopic() == null) {
				return null;
			}
			// 填充lag/logEndOffset
			groupConsumerStates.forEach(topicConsumerGroupState -> {
				List<PartitionAssignmentState> partitionAssignmentStates = topicConsumerGroupState
						.getPartitionAssignmentStates();
				partitionAssignmentStates.sort(Comparator.comparingInt(PartitionAssignmentState::getPartition));
				String topic = topicConsumerGroupState.getTopic();
				Map<TopicPartition, Long> logSizeMap = kafkaConsumersService.getLogSize(clusterID, consummerGroup, topic);
				if (logSizeMap == null || logSizeMap.isEmpty()) {
					return;
				}
				for (Entry<TopicPartition, Long> entry : logSizeMap.entrySet()) {
					if (entry == null) {
						continue;
					}
					long logEndOffset = entry.getValue();
					for (PartitionAssignmentState partitionAssignmentState : partitionAssignmentStates) {
						if (partitionAssignmentState.getPartition() == entry.getKey().partition()) {
							partitionAssignmentState.setLogEndOffset(logEndOffset);
							partitionAssignmentState.setLag(getLag(partitionAssignmentState.getOffset(), logEndOffset));
						}
					}
				}
			});
		} catch (Exception e) {
			LOG.error("getBrokerConsumerOffsetsByGroup has error",e);
		}

		return groupConsumerStates;
	}

}
