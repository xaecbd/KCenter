package org.nesc.ec.bigdata.common.model;

public class OffsetStat {
    private Long timestamp;
    private String group;
    private String topic;
    private Long offset;
    private Long logSize;
    private Long lag;
    private Long clusterId;

    public OffsetStat() {
        super();
    }

    public OffsetStat(Long timestamp, String group, String topic, Long offset, Long logSize, Long lag,Long clusterId) {
        super();
        this.timestamp = timestamp;
        this.group = group;
        this.topic = topic;
        this.offset = offset;
        this.logSize = logSize;
        this.lag = lag;
        this.clusterId = clusterId;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return this.topic;
    }

    public Long getOffset() {
        return this.offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public void setLag(Long lag) {
        this.lag = lag;
    }

    public Long getLag() {
        return this.lag;
    }

    public Long getLogSize() {
        return logSize;
    }

    public void setLogSize(Long logSize) {
        this.logSize = logSize;
    }

	public Long getClusterId() {
		return clusterId;
	}

	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}
    
}