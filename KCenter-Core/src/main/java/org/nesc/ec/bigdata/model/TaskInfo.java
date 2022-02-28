package org.nesc.ec.bigdata.model;

import java.util.Date;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * @author Lola
 */
@TableName("task_info")
public class TaskInfo {
	@TableId(value="id",type=IdType.AUTO)
	private Long id;
	@TableField(value="cluster_ids")
	private String clusterIds;
	
	private String location;
	
	@TableField(value="message_rate")
	private Integer messageRate;
	
	@TableField(value="topic_name")
	private String topicName;
	private Integer partition;
	private Short replication;
	private Integer ttl;
	@TableField(value="owner_id")
	private Long ownerId;
	@TableField(value="team_id")
	private Long teamId;
	private String comments;
	@TableField(value="create_time")
	private Date createTime;
	@TableField(value="approved_id")
	private Long approvedId;
	@TableField(value="approved_time")
	private Date approvedTime;

	@TableField(exist = false)
	private UserInfo owner;
	@TableField(exist = false)
	private UserInfo approve;
	@TableField(exist = false)
    private TeamInfo team;
	@TableField(exist = false)
	private String clusterNames;

	@TableField(value="approval_opinions")
	private String approvalOpinions;

	public UserInfo getApprove() {
		return approve;
	}

	public void setApprove(UserInfo approve) {
		this.approve = approve;
	}

	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Integer getMessageRate() {
		return messageRate;
	}
	public void setMessageRate(Integer messageRate) {
		this.messageRate = messageRate;
	}
	public String getApprovalOpinions() {
		return approvalOpinions;
	}
	public void setApprovalOpinions(String approvalOpinions) {
		this.approvalOpinions = approvalOpinions;
	}
	
	public TeamInfo getTeam() {
		return team;
	}
	public void setTeam(TeamInfo team) {
		this.team = team;
	}
	public Long getApproved() {
		return approved;
	}
	public void setApproved(Long approved) {
		this.approved = approved;
	}
	private Long approved = 0L;

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getApprovedId() {
		return approvedId;
	}

	public void setApprovedId(Long approvedId) {
		this.approvedId = approvedId;
	}

	public UserInfo getOwner() {
		return owner;
	}

	public void setOwner(UserInfo owner) {
		this.owner = owner;
	}



	public String getClusterIds() {
		return clusterIds;
	}
	public void setClusterIds(String clusterIds) {
		this.clusterIds = clusterIds;
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
	public Integer getTtl() {
		return ttl;
	}
	public void setTtl(Integer ttl) {
		this.ttl = ttl;
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

	public Date getApprovedTime() {
		return approvedTime;
	}
	public void setApprovedTime(Date approvedTime) {
		this.approvedTime = approvedTime;
	}

	public String getClusterNames() {
		return clusterNames;
	}

	public void setClusterNames(String clusterNames) {
		this.clusterNames = clusterNames;
	}
}
