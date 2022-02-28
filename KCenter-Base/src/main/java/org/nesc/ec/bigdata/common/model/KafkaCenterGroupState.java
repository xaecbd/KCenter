package org.nesc.ec.bigdata.common.model;

/**
 * @author Truman.P.Du
 * @date 2020/08/14
 * @description KafkaCenter 定义消费组状态，区别于 ConsumerGroupState、
 * ConsumerGroupState 是kafka对group状态的定义
 * KafkaCenterGroupState是KafkaCenter对group状态的定义
 */
public enum KafkaCenterGroupState {

    ACTIVE, DEAD, UNKNOWN;


}
