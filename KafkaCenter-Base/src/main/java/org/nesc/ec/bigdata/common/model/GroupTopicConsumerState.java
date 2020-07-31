package org.nesc.ec.bigdata.common.model;

import java.util.List;

import org.apache.kafka.common.ConsumerGroupState;

/**
 * 
 * @author jc1e
 *
 * 2019年5月14日
 */
public class GroupTopicConsumerState {
	private String topic;
	private String consumerMethod;
	private List<PartitionAssignmentState> partitionAssignmentStates;
	
	private ConsumerGroupState consumerGroupState;
	private boolean isSimpleConsumerGroup = false ;

	private boolean hasMembers;

	public boolean isHasMembers() {
		return hasMembers;
	}

	public void setHasMembers(boolean hasMembers) {
		this.hasMembers = hasMembers;
	}

	public String getConsumerMethod() {
		return consumerMethod;
	}

	public void setConsumerMethod(String consumerMethod) {
		this.consumerMethod = consumerMethod;
	}

	public List<PartitionAssignmentState> getPartitionAssignmentStates() {
		return partitionAssignmentStates;
	}

	public void setPartitionAssignmentStates(List<PartitionAssignmentState> partitionAssignmentStates) {
		this.partitionAssignmentStates = partitionAssignmentStates;
	}

	public ConsumerGroupState getConsumerGroupState() {
		return consumerGroupState;
	}

	public void setConsumerGroupState(ConsumerGroupState consumerGroupState) {
		this.consumerGroupState = consumerGroupState;
	}

	public boolean isSimpleConsumerGroup() {
		return isSimpleConsumerGroup;
	}

	public void setSimpleConsumerGroup(boolean isSimpleConsumerGroup) {
		this.isSimpleConsumerGroup = isSimpleConsumerGroup;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
