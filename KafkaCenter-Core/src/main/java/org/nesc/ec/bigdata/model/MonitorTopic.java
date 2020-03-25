package org.nesc.ec.bigdata.model;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午2:14:43
 * @version 1.0
 */
public class MonitorTopic {
	private String topicName;
	private String clusterName;
	private Long clusterID;
	private Long topicId;
	private boolean isCollections;

	
	public Long getTopicId() {
		return topicId;
	}

	public void setTopicId(Long topicId) {
		this.topicId = topicId;
	}

	public boolean isCollections() {
		return isCollections;
	}

	public void setCollections(boolean isCollections) {
		this.isCollections = isCollections;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Long getClusterID() {
		return clusterID;
	}

	public void setClusterID(Long clusterID) {
		this.clusterID = clusterID;
	}

}
