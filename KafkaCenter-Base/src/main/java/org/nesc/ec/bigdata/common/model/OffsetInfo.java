package org.nesc.ec.bigdata.common.model;

/**
 * @author Truman.P.Du
 * @date 2019年4月19日 上午9:25:47
 * @version 1.0
 */
public class OffsetInfo {
	private String group;
	private String topic;
	private int partition;
	private long offset;
	private long lag;
	private long logEndOffset;
	/**
	 * 消费方式：zk/broker 主要指的offset提交到哪里 新版本 broker 旧版本zk
	 */
	private String consumerMethod;

	public OffsetInfo() {
	}

	public OffsetInfo(String group, String topic, int partition, long lag, String consumerMethod) {
		this.group = group;
		this.topic = topic;
		this.partition = partition;
		this.lag = lag;
		this.consumerMethod = consumerMethod;
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

	public int getPartition() {
		return partition;
	}

	public void setPartition(int partition) {
		this.partition = partition;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public long getLogEndOffset() {
		return logEndOffset;
	}

	public void setLogEndOffset(long logEndOffset) {
		this.logEndOffset = logEndOffset;
	}

	public String getConsumerMethod() {
		return consumerMethod;
	}

	public void setConsumerMethod(String consumerMethod) {
		this.consumerMethod = consumerMethod;
	}

}
