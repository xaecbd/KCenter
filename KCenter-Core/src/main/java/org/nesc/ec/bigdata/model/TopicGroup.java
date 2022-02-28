package org.nesc.ec.bigdata.model;

import org.nesc.ec.bigdata.constant.Constants;

/**
 * @author Truman.P.Du
 * @date 2020/08/01
 * @description Topic与Group相关信息
 */
public class TopicGroup {
    private String clusterId;
    private String topic;
    private String group;
    private String consumerType;

    public TopicGroup() {
    }

    public TopicGroup(String clusterId, String topic, String group, String consumerType) {
        this.clusterId = clusterId;
        this.topic = topic;
        this.group = group;
        this.consumerType = consumerType;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String generateKey() {
        return clusterId + Constants.Symbol.VERTICAL_STR + topic + Constants.Symbol.VERTICAL_STR + group + Constants.Symbol.VERTICAL_STR + consumerType;
    }
}
