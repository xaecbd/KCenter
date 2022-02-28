package org.nesc.ec.bigdata.model;

import org.apache.kafka.common.record.TimestampType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonInclude(Include.NON_NULL)
public class KafkaRecord {
	private String topic;

	private Integer partition;

	private Long timestamp;

	private TimestampType timestampType;

	private Long offset;

	private String key;

	private String value;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Integer getPartition() {
		return partition;
	}

	public void setPartition(Integer partition) {
		this.partition = partition;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public TimestampType getTimestampType() {
		return timestampType;
	}

	public void setTimestampType(TimestampType timestampType) {
		this.timestampType = timestampType;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
