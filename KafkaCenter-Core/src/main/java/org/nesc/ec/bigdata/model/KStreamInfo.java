package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.util.Date;
import java.util.Objects;

@TableName("kstream")
public class KStreamInfo {

    @TableId(value = "id",type = IdType.AUTO)
    private long id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "team_id")
    private long teamId;
    @TableField(value = "cluster_id")
    private long clusterId;
    @TableField(value = "owner_id")
    private long ownerId;
    @TableField(value = "stream_type")
    private int streamType;
    @TableField(value = "config")
    private String config;
    @TableField(value = "script")
    private String script;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "topic")
    private String topic;
    @TableField(exist = false)
    private TeamInfo team;
    @TableField(exist = false)
    private UserInfo owner;


    public KStreamInfo(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KStreamInfo that = (KStreamInfo) o;
        return clusterId == that.clusterId && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, clusterId);
    }

    public KStreamInfo(String name, long clusterId, String topic) {
        this.name = name;
        this.clusterId = clusterId;
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "KStreamInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", teamId=" + teamId +
                ", clusterId=" + clusterId +
                ", ownerId=" + ownerId +
                ", streamType=" + streamType +
                ", config='" + config + '\'' +
                ", script='" + script + '\'' +
                ", createTime=" + createTime +
                ", topic='" + topic + '\'' +
                ", team=" + team +
                ", owner=" + owner +
                '}';
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public TeamInfo getTeam() {
        return team;
    }

    public void setTeam(TeamInfo team) {
        this.team = team;
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        this.owner = owner;
    }
}
