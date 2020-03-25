package org.nesc.ec.bigdata.common.model;

public class TopicInfo {
  private String topicName;
  private int partition;
  private String[] replicate;
  private String[] isr;
  private int leader;
  private boolean prefreLeader;
  private boolean underReplicate;
public String getTopicName() {
	return topicName;
}
public void setTopicName(String topicName) {
	this.topicName = topicName;
}
public int getPartition() {
	return partition;
}
public void setPartition(int partition) {
	this.partition = partition;
}
public String[] getReplicate() {
	return replicate;
}
public void setReplicate(String[] replicate) {
	this.replicate = replicate;
}
public String[] getIsr() {
	return isr;
}
public void setIsr(String[] isr) {
	this.isr = isr;
}
public int getLeader() {
	return leader;
}
public void setLeader(int leader) {
	this.leader = leader;
}
public boolean isPrefreLeader() {
	return prefreLeader;
}
public void setPrefreLeader(boolean prefreLeader) {
	this.prefreLeader = prefreLeader;
}
public boolean isUnderReplicate() {
	return underReplicate;
}
public void setUnderReplicate(boolean underReplicate) {
	this.underReplicate = underReplicate;
}
  
}
