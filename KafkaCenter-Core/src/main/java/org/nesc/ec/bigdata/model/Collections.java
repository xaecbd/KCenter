package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

@TableName("topic_collection")
public class Collections {
	@TableId(value="id",type=IdType.AUTO)
	private Long id;
	@TableField(value="cluster_id")
	private Long clusterId;
	@TableField(value="user_id")
	private Long userId;
	private String name;
	private String type;
	@TableField(exist = false)
	private ClusterInfo cluster;
	@TableField(exist = false)
	private UserInfo user;
	
	public ClusterInfo getCluster() {
		return cluster;
	}
	public void setCluster(ClusterInfo cluster) {
		this.cluster = cluster;
	}
	public UserInfo getUser() {
		return user;
	}
	public void setUser(UserInfo user) {
		this.user = user;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getClusterId() {
		return clusterId;
	}
	public void setClusterId(Long clusterId) {
		this.clusterId = clusterId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
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

}
