package org.nesc.ec.bigdata.service;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import org.nesc.ec.bigdata.model.TeamInfo;
import org.nesc.ec.bigdata.model.UserInfo;

import java.util.Date;
import java.util.Objects;

@TableName("ktable")
public class KTableInfo {
    @TableId(type = IdType.AUTO)
    private long id;
    @TableField(value = "cluster_id")
    private long clusterId;
    @TableField
    private String name;
    @TableField
    private String topic;
    @TableField
    private String script;
    @TableField(value = "owner_id")
    private long ownerId;
    @TableField(value = "team_id")
    private long teamId;
    @TableField(value = "create_time")
    private Date createTime = new Date();
    @TableField(exist = false)
    private UserInfo owner;
    @TableField(exist = false)
    private TeamInfo team;

    public KTableInfo(long clusterId, String name, String topic) {
        this.clusterId = clusterId;
        this.name = name;
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KTableInfo that = (KTableInfo) o;
        return clusterId == that.clusterId && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, name);
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        this.owner = owner;
    }

    public TeamInfo getTeam() {
        return team;
    }

    public void setTeam(TeamInfo team) {
        this.team = team;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
