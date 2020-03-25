package org.nesc.ec.bigdata.common.model;

import java.util.List;

import org.apache.kafka.common.ConsumerGroupState;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午4:12:17
 * @version 1.0
 */
public class TopicConsumerGroupState {
	private String groupId;
	/**
	 * 消费方式：zk/broker 主要指的offset提交到哪里 新版本 broker 旧版本zk
	 */
	private String consumerMethod;
	private List<PartitionAssignmentState> partitionAssignmentStates;
	/**
	 * Dead：组内已经没有任何成员的最终状态，组的元数据也已经被coordinator移除了。这种状态响应各种请求都是一个response：UNKNOWN_MEMBER_ID 
	 * Empty：组内无成员，但是位移信息还没有过期。这种状态只能响应JoinGroup请求
	 * PreparingRebalance：组准备开启新的rebalance，等待成员加入 
	 * AwaitingSync：正在等待leader consumer将分配方案传给各个成员 
	 * Stable：rebalance完成！可以开始消费了~
	 */
	private ConsumerGroupState consumerGroupState;
	private boolean isSimpleConsumerGroup = false ;//false：api 根据member信息判断状态,true 状态直接置黄，给提示（"SimpleConsumerGroup无法判断其状态"）低级

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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
}
