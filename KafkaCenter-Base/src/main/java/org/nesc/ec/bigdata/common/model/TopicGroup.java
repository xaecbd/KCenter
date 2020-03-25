package org.nesc.ec.bigdata.common.model;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午3:11:06
 * @version 1.0
 */
public class TopicGroup {
	private String name;
	private ConsumerTypeEnum consumerTypeEnum;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConsumerTypeEnum getConsumerTypeEnum() {
		return consumerTypeEnum;
	}

	public void setConsumerTypeEnum(ConsumerTypeEnum consumerTypeEnum) {
		this.consumerTypeEnum = consumerTypeEnum;
	}

}
