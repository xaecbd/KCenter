package org.nesc.ec.bigdata.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lg99
 */
public class HomeCache {
	/** config map for HomePage,to storage the cluster,topic,alert or group information in HomePage*/
	private static HomePageCache configCache = new HomePageCache();

	//map第二次key为:clusterId|topic|group|consumerMethod
	public static Map<String, ConsumerLagCache> consumerLagCacheMap = new ConcurrentHashMap<>();

	public static HomePageCache getConfigCache() {
		return configCache;
	}

	/**Store the information the consumer needs*/
	public static class  ConsumerLagCache{
		// consumer group name
		private String group;
		// topic name
		private String topic;
		// consumer group status
		private String status;
		// consumer lag
		private long lag;
		// consumer offset
		private long offset;
		// consumer method
		private String method;
		// checked the consumer lag time
		private long currentTime;
		// cluster name
		private String clusterName;
		// cluster id
		private String clusterId;

		@Override
		public String toString() {
			return "ConsumerLagCache{" +
					"group='" + group + '\'' +
					", topic='" + topic + '\'' +
					", status='" + status + '\'' +
					", lag=" + lag +
					", offset=" + offset +
					", method='" + method + '\'' +
					", currentTime=" + currentTime +
					", clusterName='" + clusterName + '\'' +
					", clusterId='" + clusterId + '\'' +
					'}';
		}

		public ConsumerLagCache(String group, String topic, String status, long lag, long offset, String method) {
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

		public ConsumerLagCache(String group, String topic, String status, long lag, long offset, String method, long currentTime, String clusterName, String clusterId) {
			this.group = group;
			this.topic = topic;
			this.status = status;
			this.lag = lag;
			this.offset = offset;
			this.method = method;
			this.currentTime = currentTime;
			this.clusterName = clusterName;
			this.clusterId = clusterId;
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

		public long getCurrentTime() {
			return currentTime;
		}

		public void setCurrentTime(long currentTime) {
			this.currentTime = currentTime;
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

    /**Store the information required for a homepage,such as cluster,topic,group,alert,broker information */
	public  static class HomePageCache{
		//cluster size
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
