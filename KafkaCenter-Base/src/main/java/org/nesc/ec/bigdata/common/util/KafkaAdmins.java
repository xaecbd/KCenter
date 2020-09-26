package org.nesc.ec.bigdata.common.util;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.nesc.ec.bigdata.common.model.GroupTopicConsumerState;
import org.nesc.ec.bigdata.common.model.PartitionAssignmentState;
import org.nesc.ec.bigdata.common.model.TopicConsumerGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class KafkaAdmins implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaAdmins.class);

	private int DEFAULT_TIME_OUT_SECOND = 60;

	private AdminClient adminClient;

	public KafkaAdmins(Properties adminProps) {
		this.adminClient = KafkaAdminClient.create(adminProps);
	}

	public AdminClient getAdminClient() {
		return this.adminClient;
	}

	/**
	 * 获取集群配置信息
	 */
	public DescribeClusterResult descCluster() {
		return this.adminClient.describeCluster();
	}

	/**
	 * 列出所有kakfa topic 信息 通过 ListTopicsOptions 可以设置超时间和是否为Kafka内部topic eg: 设置超时时间
	 * 设置列出所有topic(包含kafka内部topic ) ListTopicsOptions options = new
	 * ListTopicsOptions(); options.listInternal(true); options.timeoutMs(new
	 * Integer(3000));
	 * 
	 */
	public Map<String, TopicListing> listTopics(ListTopicsOptions options)
			throws InterruptedException, ExecutionException {
		ListTopicsResult listTopic = this.adminClient.listTopics(options);
		KafkaFuture<Map<String, TopicListing>> kfuture = listTopic.namesToListings();
		return kfuture.get();
	}

	/**
	 * 列出所有kakfa topic 信息 默认不会列出kafka内部的topic 超时时间默认
	 */
	public Set<String> listTopics() throws InterruptedException, ExecutionException {
		ListTopicsResult listTopic = this.adminClient.listTopics();
		return listTopic.names().get();
	}

	/**
	 * check topic 是否存在
	 */
	private Map<String, Boolean> checkExists(Set<String> topicSames, boolean containInternal)
			throws InterruptedException, ExecutionException {
		Map<String, Boolean> result = new HashMap<String, Boolean>(1 << 4);
		Set<String> kafkaTopicSet = new HashSet<String>(1 << 4);
		if (containInternal) {
			ListTopicsOptions options = new ListTopicsOptions();
			options.listInternal(true);
			kafkaTopicSet.addAll(this.listTopics(options).keySet());
		} else {
			kafkaTopicSet.addAll(this.listTopics());
		}
		for (String topicName : topicSames) {
			result.put(topicName, kafkaTopicSet.contains(topicName));
		}
		return result;
	}

	/**
	 * check topic 是否存在 默认不检查 kafka内部topic
	 */
	public boolean checkExists(String topicName) throws InterruptedException, ExecutionException {
		Set<String> set = new HashSet<String>(1 << 4);
		set.add(topicName);
		return this.checkExists(set, false).get(topicName);

	}

	/**
	 * 列出某个具体topic 详细配置信息 使用 ConfigResource.Type.TOPIC / ConfigResource.Type.BROKER
	 * 来判断资源类型
	 * 
	 */
	public Config descConfigs(ConfigResource.Type type, String resourceName)
			throws InterruptedException, ExecutionException {
		ConfigResource resource = new ConfigResource(type, resourceName);
		DescribeConfigsResult describeResult = this.adminClient.describeConfigs(Collections.singleton(resource));
		Map<ConfigResource, Config> topicConfig = describeResult.all().get();
		return topicConfig.get(resource);
	}

	/**
	 * 批量列出 topic 详细配置信息 使用 ConfigResource.Type.TOPIC / ConfigResource.Type.BROKER
	 * 来判断资源类型
	 */
	public Map<String, Config> descConfigs(Collection<String> topicNames)
			throws InterruptedException, ExecutionException {
		Map<String, Config> result = new HashMap<String, Config>(1 << 4);
		List<ConfigResource> list = new ArrayList<ConfigResource>(1 << 4);
		for (String name : topicNames) {
			list.add(new ConfigResource(ConfigResource.Type.TOPIC, name));
		}
		DescribeConfigsResult describeResult = this.adminClient.describeConfigs(list);
		Map<ConfigResource, Config> topicConfig = describeResult.all().get();
		for (ConfigResource res : list) {
			try {
				result.put(res.name(), topicConfig.get(res));
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
		return result;
	}

	/**
	 * 获取某些topics详细信息
	 */
	public Map<String, TopicDescription> descTopics(Collection<String> topicNames) {
		Map<String, TopicDescription> result = new HashMap<String, TopicDescription>(1 << 4);
		Map<String, KafkaFuture<TopicDescription>> describeFutures = this.adminClient.describeTopics(topicNames)
				.values();
		for (String topicName : topicNames) {
			try {
				result.put(topicName, describeFutures.get(topicName).get());
			} catch (Exception e) {
				LOGGER.error("desc topic:{} error.",topicName, e);
			}
		}
		return result;
	}

	/**
	 * 单独创建topic
	 * 
	 * @throws Exception
	 */
	public boolean createTopic(String topicName, int numPartitions, short replicationFactor) throws Exception {
		return this.createTopic(topicName, numPartitions, replicationFactor, null);
	}

	/**
	 * 若 topic不存在则创建 默认只检测非kafka内部的topic
	 * 
	 * @throws Exception
	 */
	public boolean createTopicIfNotExists(String topicName, int numPartitions, short replicationFactor,
			Map<String, String> topicConfig) throws Exception {
		if (this.checkExists(topicName)) {
			return this.createTopic(topicName, numPartitions, replicationFactor, topicConfig);
		}
		return true;
	}

	/**
	 * 若 topic不存在则创建 默认只检测非kafka内部的topic
	 * 
	 * @throws Exception
	 */
	public boolean createTopicIfNotExists(String topicName, int numPartitions, short replicationFactor, long ttl)
			throws Exception {
		if (!this.checkExists(topicName)) {
			Map<String, String> configs = new HashMap<>(1 << 3);
			configs.put(TopicConfig.RETENTION_MS_CONFIG, ttl + "");
			configs.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, ttl + "");
			return this.createTopic(topicName, numPartitions, replicationFactor, configs);
		}
		return true;
	}

	/**
	 * 精确创建 topic 通过 Map<String, String> configs 来指定 eg: 旧日志段的保留测率，删除或压缩，此时选择删除
	 * topicConfig.put(TopicConfig.CLEANUP_POLICY_CONFIG,TopicConfig.CLEANUP_POLICY_DELETE);
	 * 过期数据的压缩方式，如果上面选项为压缩的话才有效
	 * topicConfig.put(TopicConfig.COMPRESSION_TYPE_CONFIG,"snappy"); The amount of
	 * time to retain delete tombstone markers for log compacted topics. This
	 * setting also gives a bound on the time in which a consumer must complete a
	 * read if they begin from offset 0 to ensure that they get a valid snapshot of
	 * the final stage (otherwise delete tombstones may be collected before they
	 * complete their scan). 默认1天
	 * 
	 * topicConfig.put(TopicConfig.DELETE_RETENTION_MS_CONFIG,"86400000");
	 * 文件在文件系统上被删除前的保留时间，默认为60秒
	 * topicConfig.put(TopicConfig.FILE_DELETE_DELAY_MS_CONFIG,"60000");
	 * 将数据强制刷入日志的条数间隔
	 * topicConfig.put(TopicConfig.FLUSH_MESSAGES_INTERVAL_CONFIG,"9223372036854775807");
	 * 将数据强制刷入日志的时间间隔
	 * topicConfig.put(TopicConfig.FLUSH_MS_CONFIG,"9223372036854775807"); offset设置
	 * topicConfig.put(TopicConfig.INDEX_INTERVAL_BYTES_CONFIG,"4096"); 每个批量消息最大字节数
	 * topicConfig.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG,"1000012");
	 * 记录标记时间与kafka本机时间允许的最大间隔，超过此值的将被拒绝
	 * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_DIFFERENCE_MAX_MS_CONFIG,"9223372036854775807");
	 * 标记时间类型，是创建时间还是日志时间 CreateTime/LogAppendTime
	 * topicConfig.put(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG,"CreateTime");
	 * 如果日志压缩设置为可用的话，设置日志压缩器清理日志的频率。默认情况下，压缩比率超过50%时会避免清理日志。
	 * 此比率限制重复日志浪费的最大空间，设置为50%，意味着最多50%的日志是重复的。更高的比率设置意味着更少、更高效 的清理，但会浪费更多的磁盘空间。
	 * topicConfig.put(TopicConfig.MIN_CLEANABLE_DIRTY_RATIO_CONFIG,"0.5");
	 * 消息在日志中保持未压缩状态的最短时间，只对已压缩的日志有效
	 * topicConfig.put(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG,"0");
	 * 当一个producer的ack设置为all（或者-1）时，此项设置的意思是认为新记录写入成功时需要的最少副本写入成功数量。
	 * 如果此最小数量没有达到，则producer抛出一个异常（NotEnoughReplicas
	 * 或者NotEnoughReplicasAfterAppend）。 你可以同时使用min.insync.replicas
	 * 和ack来加强数据持久话的保障。一个典型的情况是把一个topic的副本数量设置为3,
	 * min.insync.replicas的数量设置为2,producer的ack模式设置为all，这样当没有足够的副本没有写入数据时，producer会抛出一个异常。
	 * topicConfig.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG,"1");
	 * 如果设置为true，会在新日志段创建时预分配磁盘空间
	 * topicConfig.put(TopicConfig.PREALLOCATE_CONFIG,"true");
	 * 当保留策略为删除（delete）时，此设置控制在删除就日志段来清理磁盘空间前，保存日志段的partition能增长到的最大尺寸。
	 * 默认情况下没有尺寸大小限制，只有时间限制。。由于此项指定的是partition层次的限制，它的数量乘以分区数才是topic层面保留的数量。
	 * topicConfig.put(TopicConfig.RETENTION_BYTES_CONFIG,"-1");
	 * 当保留策略为删除（delete）时，此设置用于控制删除旧日志段以清理磁盘空间前，日志保留的最长时间。默认为7天。
	 * 这是consumer在多久内必须读取数据的一个服务等级协议（SLA）。
	 * topicConfig.put(TopicConfig.RETENTION_MS_CONFIG,"604800000");
	 * 此项用于控制日志段的大小，日志的清理和持久话总是同时发生，所以大的日志段代表更少的文件数量和更小的操作粒度。
	 * topicConfig.put(TopicConfig.SEGMENT_BYTES_CONFIG,"1073741824");
	 * 此项用于控制映射数据记录offsets到文件位置的索引的大小。我们会给索引文件预先分配空间，然后在日志滚动时收缩它。 一般情况下你不需要改动这个设置。
	 * topicConfig.put(TopicConfig.SEGMENT_INDEX_BYTES_CONFIG,"10485760");
	 * 从预订的段滚动时间中减去最大的随机抖动，避免段滚动时的惊群（thundering herds）
	 * topicConfig.put(TopicConfig.SEGMENT_JITTER_MS_CONFIG,"0");
	 * 此项用户控制kafka强制日志滚动时间，在此时间后，即使段文件没有满，也会强制滚动，以保证持久化操作能删除或压缩就数据。默认7天
	 * topicConfig.put(TopicConfig.SEGMENT_MS_CONFIG,"604800000");
	 * 是否把一个不在isr中的副本被选举为leader作为最后手段，即使这样做会带来数据损失
	 * topicConfig.put(TopicConfig.UNCLEAN_LEADER_ELECTION_ENABLE_CONFIG,"false");
	 * 
	 * @throws Exception
	 */
	private boolean createTopic(String topicName, int numPartitions, short replicationFactor,
								Map<String, String> topicConfig) throws Exception {
		boolean success = false;
		NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
		newTopic.configs(topicConfig);
		CreateTopicsResult createTopicResult = this.adminClient.createTopics(Collections.singleton(newTopic));

		try {
			createTopicResult.values().get(topicName).get();
			success = true;
		} catch (Exception e) {
			throw e;
		}
		return success;
	}

	/**
	 * 指定ttl partitionNumber 与 副本数
	 */
	public boolean createTopic(String topicName, int numPartitions, short replicationFactor, long ms) {
		boolean success = false;
		NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
		Map<String, String> configs = new HashMap<>(1 << 3);
		configs.put(TopicConfig.RETENTION_MS_CONFIG, ms + "");
		configs.put(TopicConfig.DELETE_RETENTION_MS_CONFIG, ms + "");
		newTopic.configs(configs);
		CreateTopicsResult createTopicResult = this.adminClient.createTopics(Collections.singleton(newTopic));
		try {
			createTopicResult.values().get(topicName).isCompletedExceptionally();
			success = true;
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return success;
	}

	/**
	 * 批量创建topics
	 */
	public Map<String, Boolean> createtopics(Collection<NewTopic> newTopics) {
		Map<String, Boolean> result = new HashMap<String, Boolean>(1 << 4);
		CreateTopicsResult futureRes = this.adminClient.createTopics(newTopics);
		Map<String, KafkaFuture<Void>> map = futureRes.values();

		for (NewTopic topic : newTopics) {
			boolean sucess = false;
			try {
				map.get(topic.name()).get();
				sucess = true;
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("", e);
			}
			result.put(topic.name(), sucess);
		}
		return result;
	}

	/**
	 * 删除topic
	 */
	public boolean delete(String topic) {
		Map<String, Boolean> resultMap = this.delete(Collections.singleton(topic));
		return resultMap.get(topic);
	}

	/**
	 * 删除topics
	 */
	public Map<String, Boolean> delete(Collection<String> topics) {
		Map<String, Boolean> result = new HashMap<String, Boolean>(1 << 4);
		DeleteTopicsResult delRes = this.adminClient.deleteTopics(topics);
		Map<String, KafkaFuture<Void>> deleteFutures = delRes.values();
		for (String topicName : topics) {
			boolean flag = false;
			try {
				KafkaFuture<Void> kafkaFutrue = deleteFutures.get(topicName);
				kafkaFutrue.get();
				flag = true;
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			result.put(topicName, flag);
		}
		return result;
	}

	/**
	 * 修改某个topic的某个配置项
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public boolean updateTopicConfig(String topicName, ConfigEntry configEntry)
			throws InterruptedException, ExecutionException {

		boolean sucess = false;
		try {
			ConfigResource topicResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
			Map<ConfigResource, Config> updateConfig = new HashMap<ConfigResource, Config>();
			updateConfig.put(topicResource, new Config(Collections.singleton(configEntry)));
			AlterConfigsResult alterResult = this.adminClient.alterConfigs(updateConfig);
			alterResult.all().get();
			sucess = true;
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error("", e);
		}
		return sucess;
	}

	/**
	 * 修改某个topic的某些配置项
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException 配置项构造方法 eg: ConfigEntry retentionEntry = new
	 *                              ConfigEntry(TopicConfig.RETENTION_MS_CONFIG,
	 *                              "860000");
	 */
	public boolean updateTopicConfigs(String topicName, List<ConfigEntry> configList)
			throws InterruptedException, ExecutionException {
		boolean sucess = false;
		ConfigResource topicResource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);
		Map<ConfigResource, Config> updateConfig = new HashMap<ConfigResource, Config>();
		updateConfig.put(topicResource, new Config(configList));
		AlterConfigsResult alterResult = this.adminClient.alterConfigs(updateConfig);
		alterResult.all().get();
		sucess = true;
		return sucess;
	}

	/**
	 * 根据指定topic获取所有的ConsumerGroup
	 * 
	 * @param topic
	 * @return
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Set<String> listConsumerGroups(String topic)
			throws InterruptedException, ExecutionException, TimeoutException {
		final Set<String> filteredGroups = new HashSet<>();
		Set<String> allGroups = this.adminClient.listConsumerGroups().all().get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS).stream()
				.map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
		allGroups.forEach(groupId -> {
			try {
				adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get().keySet().stream()
				.filter(tp -> tp.topic().equals(topic)).forEach(tp -> filteredGroups.add(groupId));
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("" , e);
			}
		});
		return filteredGroups;
	}

	/**
	 * 获取所有的consumer(仅能获取broker消费方式)
	 * 
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public Set<String> listConsumerGroups() throws InterruptedException, ExecutionException, TimeoutException {

		Set<String> allGroups = this.adminClient.listConsumerGroups().all().get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS).stream()
				.map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
		return allGroups;
	}



	/**
	 * 根据topic 获取offset,该结果是不同group 组不同patition的当前消费offset
	 * 
	 * @param topic
	 * @return Map<String, Set<Entry<TopicPartition, OffsetAndMetadata>>>
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public Map<String, Set<Entry<TopicPartition, OffsetAndMetadata>>> listConsumerGroupOffsets(String topic)
			throws InterruptedException, ExecutionException, TimeoutException {
		Set<String> groupIds = this.listConsumerGroups(topic);
		Map<String, Set<Entry<TopicPartition, OffsetAndMetadata>>> consumerGroupOffsets = new HashMap<>();
		groupIds.forEach(groupId -> {
			Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPatitionOffsets = new HashSet<>();
			try {
				consumerPatitionOffsets = this.adminClient.listConsumerGroupOffsets(groupId)
						.partitionsToOffsetAndMetadata().get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS).entrySet().stream()
						.filter(entry -> topic.equalsIgnoreCase(entry.getKey().topic())).collect(Collectors.toSet());
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				LOGGER.error("", e);
			}
			consumerGroupOffsets.put(groupId, consumerPatitionOffsets);
		});
		return consumerGroupOffsets;
	}

	public boolean createPartitions(Map<String,NewPartitions> newPartitions) throws InterruptedException, ExecutionException{
		boolean flag = false;
		KafkaFuture<Void> result = this.adminClient.createPartitions(newPartitions).all();
		result.get();
		flag = true;	 
		return flag;
	}

	public Set<String> listTopics(String group) throws ExecutionException, InterruptedException {
		Map<TopicPartition, OffsetAndMetadata> partitionsToOffsetAndMetadataMap = adminClient
				.listConsumerGroupOffsets(group).partitionsToOffsetAndMetadata().get();
		Set<String> topic = new HashSet<>();
		partitionsToOffsetAndMetadataMap.forEach((partition,offsetAndMetadata)->{
			topic.add(partition.topic());
		});
		return topic;
	}

	/**
	 * 根据topic 获取topicConsumerGroupStates,其中未包含lag/logEndOffset(consumer可见offset)
	 * 
	 * @param topic
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public List<TopicConsumerGroupState> describeConsumerGroups(String topic)
			throws InterruptedException, ExecutionException, TimeoutException {
		final List<TopicConsumerGroupState> topicConsumerGroupStates = new ArrayList<>();
		Set<String> groupIds = new HashSet<>();
		Map<String, Set<Entry<TopicPartition, OffsetAndMetadata>>> consumerPatitionOffsetMap = new HashMap<>();
		Set<String> allGroups = this.adminClient.listConsumerGroups().all().get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS).stream()
				.map(ConsumerGroupListing::groupId).collect(Collectors.toSet());
		allGroups.forEach(groupId -> {
			try {
				Map<TopicPartition, OffsetAndMetadata> partitionsToOffsetAndMetadataMap = adminClient
						.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
				boolean isWithGroup = false;
				Set<Entry<TopicPartition, OffsetAndMetadata>> thisTopicPartitionsToOffsetAndMetadataSet = new HashSet<>();
				for (Entry<TopicPartition, OffsetAndMetadata> entry : partitionsToOffsetAndMetadataMap.entrySet()) {
					String getTopic = entry.getKey().topic();
					if (topic.equalsIgnoreCase(getTopic)) {
						isWithGroup = true;
						thisTopicPartitionsToOffsetAndMetadataSet.add(entry);
					}
				}
				// 属于该topic下的group
				if (isWithGroup) {
					groupIds.add(groupId);
					consumerPatitionOffsetMap.put(groupId, thisTopicPartitionsToOffsetAndMetadataSet);
				}

			} catch (InterruptedException | ExecutionException e) {
				LOGGER.error("", e);
			}
		});

		Map<String, ConsumerGroupDescription> groupDetails = this.adminClient.describeConsumerGroups(groupIds).all()
				.get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS);

		groupDetails.entrySet().forEach(entry -> {
			String groupId = entry.getKey();
			ConsumerGroupDescription description = entry.getValue();

			TopicConsumerGroupState topicConsumerGroupState = new TopicConsumerGroupState(groupId,"broker");

			topicConsumerGroupState.setConsumerGroupState(description.state());
			topicConsumerGroupState.setSimpleConsumerGroup(description.isSimpleConsumerGroup());
			//判断group下是否有members
			topicConsumerGroupState.setHasMembers(description.members().size()>0);
			// 获取group下不同patition消费offset信息
			Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPatitionOffsets = consumerPatitionOffsetMap
					.get(groupId);
			List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();

			if (!description.members().isEmpty()) {
				// 获取存在consumer(memeber存在的情况)
				partitionAssignmentStates = this.withMembers(consumerPatitionOffsets, groupId, description);
			} else {
				// 获取不存在consumer
				partitionAssignmentStates = this.withNoMembers(consumerPatitionOffsets, groupId);
			}

			// 这块增加这个的逻辑是因为可能存在member，但是这个memeber不属于这个topic，因此会存在partitionAssignmentStates为空的状况
			if (partitionAssignmentStates.isEmpty()) {
				partitionAssignmentStates = this.withNoMembers(consumerPatitionOffsets, groupId);
			}
			topicConsumerGroupState.setPartitionAssignmentStates(partitionAssignmentStates);
			topicConsumerGroupStates.add(topicConsumerGroupState);
		});

		return topicConsumerGroupStates;
	}

	/**
	 * 根据group获取broker消费方式下 GroupTopicConsumerState信息，不包含lag/logEndOffset
	 * 
	 * @param consumerGroup
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public List<GroupTopicConsumerState> describeConsumerGroupsByGroup(String consumerGroup)
			throws InterruptedException, ExecutionException, TimeoutException {
		final List<GroupTopicConsumerState> groupConsumerStates = new ArrayList<>();
		Map<TopicPartition, OffsetAndMetadata> partitionsToOffsetAndMetadataMap = adminClient
				.listConsumerGroupOffsets(consumerGroup).partitionsToOffsetAndMetadata().get();

		Map<String, ConsumerGroupDescription> groupDetails = this.adminClient
				.describeConsumerGroups(Arrays.asList(consumerGroup)).all().get(DEFAULT_TIME_OUT_SECOND, TimeUnit.SECONDS);

		Map<String, Map<TopicPartition, OffsetAndMetadata>> resMap = new HashMap<>();
		for (Entry<TopicPartition, OffsetAndMetadata> entry : partitionsToOffsetAndMetadataMap.entrySet()) {
			String topic = entry.getKey().topic();
			Map<TopicPartition, OffsetAndMetadata> dataMap = new HashMap<>();
			if (resMap.containsKey(topic)) {
				dataMap = resMap.get(topic);
			}
			dataMap.put(entry.getKey(), entry.getValue());
			resMap.put(topic, dataMap);
		}

		ConsumerGroupDescription description = null;
		if (groupDetails.containsKey(consumerGroup)) {
			description = groupDetails.get(consumerGroup);
			for (Entry<String, Map<TopicPartition, OffsetAndMetadata>> oneTopic : resMap.entrySet()) {
				GroupTopicConsumerState groupConsumerState = new GroupTopicConsumerState(consumerGroup,oneTopic.getKey(),"broker");

				groupConsumerState.setConsumerGroupState(description.state());
				groupConsumerState.setSimpleConsumerGroup(description.isSimpleConsumerGroup());
				Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPatitionOffsets = oneTopic.getValue().entrySet();
				List<PartitionAssignmentState> partitionAssignmentStates;

				if (!description.members().isEmpty()) {
					// 获取存在consumer(memeber存在的情况)
					partitionAssignmentStates = this.withMembers(consumerPatitionOffsets, consumerGroup, description);
				} else {
					// 获取不存在consumer
					partitionAssignmentStates = this.withNoMembers(consumerPatitionOffsets, consumerGroup);
				}
				// 这块增加这个的逻辑是因为可能存在member，但是这个memeber不属于这个topic，因此会存在partitionAssignmentStates为空的状况
				if (partitionAssignmentStates.isEmpty()) {
					partitionAssignmentStates = this.withNoMembers(consumerPatitionOffsets, consumerGroup);
				}
				groupConsumerState.setPartitionAssignmentStates(partitionAssignmentStates);
				groupConsumerStates.add(groupConsumerState);
			}
		} else {
			for (Entry<String, Map<TopicPartition, OffsetAndMetadata>> oneTopic : resMap.entrySet()) {
				GroupTopicConsumerState groupConsumerState = new GroupTopicConsumerState(consumerGroup,oneTopic.getKey(),"broker");
				Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPartitionOffsets = oneTopic.getValue().entrySet();
				List<PartitionAssignmentState> partitionAssignmentStates = this.withNoMembers(consumerPartitionOffsets,
						consumerGroup);
				groupConsumerState.setPartitionAssignmentStates(partitionAssignmentStates);
				groupConsumerStates.add(groupConsumerState);
			}
		}
		return groupConsumerStates;
	}

	/**
	 * 
	 * @param consumerPatitionOffsets
	 * @param groupId
	 * @param description
	 * @return
	 */
	private List<PartitionAssignmentState> withMembers(
			Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPatitionOffsets, String groupId,
			ConsumerGroupDescription description) {
		Set<PartitionAssignmentState> partitionAssignmentStates = new HashSet<>();
		Map<Integer, Long> consumerPatitionOffsetMap = new HashMap<>();
		Set<String> topics = new HashSet<>();
		consumerPatitionOffsets.forEach(entryInfo -> {
			TopicPartition topicPartition = entryInfo.getKey();
			OffsetAndMetadata offsetAndMetadata = entryInfo.getValue();
			consumerPatitionOffsetMap.put(topicPartition.partition(), offsetAndMetadata.offset());
			topics.add(topicPartition.topic());
		});
		description.members().forEach(memberDescription -> {
			memberDescription.assignment().topicPartitions().forEach(topicPation -> {
				String topic = topicPation.topic();
				if (topics.contains(topic)) {
					PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
					partitionAssignmentState.setPartition(topicPation.partition());
					partitionAssignmentState.setTopic(topic);
					partitionAssignmentState.setClientId(Optional.ofNullable(memberDescription.clientId()).orElse("-"));
					partitionAssignmentState.setGroup(groupId);
					partitionAssignmentState
					.setConsumerId(Optional.ofNullable(memberDescription.consumerId()).orElse("-"));
					partitionAssignmentState.setHost(Optional.ofNullable(memberDescription.host()).orElse("-"));
					partitionAssignmentState.setOffset(
							Optional.ofNullable(consumerPatitionOffsetMap.get(topicPation.partition())).orElse(-1L));
					partitionAssignmentStates.add(partitionAssignmentState);
				}
			});
		});
		return new ArrayList<>(partitionAssignmentStates);
	}

	/**
	 * 
	 * @param consumerPatitionOffsets
	 * @param groupId
	 * @return
	 */
	private List<PartitionAssignmentState> withNoMembers(
			Set<Entry<TopicPartition, OffsetAndMetadata>> consumerPatitionOffsets, String groupId) {
		List<PartitionAssignmentState> partitionAssignmentStates = new ArrayList<>();
		consumerPatitionOffsets.forEach(entryInfo -> {
			TopicPartition topicPartition = entryInfo.getKey();
			String topic = topicPartition.topic();
			OffsetAndMetadata offsetAndMetadata = entryInfo.getValue();
			PartitionAssignmentState partitionAssignmentState = new PartitionAssignmentState();
			partitionAssignmentState.setPartition(topicPartition.partition());
			partitionAssignmentState.setTopic(topic);
			partitionAssignmentState.setGroup(groupId);
			partitionAssignmentState.setOffset(Optional.ofNullable(offsetAndMetadata.offset()).orElse(-1L));
			partitionAssignmentStates.add(partitionAssignmentState);
		});
		return partitionAssignmentStates;
	}

	@Override
	public void close() throws IOException {
		this.adminClient.close();
	}

	/**
	 * delete group
	 * @param consumerGroup group name
	 */
	public void deleteGroup(String consumerGroup) {
		this.deleteGroups(Collections.singletonList(consumerGroup));
	}

	/**
	 * delete groups
	 * @param consumerGroups group name
	 */
	private void deleteGroups(List<String> consumerGroups) {
		try {
			DeleteConsumerGroupsResult result = this.adminClient.deleteConsumerGroups(consumerGroups);
			KafkaFuture future = result.all();
			future.get();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public Map<String,Long> getTopicDiskSizeForBroker(List<Integer> brokerIds) throws ExecutionException, InterruptedException {
		Map<String,Long>  sizeMap = new HashMap<>();
		DescribeLogDirsResult describeLogDirsResult = this.adminClient.describeLogDirs(brokerIds);
		Map<Integer,Map<String, DescribeLogDirsResponse.LogDirInfo>> tmp = describeLogDirsResult.all().get();
		tmp.forEach((k,map)->{
			map.forEach((k2,v)->{
				DescribeLogDirsResponse.LogDirInfo info = v;
				Map<TopicPartition, DescribeLogDirsResponse.ReplicaInfo> replicaInfoMap = info.replicaInfos;
				replicaInfoMap.forEach((topic,replicaInfo)->{
					long size = sizeMap.getOrDefault(topic.topic(),0L);
					size += replicaInfo.size;
					sizeMap.put(topic.topic(),size);
				});
			});
		});
		return sizeMap;
	}

	public Map<String,Long> getTopicDiskSizeForBroker(int brokerId) throws ExecutionException, InterruptedException {
		return getTopicDiskSizeForBroker(Collections.singletonList(brokerId));
	}

}
