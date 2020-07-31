package org.nesc.ec.bigdata.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lg99
 */
public class HomeCache {
	private static HomePageCache configCache = new HomePageCache();

	//map第一层key为clusterId
	//map第二次key为:topic|group|consumerMethod
	public static Map<String, ConsumerLagCache> consumerLagCacheMap = new ConcurrentHashMap<>();
	public static HomePageCache getConfigCache() {
		return configCache;
	}

	//存放consumer所需要的信息
	public static class  ConsumerLagCache{
		private String group;
		private String topic;
		private String status;
		private long lag;
		private long offset;
		private String method;
		public ConsumerLagCache(String group, String topic, String status, long lag, long offset,String method) {
			this.group = group;
			this.topic = topic;
			this.status = status;
			this.lag = lag;
			this.offset = offset;
			this.method = method;
		}

		public ConsumerLagCache(String group, String topic, String status, long lag, String method) {
			this.group = group;
			this.topic = topic;
			this.status = status;
			this.lag = lag;
			this.method = method;
		}

		public long getOffset() {
			return offset;
		}

		public void setOffset(long offset) {
			this.offset = offset;
		}

		public String getGroup() {
			return group;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public long getLag() {
			return lag;
		}

		public void setLag(long lag) {
			this.lag = lag;
		}

		public String getMethod() {
			return method;
		}

		public void setMethod(String method) {
			this.method = method;
		}
	}


	public  static class HomePageCache{
		private int clusterSize;
		private int topicSize;
		private int groupSize;
		private int alertSize;
		private int brokerSize;

		public int getClusterSize() {
			return clusterSize;
		}

		public int getTopicSize() {
			return topicSize;
		}

		public void setTopicSize(int topicSize) {
			this.topicSize = topicSize;
		}

		public int getGroupSize() {
			return groupSize;
		}

		public void setGroupSize(int groupSize) {
			this.groupSize = groupSize;
		}

		public int getAlertSize() {
			return alertSize;
		}

		public void setAlertSize(int alertSize) {
			this.alertSize = alertSize;
		}

		public void setClusterSize(int clusterSize) {
			this.clusterSize = clusterSize;
		}

		public int getBrokerSize() {
			return brokerSize;
		}

		public void setBrokerSize(int brokerSize) {
			this.brokerSize = brokerSize;
		}
	}
}
