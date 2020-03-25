package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.util.Date;

@TableName("cluster_info")
public class ClusterInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String name;
    @TableField(value = "zk_address")
    private String zkAddress;
    private String broker;
    @TableField(value = "create_time")
    private Date createTime;

    private boolean enable;
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @TableField(value = "broker_size")
    private int brokerSize;

    @TableField(value = "kafka_version")
    private String kafkaVersion;
    @TableField(value = "graf_addr")
    private String grafAddr;


    private String comments;

    public String getGrafAddr() {
        return grafAddr;
    }

    public void setGrafAddr(String grafAddr) {
        this.grafAddr = grafAddr;
    }

    public String getKafkaVersion() {
        return kafkaVersion;
    }

    public void setKafkaVersion(String kafkaVersion) {
        this.kafkaVersion = kafkaVersion;
    }

    public int getBrokerSize() {
        return brokerSize;
    }

    public void setBrokerSize(int brokerSize) {
        this.brokerSize = brokerSize;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZkAddress() {
        return zkAddress;
    }

    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

}
