package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.nesc.ec.bigdata.common.util.JmxCollector;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.KafkaManagerBroker;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaManagerService {
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaManagerService.class);
	@Autowired
	KafkaAdminService kafkaAdminService;

	@Autowired
	ClusterService clusterService;

	@Autowired
	TopicInfoService topicInfoService;
	@Autowired
	ZKService zkService;

	@Autowired
	KafkaConsumersService kafkaConsumersService;

	public JSONArray topicConfig(String clusterId,String topicName) {
		JSONArray array = new JSONArray();
		try {
			org.apache.kafka.clients.admin.Config config = kafkaAdminService.getKafkaAdmins(clusterId)
					 .descConfigs(Collections.singletonList(topicName)).get(topicName);
			config.entries().forEach(entry->{
				JSONObject json = new JSONObject();

				if(!Constants.Symbol.EMPTY_STR.equals(entry.value()) && !entry.isDefault()) {
					json.put(Constants.JsonObject.CONFIG, entry.name());
					json.put(Constants.JsonObject.VALUE, entry.value());
					array.add(json);
				}				
			});		
		} catch (InterruptedException | ExecutionException e){
			JSONObject obj = zkService.getZK(clusterId).descConfig(topicName);
			Set<String> keys= obj.keySet();
			keys.forEach(key->{
				JSONObject json = new JSONObject();
				json.put(Constants.JsonObject.CONFIG, key);
				json.put(Constants.JsonObject.VALUE, obj.get(key));
				array.add(json);
			});
		}
		return array;
	}

	public Map<String,Object> topicAndPartition(String clusterId,String topicName) {
		Map<String,Object> result = new HashMap<>();
		KafkaAdmins admins = kafkaAdminService.getKafkaAdmins(clusterId);
		try {
			if(!admins.checkExists(topicName)) {
				return result;
			}			
			Set<String> topicNames = new HashSet<>();
			topicNames.add(topicName);
			TopicDescription topicDesc = admins.descTopics(topicNames).get(topicName);
			Map<Integer,Long> endOffSet = new HashMap<>();
			Map<String,Object> partitionInfo = partitionsInfo(topicDesc, endOffSet,clusterId);	
			Object partitions = partitionInfo.get(TopicConfig.PARTITION);
			partitionInfo.remove(TopicConfig.PARTITION);
			Map<String,Object> partitionBroker = partitionsByBroker(topicDesc, clusterId);
			Object partitionByBroker = partitionBroker.get(BrokerConfig.BROKER);
			partitionBroker.remove(BrokerConfig.BROKER);
			Map<String,Object> summary = recombineMap(partitionInfo, partitionBroker);
			result.put(TopicConfig.PARTITION,partitions==null?new JSONArray():partitions);
			result.put(BrokerConfig.BROKER,partitionByBroker==null?new JSONArray():partitionByBroker);
			result.put(Constants.JsonObject.SUMMARY, ConvertMapToArr(summary));

		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("get topic and partition info has error",e);
		} 
		return result;
	}


	public JSONArray ConvertMapToArr(Map<String,Object> summary) {
		JSONArray array = new JSONArray();
		summary.forEach((k,v)->{
			JSONObject obj = new JSONObject();
			obj.put(Constants.JsonObject.NAME, k);
			obj.put(Constants.JsonObject.VALUE, v);
			array.add(obj);
		});
		return array;
	}
	public Map<String,Object> recombineMap(Map<String,Object> map1,Map<String,Object> map2){
		map2.forEach(map1::put);
		return map1;

	}
	public Map<String,Object> partitionsInfo(TopicDescription topicDesc,Map<Integer,Long> endOffSet,String clusterId) {
		JSONArray array = new JSONArray();
		Map<String,Object> map = new HashMap<>();
		try {
			int underReCount = 0;
			int preferredLeader = 0;
			topicDesc.partitions().sort(Comparator.comparingInt(TopicPartitionInfo::partition));
			Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> parfromApi = this.descPartition(topicDesc);
			for(Integer key:parfromApi.keySet()) {
				org.nesc.ec.bigdata.common.model.TopicInfo topicInfo = parfromApi.get(key);
				if(!topicInfo.isPrefreLeader()) {
					preferredLeader += 1;
				}
				if(!topicInfo.isUnderReplicate()) {
					underReCount +=1;		    		
				}
				JSONObject 	obj = new JSONObject();
				obj.put(TopicConfig.PARTITION, key);
				obj.put(TopicConfig.IN_SYNC_REPLICAS, displayData(topicInfo.getIsr()));
				obj.put(TopicConfig.REPLICAS, displayData(topicInfo.getReplicate()));
				obj.put(TopicConfig.LEADER, topicInfo.getLeader());
				obj.put(TopicConfig.UNDEREPLICATED,String.valueOf(topicInfo.isUnderReplicate()));
				obj.put(TopicConfig.LATEST_OFFSET, endOffSet.isEmpty()?0:endOffSet.get(key));
				obj.put(TopicConfig.PREFERRED_LEADER,String.valueOf(topicInfo.isPrefreLeader()));
				array.add(obj);
			}
			map.put(TopicConfig.UNDER_REPLICATION, 100*underReCount/topicDesc.partitions().size());
			map.put(TopicConfig.PREFERRED_REPLICAS, 100*preferredLeader/topicDesc.partitions().size());
			map.put(TopicConfig.NUMBER_OF_PARTITIONS, array.size());
			long summaryOffset = 0;
			for(Long offset:endOffSet.values()) {
				summaryOffset += offset;
			}
			map.put(TopicConfig.SUM_OF_PARTITION_OFFSETS,summaryOffset);
			map.put(TopicConfig.PARTITION, array);

		} catch (Exception e) {
			LOGGER.error("get partitions infor has error",e);
			return map;
		}
		return map;
	}

	public String[] displayData(String[] str) {
		String[] result = new String[str.length];
		for (int i = 0; i < result.length; i++) {
			if(i==result.length-1) {
				result[i] = str[i];
			}else {
				result[i] = str[i]+",";
			}
		}
		return result;
	}
	public Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> compare(Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> zkMap, Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> partitionMap){
		Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> result = new HashMap<>();
		zkMap.forEach((k,topic)->{
			org.nesc.ec.bigdata.common.model.TopicInfo apiTopic = partitionMap.get(k);
			org.nesc.ec.bigdata.common.model.TopicInfo zkTopic = topic;
			if(apiTopic==null) {
				result.put(k, zkTopic);
			}else {
				boolean flag = compareArray(zkTopic.getReplicate(), apiTopic.getReplicate());
				boolean isrFlag = compareArray(zkTopic.getIsr(),apiTopic.getIsr());
				if(flag || isrFlag) {
					result.put(k, zkTopic);
				}else {
					result.put(k, apiTopic);
				}
			}
			
		});
		return result;
	}
	public boolean compareArray(String[] zkArr,String[] partitionArr) {
		boolean flag = true;
		for (int i = 0; i < partitionArr.length; i++) {
			if(Integer.parseInt(partitionArr[i]) != Integer.parseInt(zkArr[i])) {
				flag = false;
			}
		}
		return flag;
	}
	public Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> descPartition(TopicDescription topicDesc){
		Map<Integer, org.nesc.ec.bigdata.common.model.TopicInfo> map = new HashMap<>();
		for (TopicPartitionInfo partitions : topicDesc.partitions()) {
			int leader = partitions.leader().id();
			List<String> replicaList = new LinkedList<>();
			partitions.replicas().forEach(repla->{
				replicaList.add(repla.idString());
			});
			List<String> isrList = new LinkedList<>();
			partitions.isr().forEach(isrNode->{
				isrList.add(isrNode.idString());
			});
			org.nesc.ec.bigdata.common.model.TopicInfo topicInfo = new org.nesc.ec.bigdata.common.model.TopicInfo();
			topicInfo.setTopicName(topicDesc.name());
			topicInfo.setLeader(leader);
			topicInfo.setPartition(partitions.partition());
			topicInfo.setIsr(converListToArr(isrList));
			topicInfo.setReplicate(converListToArr(replicaList));
			topicInfo.setPrefreLeader(Integer.parseInt(replicaList.get(0)) == leader);
			topicInfo.setUnderReplicate(isrList.size() == replicaList.size());
			map.put(partitions.partition(), topicInfo);
		}
		return map;
	}

	public String[] converListToArr(List<String> list) {
		String[] str = new String[list.size()];
		for(int i =0;i<list.size();i++) {
			str[i] = list.get(i);
		}
		return str;
	}
	public boolean deleteTopic(String clusterId,String topicName) {
		boolean delete = false;
		KafkaAdmins admins = kafkaAdminService.getKafkaAdmins(clusterId);
		try {	
			boolean isclusterDelete = admins.delete(topicName);
			if(isclusterDelete) {
				boolean dataBaseDelete = topicInfoService.isDelete(topicName,clusterId);
				if(dataBaseDelete){
					delete = true;
				}
			}
		} catch (Exception e) {
			LOGGER.error("delete topic has error",e);
		}
		return delete;
	}

	public Map<Integer,Long> getEndOffset(String clusterId,String topicName) {
		Map<Integer,Long> partitionMap = new HashMap<>();
		String group= BrokerConfig.GROUP_NAME;
		Map<TopicPartition, Long> logSizeMap =  kafkaConsumersService.getLogSize(clusterId, group, topicName);
		for (TopicPartition partitions : logSizeMap.keySet()) {
			if(partitionMap.containsKey(partitions.partition())) {
				Long endOffset = partitionMap.get(partitions.partition());
				Long newEndOffset = logSizeMap.get(partitions);
				Long logSize = endOffset>newEndOffset?endOffset:newEndOffset;
				partitionMap.put(partitions.partition(), logSize);
			}else {
				partitionMap.put(partitions.partition(), logSizeMap.get(partitions));
			}
		}
		return partitionMap;
	}

	public boolean addPartition(String clusterId,String topicName,int partitions,String broker,int oldPartition) {
		boolean flag = false;
		Set<String> topicNames = new HashSet<>();
		topicNames.add(topicName);
		TopicDescription topicDescription = kafkaAdminService.getKafkaAdmins(clusterId).descTopics(topicNames).get(topicName);
		List<String> brokerList = new ArrayList<>(Arrays.asList(broker.split(Constants.Symbol.COMMA)));
		Map<Integer,List<Integer>> partitiosMap = this.assignReplicasToBrokers(brokerList, partitions-topicDescription.partitions().size(),
				topicDescription.partitions().get(0).replicas().size(), oldPartition);
		List<List<Integer>> newAssignments = new ArrayList<>();
		partitiosMap.forEach((k,v)->{
			newAssignments.add(v);
		});
		NewPartitions newPartitions = NewPartitions.increaseTo(partitions, newAssignments);
		Map<String,NewPartitions> map = new HashMap<>();
		map.put(topicName, newPartitions);
		try {
			kafkaAdminService.getKafkaAdmins(clusterId).createPartitions(map);
			flag = true;
		} catch (InterruptedException | ExecutionException e) {
			flag = zkService.getZK(clusterId).createPartitionPath(topicName, oldPartition, partitiosMap);
		}
		return flag;
	}

	public  Map<Integer,List<Integer>> assignReplicasToBrokers(List<String> brokerList,int partitionsToAdd,int replicator,int startPartitionId) {
		Map<Integer,List<Integer>> map = new HashMap<>();
		int currentPartitionId = Math.max(startPartitionId, 0);
		
		int startIndex = new Random().nextInt(brokerList.size());
		int nextReplicaShift = new Random().nextInt(brokerList.size());
		for(int i=0;i<partitionsToAdd;i++) {
			if (currentPartitionId > 0 && (currentPartitionId % brokerList.size() == 0)) {
				nextReplicaShift += 1;
			}
			int firstReplicaIndex = (currentPartitionId + startIndex) % (brokerList.size());
			List<Integer> replicaList = new ArrayList<>();
			replicaList.add(Integer.valueOf(brokerList.get(firstReplicaIndex)));
			for(int j =0;j<replicator-1;j++) {
				int replicaIndex = replicaIndex(firstReplicaIndex, nextReplicaShift, j,brokerList.size());
				replicaList.add(Integer.valueOf(brokerList.get(replicaIndex)));
			}
			map.put(currentPartitionId, replicaList);
			currentPartitionId  = currentPartitionId +1;
		}
		return map;
	}
	
	private  int replicaIndex(int firstReplicaIndex, int secondReplicaShift, int replicaIndex, int nBrokers) {
		int shift = 1 + (secondReplicaShift + replicaIndex) % (nBrokers - 1);
		shift = (firstReplicaIndex + shift) % nBrokers;
		return shift;
	}


	public JSONObject descConfig(String clusterId,String topicName) {
		JSONObject obj = new JSONObject();
		try {
			org.apache.kafka.clients.admin.Config config = kafkaAdminService.getKafkaAdmins(clusterId)
					 .descConfigs(Collections.singletonList(topicName)).get(topicName);
			config.entries().stream().filter(entrys->!entrys.isDefault()).forEach(entry->{
				if(!"".equals(entry.value())) {
					obj.put(entry.name().replaceAll("\\.","\\_"), entry.value());
				}				
			});		
		} catch (InterruptedException | ExecutionException e) {
			JSONObject zkObj =  zkService.getZK(clusterId).descConfig(topicName);
			Set<String> keys = zkObj.keySet();
			keys.forEach(key-> obj.put(key.replaceAll("\\.","\\_"), zkObj.get(key)));
		}	
		return obj;
	}

	public List<Integer> getPartitionByTopic(String clusterId,String topicName){
		List<Integer> list = new ArrayList<Integer>();
		KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);
		Map<String, TopicDescription> map = kafkaAdmins.descTopics(Collections.singleton(topicName));
		map.forEach((k,v)->{
			for(TopicPartitionInfo partition:v.partitions()) {
				list.add(partition.partition());
			}
		});
		list.sort(Comparator.comparingInt(id -> (Integer) id));
		return list;
	}

	public JSONArray topicList(String clusterId) {
		Map<String, TopicInfo> topicMap = new HashMap<>();
		List<TopicInfo> topics = null;
		List<ClusterInfo> clusterInfos = null;
		if(!clusterId.equalsIgnoreCase("-1")){
			topics = topicInfoService.selectAllByClusterId(clusterId) ;
			clusterInfos = new ArrayList<>();
			clusterInfos.add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));
		}else{
			topics = topicInfoService.getTotalData() ;
			clusterInfos = clusterService.getTotalData();

		}
		topics.forEach(topic->{
			String key = this.generatorKey(topic.getCluster().getId().toString(), topic.getTopicName());
			topicMap.put(key, topic);
		});
		JSONArray array = new JSONArray();


		clusterInfos.forEach(cluster->{
			try {
				KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(cluster.getId().toString());
				Set<String> topicNames = kafkaAdmins.listTopics();
				Map<String, TopicDescription> map = kafkaAdmins.descTopics(topicNames);
				map.forEach((k,v)->{
					int isrCount = 0;
					int partitionCount = 0;
					for(TopicPartitionInfo partition:v.partitions()) {
						partitionCount = partitionCount+partition.replicas().size();
						isrCount = isrCount+partition.isr().size();
					}
					String key = this.generatorKey(cluster.getId().toString(),k);
					JSONObject obj = new JSONObject();
					if(topicMap.containsKey(key)) {		
						TopicInfo topicEntry = topicMap.get(key);
						obj.put(TopicConfig.TTL, topicEntry.getTtl());
					}
					obj.put(TopicConfig.PARTITION, v.partitions().size());
					obj.put(TopicConfig.REPLICATION, v.partitions().get(0).replicas().size());
					obj.put(Constants.KeyStr.CLUSTER, cluster.getName());
					obj.put(BrokerConfig.TOPIC_NAME, k);
					obj.put(Constants.KeyStr.clusterId, cluster.getId());
					obj.put(TopicConfig.UNDER_REPLICATION, (1-(isrCount/partitionCount))*100);
					array.add(obj);
				});
			} catch (Exception e) {
				LOGGER.error("get topic info by clusterId has error",e);
			}

		});
		return array;
	}

	public boolean updateConfig(String topicName,String clusterId,JSONObject entry) {
		boolean flag = false;
		try {
			flag = this.updateConfigByClient(entry, topicName, clusterId);
		} catch (InterruptedException | ExecutionException e) {
			flag = zkService.getZK(clusterId).updateConfig(entry, topicName);
		}
		return flag;
	}
	
	public boolean updateConfigByClient(JSONObject obj,String topic,String clusterId) throws InterruptedException, ExecutionException {
		List<ConfigEntry> entrys = new ArrayList<>();
		Set<String> keys = obj.keySet();
		keys.forEach(key->{
			if(!"".equalsIgnoreCase(obj.getString(key).trim())) {
				ConfigEntry entry = new ConfigEntry(key.replaceAll("\\_", "\\."), obj.getString(key).trim());
				entrys.add(entry);
			}			
		});
		return kafkaAdminService.getKafkaAdmins(clusterId).updateTopicConfigs(topic, entrys);
		
	}

	public Map<String,Object>  partitionsByBroker(TopicDescription topicDesc,String clusterId) {
		JSONArray array = new JSONArray();
		Map<String,Object> result = new HashMap<>();
		Map<Integer,String> map = new HashMap<>();
		Map<Integer,Integer> leaderMap = new HashMap<>();
		try {
			List<BrokerInfo> brokers = zkService.getZK(clusterId).getBrokers();
			List<Integer> brokerList = new ArrayList<>();
			brokers.forEach(broker-> brokerList.add(broker.getBid()));
			int partiSize = topicDesc.partitions().size();
			int replicSize = topicDesc.partitions().get(0).replicas().size();
			topicDesc.partitions().forEach(partition->{
				partition.replicas().forEach(replicas->{
					if(brokerList.contains(replicas.id())) {
						String partitions = "";
						if(map.containsKey(replicas.id())) {
							partitions = map.get(replicas.id())+Constants.Symbol.COMMA+partition.partition();
						}else {
							partitions = partition.partition()+ Constants.Symbol.EMPTY_STR;
						}
						map.put(replicas.id(), partitions);
					}
				});
				if(brokerList.contains(partition.leader().id())) {
					int leader = 0;
					if(leaderMap.containsKey(partition.leader().id())) {
						leader = leaderMap.get(partition.leader().id())+1;
					}else {
						leader = 1;
					}
					leaderMap.put(partition.leader().id(),leader);
				}

			});
			int brokerSkewCount = 0;
			int leaderSkewCount = 0;
			for (Integer key : map.keySet()) {
				JSONObject obj = new JSONObject();
				obj.put(BrokerConfig.BROKER, key);
				obj.put(TopicConfig.PARTITIONS, map.get(key).split(Constants.Symbol.COMMA).length);
				obj.put(TopicConfig.LEADER_COUNT, leaderMap.get(key));
				obj.put(TopicConfig.PARTITION, map.get(key));
				double skewed = Math.ceil((double)partiSize*replicSize/(double)brokerList.size());
				boolean flagSkew = false;
				boolean skew = false;
				if(skewed < map.get(key).split(Constants.Symbol.COMMA).length) {
					flagSkew = true;
					brokerSkewCount += 1;
				}
				if(leaderMap.containsKey(key)&& skewed < leaderMap.get(key)) {
					skew = true;
					leaderSkewCount += 1;
				}
				obj.put(TopicConfig.SKEWED,String.valueOf(flagSkew));
				obj.put(TopicConfig.LEADERSKEWED, String.valueOf(skew));
				array.add(obj);
			}
			result.put(TopicConfig.REPLICATION, replicSize);
			
			result.put(TopicConfig.SUM_OF_PARTITION_OFFSETS, 0);
			result.put(TopicConfig.TOTAL_NUMBER_OF_BROKERS, brokers.size());
			result.put(TopicConfig.NUMBER_OF_BROKERS_FOR_TOPIC, map.keySet().size());
			result.put(TopicConfig.BROKERS_SKEWED, (100*brokerSkewCount/brokers.size()));
			result.put(TopicConfig.BROKERS_LEADER_SKEWED, 100*leaderSkewCount/brokers.size());
			result.put(TopicConfig.BROKERS_SPREAD, 100*map.keySet().size()/brokers.size());
			result.put(BrokerConfig.BROKER, array);
		} catch (Exception e) {
			LOGGER.error("get partitions info by clusterId has error",e);
		}
		return result;

	}

	private String generatorKey(String clusterId,String topicName) {
		return clusterId+"|"+topicName;
	}

	public JSONArray brokersList(String clusterId) {
		JSONArray array = new JSONArray();
		try {
			List<BrokerInfo> brokerList = zkService.getZK(clusterId).getBrokers();
			brokerList.forEach(broker->{
				JSONObject obj = new JSONObject();
				obj.put(Constants.JsonObject.VALUE,broker.getBid());
				obj.put(Constants.JsonObject.LABEL,broker.getBid()+"-"+broker.getHost());
				array.add(obj);
			});
		} catch (Exception e) {
			LOGGER.error("get broker list has error",e);
		}
		return array;

	}

	public void deleteGroup(String clusterId, String consumerGroup, String consumerApi) {
		if (Constants.KeyStr.BROKER.equals(consumerApi)) {
			KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);
			if (null != kafkaAdmins) {
				kafkaAdmins.deleteGroup(consumerGroup);
			} else {
				throw new IllegalArgumentException("no this cluster, please check cluster!");
			}
		} else if (Constants.KeyStr.ZK.equals(consumerApi)) {
			zkService.deleteGroup(clusterId, consumerGroup);
		} else {
			throw new IllegalArgumentException("consumerAPI is neither BROKER nor ZK!");
		}
	}

	public  List<KafkaManagerBroker> getAllClusterBrokes(String clusterId){
		List<KafkaManagerBroker> kafkaManagerBrokers = new ArrayList<>();
		List<ClusterInfo> clusterInfoList = null;
		if(!clusterId.equalsIgnoreCase("-1")){
			clusterInfoList = new ArrayList<>();
			clusterInfoList.add(clusterService.selectById(Long.parseUnsignedLong(clusterId)));
		}else{
			clusterInfoList = clusterService.getTotalData();
		}
		clusterInfoList.forEach((ClusterInfo cluster) ->{
			try {
				String clusterID = cluster.getId().toString();
				List<BrokerInfo> brokers = zkService.getZK(clusterID).getBrokers();
				brokers.sort(Comparator.comparingInt(BrokerInfo::getBid));
				Map<String,Set<MeterMetric>> metricBroker = JmxCollector.getInstance().metricEveryBroker(brokers);
				Map<Integer,KafkaManagerBroker> brokerMap = new HashMap<>(brokers.size());
				brokers.forEach(broker->{
					KafkaManagerBroker kafkaManagerBroker = new KafkaManagerBroker();
					kafkaManagerBroker.setBrokerInfo(broker);
					kafkaManagerBroker.setClusterName(cluster.getName());
					Set<MeterMetric>  metricSet = metricBroker.isEmpty()?new HashSet<>():metricBroker.get(broker.getHost());
					try{
						metricSet.forEach(meterMetric -> {
							switch (meterMetric.getMetricName()){
								case BrokerConfig.BYTES_IN_PER_SEC:{
									kafkaManagerBroker.setBytesIn(meterMetric.getOneMinuteRate());
								}break;
								case BrokerConfig.BYTES_OUT_PER_SEC:{
									kafkaManagerBroker.setBytesOut(meterMetric.getOneMinuteRate());
								}break;
								case BrokerConfig.MESSAGES_IN_PER_SEC:{
									kafkaManagerBroker.setMessages(meterMetric.getOneMinuteRate()+"");
								}break;
								default:break;
							}
						});
					}catch (Exception e){
						LOGGER.error("kafkaManagerBroker set metric error.",e);
					}
					brokerMap.put(broker.getBid(),kafkaManagerBroker);
				});

				KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterID);
				ListTopicsOptions options = new ListTopicsOptions();
				options.listInternal(true);
				Set<String> topicNames= kafkaAdmins.listTopics(options).keySet();
				Map<String, TopicDescription> map = kafkaAdmins.descTopics(topicNames);
				for (Map.Entry<String, TopicDescription> entry:map.entrySet()){
					Set<String>topicBrokers = new HashSet<>();
					TopicDescription topicDescription = entry.getValue();
					topicDescription.partitions().forEach(topicPartitionInfo -> {
						int leaderId = topicPartitionInfo.leader().id();
						KafkaManagerBroker kafkaManagerBroker= brokerMap.get(leaderId);
						kafkaManagerBroker.setPartitionsAsLeader(kafkaManagerBroker.getPartitionsAsLeader()+1);
						List<Node> nodes = topicPartitionInfo.replicas();
						nodes.forEach(node -> {
							topicBrokers.add(node.idString());
							KafkaManagerBroker tempKafkaManagerBroker= brokerMap.get(node.id());
							tempKafkaManagerBroker.setPartitions(tempKafkaManagerBroker.getPartitions()+1);
						});
					});
					topicBrokers.forEach(broker->{
						KafkaManagerBroker kafkaManagerBroker= brokerMap.get(Integer.parseInt(broker));
						kafkaManagerBroker.setTopics(kafkaManagerBroker.getTopics()+1);
					});
				}


				brokerMap.forEach((k,v)-> kafkaManagerBrokers.add(v));


			} catch (Exception e) {
				LOGGER.error("listTopics or descTopics has error.",e);
			}

		});
		return kafkaManagerBrokers;
	}
	
	public boolean resetOffset(String clusterId, String group, String topic) {
		return kafkaConsumersService.commitOffsetLatest(clusterId,group,topic);
	}

	public Set<String> getTopicByGroup(String clusterId,String group){
		try {
			return kafkaAdminService.getKafkaAdmins(clusterId).listTopics(group);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return new HashSet<>();
	}
}
