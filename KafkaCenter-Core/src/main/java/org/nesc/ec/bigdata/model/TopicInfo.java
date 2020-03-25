package org.nesc.ec.bigdata.model;

import java.util.Date;
import java.util.Objects;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * Topic实体类
 * @author Reason.H.Duan
 * @date 2019-3-26
 */
@TableName("topic_info")
public class TopicInfo {
	@TableId(value="id",type=IdType.AUTO)
	private Long id;
	@TableField(value="topic_name")
	private String topicName;
	private Integer partition;
	private Short replication;
	private Long ttl;
	private String config;
	@TableField(value="owner_id")
	private Long ownerId;
	@TableField(value="team_id")
	private Long teamId;
	@TableField(value="cluster_id")
	private String clusterId;
	private String comments;
	@TableField(value="create_time")
	private Date createTime;
	@TableField(exist = false)
	private TeamInfo team;
	@TableField(exist = false)
	private ClusterInfo cluster;
	@TableField(exist = false)
	private UserInfo owner;	
	public TeamInfo getTeam() {
		return team;
	}
	public void setTeam(TeamInfo team) {
		this.team = team;
	}
	public Long getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public UserInfo getOwner() {
		return owner;
	}

	public void setOwner(UserInfo owner) {
		this.owner = owner;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public Integer getPartition() {
		return partition;
	}
	public void setPartition(Integer partition) {
		this.partition = partition;
	}
	public Short getReplication() {
		return replication;
	}
	public void setReplication(Short replication) {
		this.replication = replication;
	}
	public Long getTtl() {
		return ttl;
	}
	public void setTtl(Long ttl) {
		this.ttl = ttl;
	}
	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public Long getTeamId() {
		return teamId;
	}
	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public ClusterInfo getCluster() {
		return cluster;
	}

	public void setCluster(ClusterInfo cluster) {
		this.cluster = cluster;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TopicInfo topicInfo = (TopicInfo) o;
		return Objects.equals(id, topicInfo.id) &&
				Objects.equals(topicName, topicInfo.topicName) &&
				Objects.equals(partition, topicInfo.partition) &&
				Objects.equals(replication, topicInfo.replication) &&
				Objects.equals(ttl, topicInfo.ttl) &&
				Objects.equals(config, topicInfo.config) &&
				Objects.equals(ownerId, topicInfo.ownerId) &&
				Objects.equals(teamId, topicInfo.teamId) &&
				Objects.equals(clusterId, topicInfo.clusterId) &&
				Objects.equals(comments, topicInfo.comments) &&
				Objects.equals(createTime, topicInfo.createTime) &&
				Objects.equals(team, topicInfo.team) &&
				Objects.equals(cluster, topicInfo.cluster) &&
				Objects.equals(owner, topicInfo.owner);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, topicName, partition, replication, ttl, config, ownerId, teamId, clusterId, comments, createTime, team, cluster, owner);
	}
}
