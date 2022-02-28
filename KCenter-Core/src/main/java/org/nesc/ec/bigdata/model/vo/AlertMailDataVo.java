package org.nesc.ec.bigdata.model.vo;

import java.util.List;

import org.nesc.ec.bigdata.common.model.OffsetInfo;

public class AlertMailDataVo {
	private Long clusterId;
	private String topicName;
	private String group;
	private Long lagCount;
	private String publicURL;
	private List<OffsetInfo> offsets;
	public Long getClusterId() {
		return clusterId;
	}
	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public Long getLagCount() {
		return lagCount;
	}
	public void setLagCount(Long lagCount) {
		this.lagCount = lagCount;
	}
	public String getPublicURL() {
		return publicURL;
	}
	public void setPublicURL(String publicURL) {
		this.publicURL = publicURL;
	}
	public List<OffsetInfo> getOffsets() {
		return offsets;
	}
	public void setOffsets(List<OffsetInfo> offsets) {
		this.offsets = offsets;
	}
}
 