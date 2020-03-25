package org.nesc.ec.bigdata.cache;

/**
 * @author lg99
 */
public class HomeCache {
	private static HomePageCache configCache = new HomePageCache();

	public static HomePageCache getConfigCache() {
		return configCache;
	}

	public  static class HomePageCache{
		private int clusterSize;
		private int topicSize;
		private int groupSize;
		private int alertSize;

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
	}
}
