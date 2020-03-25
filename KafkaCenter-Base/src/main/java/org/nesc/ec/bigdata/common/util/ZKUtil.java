package org.nesc.ec.bigdata.common.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.common.model.TopicInfo;

import kafka.utils.ZKStringSerializer;

/**
 * @author Truman.P.Du
 * @date 2019年4月12日 上午10:19:35
 * @version 1.0
 */
public class ZKUtil implements Closeable {

	private ZkClient zkClient;
	private int SESSIONT_IMEOUT = 1000 * 60;
	private int CONNECTION_TIMEOUT = 1000 * 60;
	private static final java.lang.String brokerPath = "/brokers/topics/";
	private static final String TopicConfigChangesPath = "/config/changes";
	private static final String TopicConfigChangeZnodePrefix = "config_change_";
	private String rootPath = "";
	public ZKUtil(String zkServers) {
		String lastChar = zkServers.substring(zkServers.length()-1, zkServers.length());
		// 如果最后一个字符是'/',去掉
		if("/".equals(lastChar)) {
			zkServers = zkServers.substring(0,zkServers.length()-1);
		}
		// 获取指定path
		int index = zkServers.indexOf("/");
		if(index != -1) {
			// 存在rootPath
			String newzkServers = zkServers.substring(0,index);
			this.rootPath = zkServers.substring(index);
			zkClient = new ZkClient(newzkServers, SESSIONT_IMEOUT, CONNECTION_TIMEOUT, new ZkStringSerialize());
		} else {
			zkClient = new ZkClient(zkServers, SESSIONT_IMEOUT, CONNECTION_TIMEOUT, new ZkStringSerialize());
		}
	}

	public ZKUtil(String zkServers, int sessionTimeout, int connectionTimeout) {
		String lastChar = zkServers.substring(zkServers.length()-1, zkServers.length());
		if("/".equals(lastChar)) {
			zkServers = zkServers.substring(0,zkServers.length()-1);
		}
		int index = zkServers.indexOf("/");
		if(index != -1) {
			// 存在rootPath
			String newzkServers = zkServers.substring(0,index);
			this.rootPath = zkServers.substring(index);
			zkClient = new ZkClient(newzkServers, sessionTimeout, connectionTimeout, new ZkStringSerialize());
		} else {
			zkClient = new ZkClient(zkServers, sessionTimeout, connectionTimeout, new ZkStringSerialize());
		}
	}

	/**
	 * 根据group与topic获取相应的patition offset提交情况
	 * 
	 * @param groupId
	 * @param topic
	 * @return
	 */
	public Map<String, Map<String, String>> getZKConsumerOffsets(String groupId, String topic) {
		Map<String, Map<String, String>> result = new HashMap<>();
		String offsetsPath = rootPath + "/consumers/" + groupId + "/offsets/" + topic;
		if (zkClient.exists(offsetsPath)) {
			List<String> offsets = zkClient.getChildren(offsetsPath);
			offsets.forEach(patition -> {
				try {
					String offset = zkClient.readData(offsetsPath + "/" + patition, true);
					if (offset != null) {
						Map<String, String> map = new HashMap<>();
						map.put("offset", offset);
						result.put(patition, map);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			});
		}

		String ownersPath = rootPath + "/consumers/" + groupId + "/owners/" + topic;
		if (zkClient.exists(ownersPath)) {
			List<String> owners = zkClient.getChildren(ownersPath);
			owners.forEach(patition -> {
				try {
					try {
						String owner = zkClient.readData(ownersPath + "/" + patition, true);
						if (owner != null) {
							Map<String, String> map = result.get(patition);
							map.put("owner", owner);
							result.put(patition, map);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			});
		}

		return result;
	}
	/**
	 * 获取zookeeper 上消费offset信息
	 * Map<group, Map<topic, Map<patition, offset>>>
	 * @return
	 */
	public Map<String, Map<String, Map<String, String>>> getZKConsumerOffsets() {
		Map<String, Map<String, Map<String, String>>> result = new HashMap<>();
		List<String> allGroups = zkClient.getChildren(rootPath + "/consumers");
		allGroups.forEach(group -> {
			if (zkClient.exists(rootPath + "/consumers/" + group + "/offsets")) {
				Map<String,Map<String,String>> topicOffsets = new HashMap<>();

				Set<String> topics = new HashSet<>(zkClient.getChildren(rootPath + "/consumers/" + group + "/offsets"));
				topics.forEach(topic->{
					List<String> patitions = zkClient.getChildren(rootPath + "/consumers/" + group + "/offsets/"+topic);
					Map<String,String> offsets = new HashMap<>();
					Map<String,String> results = getState(topic,group);
					patitions.forEach(patition->{
						String path = rootPath + "/consumers/" + group + "/offsets/"+topic+"/"+patition;
						String offset = zkClient.readData(path, true);
						String state = results.get(patition);
						offsets.put(patition, offset+"|"+state);
					});
					topicOffsets.put(topic, offsets);
				});
				result.put(group, topicOffsets);
			}
		});
		return result;
	}

	public Map<String,String> getState(String topic,String groupId){
		Map<String,String> result  = new HashMap<>();
		String ownersPath = rootPath + "/consumers/" + groupId + "/owners/" + topic;
		if (zkClient.exists(ownersPath)) {
			List<String> owners = zkClient.getChildren(ownersPath);
			for (String partition:owners){
				try {
					try {
						String owner = zkClient.readData(ownersPath + "/" + partition, true);
						if (owner != null) {
							result.put(partition,owner);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return result;
	}

	/**
	 * 根据topic 获取消费组
	 * 
	 * @param topic
	 * @return
	 */
	public Set<String> listTopicGroups(String topic) {
		Set<String> groups = new HashSet<>();
		try {
			List<String> allGroups = zkClient.getChildren(rootPath + "/consumers");
			allGroups.forEach(group -> {
				if (zkClient.exists(rootPath + "/consumers/" + group + "/offsets")) {
					Set<String> offsets = new HashSet<>(zkClient.getChildren(rootPath + "/consumers/" + group + "/offsets"));
					if (offsets.contains(topic)) {
						groups.add(group);
					}
				}
			});
		} catch (Exception e) {
		}

		return groups;
	}

	public  ZkDataAndStat readDataMaybeNull(String path) {
		Stat stat = new Stat();
		String data = null;
		try {
			data = zkClient.readData(path, stat);
		} catch (Exception e) {
			//			LOG.warn("Path: " + path + " do not exits in ZK!" + e.getMessage());
		}
		return new ZkDataAndStat(data, stat);
	}

	public  List<BrokerInfo> getBrokers() throws Exception {
		List<BrokerInfo> kafkaHosts = new ArrayList<BrokerInfo>();
		try {
			List<String> ids = zkClient.getChildren(rootPath + "/brokers/ids");
			for (String id : ids) {
				String brokerInfo = new String(readDataMaybeNull(rootPath + "/brokers/ids/" + id).getData());
				BrokerInfo bi = new BrokerInfo();
				bi.setBid(Integer.parseInt(id));
				JSONObject jsonObj = JSONObject.parseObject(brokerInfo);
				if (jsonObj.containsKey("host")) {
					bi.setHost(jsonObj.get("host").toString());
				}
				if (jsonObj.containsKey("port")) {
					bi.setPort(Integer.parseInt(jsonObj.get("port").toString()));
				}
				if (jsonObj.containsKey("jmx_port")) {
					bi.setJmxPort(Integer.parseInt(jsonObj.get("jmx_port").toString()));
				}
				if (jsonObj.containsKey("version")) {
					bi.setVersion(Integer.parseInt(jsonObj.get("version").toString()));
				}
				if (jsonObj.containsKey("timestamp")) {
					bi.setTimestamp(Long.parseLong(jsonObj.get("timestamp").toString()));
				}
				kafkaHosts.add(bi);
			} 
		}catch (Exception e) {
		}
		return kafkaHosts;
	}

	public JSONObject descConfig(String topicName) {
		String configInfo = new String(readDataMaybeNull(rootPath + "/config/topics/"+topicName).getData());
		JSONObject configObj = JSONObject.parseObject(configInfo);
		return configObj.getJSONObject("config");
	}	

	public boolean updateConfig(JSONObject entry,String topicName) {
		boolean flag = false;
		try {
			String configInfo = new String(readDataMaybeNull(rootPath + "/config/topics/"+topicName).getData());
			JSONObject obj = JSONObject.parseObject(configInfo);
			JSONObject configObj = obj.getJSONObject("config");	
			configObj.clear();
			Set<String> keys = entry.keySet();
			keys.forEach(key->{
				if(!"".equalsIgnoreCase(entry.getString(key).trim())) {
					configObj.put(key.replaceAll("\\_", "."), entry.getString(key).trim());
				}				
			});
			zkClient.writeData(rootPath + "/config/topics/"+topicName,obj.toJSONString());
			flag = createNofity(topicName);
		} catch (Exception e) {
			flag = false;
		}
		return flag;

	}

	public boolean createNofity(String topicName) {
		boolean flag = false;
		try {
			if(!zkClient.exists(TopicConfigChangesPath)) {
				zkClient.createPersistent(TopicConfigChangesPath);
			}
			Map<String,Object> map = new LinkedHashMap<>();
			map.put("version", 1);
			map.put("entity_type","topics");
			map.put("entity_name", topicName);
		    zkClient.create(rootPath+TopicConfigChangesPath+"/"+TopicConfigChangeZnodePrefix, 
					JSONObject.toJSONString(map), CreateMode.PERSISTENT_SEQUENTIAL);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
		
	}
	public boolean  deletePath(String topicName) {
		zkClient.deleteRecursive(rootPath+"/brokers/topics/"+topicName);
		return zkClient.deleteRecursive(rootPath + "/config/topics/"+topicName);
	}
	

	public boolean pathExist(String topicName) {
		return zkClient.exists(rootPath + "/config/topics/"+topicName);
	}

	public String getTopicPath(String topic) {
		return brokerPath+topic;
	}

	public String getPartitionPath(String topic) {
		return brokerPath+topic+"/partitions";
	}

	public Map<Integer, TopicInfo> readPartition(String topicName) {
		String partition = new String(readDataMaybeNull(rootPath +"/brokers/topics/"+topicName).getData());
		JSONObject partitionObj = JSONObject.parseObject(partition).getJSONObject("partitions");
		List<String> partitions = zkClient.getChildren(rootPath + "/brokers/topics/"+topicName+"/partitions");
		Map<Integer,TopicInfo> topics = new HashMap<>();
		if(!partitions.isEmpty()) {
			for(String state:partitions) {
				String replicateData = new String(readDataMaybeNull(rootPath + "/brokers/topics/"+topicName+"/partitions/"+state+"/state").getData());
				JSONObject replicateObj = JSONObject.parseObject(replicateData);
				TopicInfo topicInfo = new TopicInfo();
				topicInfo.setTopicName(topicName);			
				String replicate = partitionObj.getString(state);
				String isr =  replicateObj.getString("isr");
				int leader = replicateObj.getIntValue("leader");
				String[] replicateArr = replicate.substring(1, replicate.length()-1).split(",");
				String[] isrArr = isr.substring(1, replicate.length()-1).split(",");
				topicInfo.setIsr(isrArr);
				topicInfo.setLeader(leader);
				topicInfo.setReplicate(replicateArr);
				topicInfo.setPrefreLeader(Integer.parseInt(replicateArr[0])==leader?true:false);
				topicInfo.setUnderReplicate(isrArr.length==replicateArr.length?true:false);
				topics.put(Integer.parseInt(state), topicInfo);
			}
		}

		return topics;
	}
	public boolean createPartitionPath(String topicName,int currentPartition,Map<Integer,List<Integer>> data) {
		boolean flag = false;
		try {
			String topicPartition = new String(readDataMaybeNull(rootPath + getTopicPath(topicName)).getData());

			JSONObject partitionObj = JSONObject.parseObject(topicPartition);
			JSONObject configObj = partitionObj.getJSONObject("partitions");
			Map<String,Object> map = new LinkedHashMap<>();
			map.put("version", partitionObj.get("version"));
			data.forEach((k,v)->{
				configObj.put(String.valueOf(k), v);
			});
			map.put("partitions", configObj);
			zkClient.writeData(rootPath + getTopicPath(topicName),JSONObject.toJSONString(map));
			flag = true;
		} catch (Exception e) {
			flag = false;
		}
		return flag;



	}

	@Override
	public void close() throws IOException {
		if (zkClient != null) {
			zkClient.close();
		}
	}

	class ZkStringSerialize implements ZkSerializer {
		@Override
		public byte[] serialize(Object paramObject) throws ZkMarshallingError {
			return ZKStringSerializer.serialize(paramObject);
		}

		@Override
		public Object deserialize(byte[] paramArrayOfByte) throws ZkMarshallingError {
			return ZKStringSerializer.deserialize(paramArrayOfByte);
		}
	}

	class ZkDataAndStat {
		private String data;
		private Stat stat;
		public String getData() {
			return data;
		}
		public void setData(String data) {
			this.data = data;
		}
		public Stat getStat() {
			return stat;
		}
		public void setStat(Stat stat) {
			this.stat = stat;
		}
		public ZkDataAndStat(String data, Stat stat) {
			super();
			this.data = data;
			this.stat = stat;
		}
	}

	/**
	 * 获取所有消费组
	 * @param path
	 * @return
	 */
	public Set<String> listConsumerGroups() {
		List<String> allGroups = new ArrayList<>();
		try {
			allGroups = zkClient.getChildren(rootPath + "/consumers");
		} catch (Exception e) {
			return new HashSet<>(allGroups);
		}
		return new HashSet<>(allGroups);
	}

	/**
	 * 根据group 获取topic
	 * @param group
	 * @return
	 */
	public Set<String> listTopicsByGroup(String group) {
		Set<String> topics = new HashSet<>();
		try {
			topics = new HashSet<>(zkClient.getChildren(rootPath + "/consumers/" + group + "/offsets"));
		} catch (Exception e) {
		}
		return topics;
	}

	/**
	 * 判断 group 能否删除
	 * @param consumerGroup group name
	 * @return true: group can del, false: can't del
	 */
	private boolean groupCanDelete(String consumerGroup) {
		Set<String> topics = listTopicsByGroup(consumerGroup);
		String ownersPath;
		for (String topic : topics) {
			ownersPath = rootPath + "/consumers/" + consumerGroup + "/owners/" + topic;
			if (zkClient.exists(ownersPath)) {
				List<String> partitions = zkClient.getChildren(ownersPath);
				for (String partition : partitions) {
					String owner = zkClient.readData(ownersPath + "/" + partition, true);
					if (null != owner) {
						return false;
					}
				}
			}
		}
		return true;
	}


	/**
	 * 删除group
	 * @param consumerGroup group
	 */
	public void deleteGroup(String consumerGroup) {
		if (groupCanDelete(consumerGroup)) {
			zkClient.deleteRecursive(rootPath + "/consumers/" + consumerGroup);
		} else {
			throw new IllegalStateException("group can't delete!");
		}
	}

}
