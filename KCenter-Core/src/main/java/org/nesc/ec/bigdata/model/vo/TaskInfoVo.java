package org.nesc.ec.bigdata.model.vo;

import java.util.List;

public class TaskInfoVo {
	private String topicName;
	private String location;
	private Integer messageRate;
	private Integer partition;
	private Short replication;
	private Integer ttl;
	private String comments;
	private String owner;
	private String approvalOpinions;
	private String approveURL;
	
	private List<TaskClusterVo> clusterMessList;
	
	public List<TaskClusterVo> getClusterMessList() {
		return clusterMessList;
	}
	public void setClusterMessList(List<TaskClusterVo> clusterMessList) {
		this.clusterMessList = clusterMessList;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Integer getMessageRate() {
		return messageRate;
	}
	public void setMessageRate(Integer messageRate) {
		this.messageRate = messageRate;
	}
	public Integer getPartition() {
		return partition;
	}
	public void setPartition(Integer partition) {
		this.partition = partition;
	}
	public Short getReplication() {
		return replication;
	}
	public void setReplication(Short replication) {
		this.replication = replication;
	}
	public Integer getTtl() {
		return ttl;
	}
	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getApprovalOpinions() {
		return approvalOpinions;
	}
	public void setApprovalOpinions(String approvalOpinions) {
		this.approvalOpinions = approvalOpinions;
	}
	public String getApproveURL() {
		return approveURL;
	}
	public void setApproveURL(String approveURL) {
		this.approveURL = approveURL;
	}
}
