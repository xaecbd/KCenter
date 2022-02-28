package org.nesc.ec.bigdata.model;

import org.nesc.ec.bigdata.common.model.BrokerInfo;

/**
 * @author：Truman.P.Du
 * @createDate: 7/12/2019
 * @version:1.0
 * @description:
 */
public class KafkaManagerBroker {
    private BrokerInfo brokerInfo;
    private long topics;
    private long partitions;
    private long partitionsAsLeader;
    private String messages;
    private Double bytesIn;
    private Double bytesOut;
    private String clusterName;

    private boolean isController;

    public boolean isController() {
        return isController;
    }

    public void setController(boolean controller) {
        isController = controller;
    }

    public BrokerInfo getBrokerInfo() {
        return brokerInfo;
    }

    public void setBrokerInfo(BrokerInfo brokerInfo) {
        this.brokerInfo = brokerInfo;
    }

    public long getTopics() {
        return topics;
    }

    public void setTopics(long topics) {
        this.topics = topics;
    }

    public long getPartitions() {
        return partitions;
    }

    public void setPartitions(long partitions) {
        this.partitions = partitions;
    }

    public long getPartitionsAsLeader() {
        return partitionsAsLeader;
    }

    public void setPartitionsAsLeader(long partitionsAsLeader) {
        this.partitionsAsLeader = partitionsAsLeader;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public Double getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(Double bytesIn) {
        this.bytesIn = bytesIn;
    }

    public Double getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(Double bytesOut) {
        this.bytesOut = bytesOut;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
