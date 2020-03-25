package org.nesc.ec.bigdata.common.model;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午4:11:05
 * @version 1.0
 */
public class PartitionAssignmentState {
	private String group; // groupId
	private String topic;
	private int partition;
	private long offset;
	private long lag;
	private String consumerId;
	private String host;
	private String clientId;
	private long logEndOffset;

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

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public long getLogEndOffset() {
		return logEndOffset;
	}

	public void setLogEndOffset(long logEndOffset) {
		this.logEndOffset = logEndOffset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + partition;
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		PartitionAssignmentState other = (PartitionAssignmentState) obj;
		if (group == null) {
			if (other.group != null) {
                return false;
            }
		} else if (!group.equals(other.group)) {
            return false;
        }
		if (partition != other.partition) {
            return false;
        }
		if (topic == null) {
			return other.topic == null;
		} else {
			return topic.equals(other.topic);
		}
	}
}
