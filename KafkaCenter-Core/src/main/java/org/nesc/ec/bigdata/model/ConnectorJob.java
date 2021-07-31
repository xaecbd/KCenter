package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.util.Date;
import java.util.Objects;

@TableName("connect_job")
public class ConnectorJob {
    @TableId(type = IdType.AUTO)
    private long id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "type")
    private String type;
    @TableField(value = "class_name")
    private String className;
    @TableField(value = "cluster_id")
    private int clusterId;
    @TableField(value = "state")
    private String state;
    @TableField(value = "script")
    private String script;
    @TableField(value = "owner_id")
    private long ownerId;
    @TableField(value = "team_id")
    private long teamId;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;
    @TableField(value = "is_history")
    private boolean isHistory;
    @TableField(exist = false)
    private TeamInfo team;
    @TableField(exist = false)
    private UserInfo owner;

    public ConnectorJob(String name, String type, String className, int clusterId, String state) {
        this.name = name;
        this.type = type;
        this.className = className;
        this.clusterId = clusterId;
        this.state = state;
    }

    public ConnectorJob(String name, String type, String className, int clusterId, String state, String script) {
        this.name = name;
        this.type = type;
        this.className = className;
        this.clusterId = clusterId;
        this.state = state;
        this.script = script;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectorJob that = (ConnectorJob) o;
        return clusterId == that.clusterId && name.equals(that.name) && type.equals(that.type) && className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, className, clusterId);
    }

    @Override
    public String toString() {
        return "ConnectorJob{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", className='" + className + '\'' +
                ", clusterId=" + clusterId +
                ", state='" + state + '\'' +
                ", script='" + script + '\'' +
                ", ownerId=" + ownerId +
                ", teamId=" + teamId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isHistory=" + isHistory +
                ", team=" + team +
                ", owner=" + owner +
                '}';
    }

    public ConnectorJob() {
    }


    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
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

    public boolean isHistory() {
        return isHistory;
    }

    public void setHistory(boolean history) {
        isHistory = history;
    }
}
