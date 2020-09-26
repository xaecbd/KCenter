package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.util.Date;


/**
 * 
 * @author jc1e
 *
 */
@TableName("alert_group")
public class AlertGoup {
	
	@TableId(value="id",type=IdType.AUTO)
	private Long id;
	
	@TableField(value="topic_name")
	private String topicName;
	
	@TableField(exist = false)
	private ClusterInfo cluster;
	
	@TableField(value="consummer_group")
	private String consummerGroup;
	
	@TableField(value="consummer_api")
	private String consummerApi;
	
	private Integer threshold;
	
	private Integer dispause;
	
	@TableField(value="cluster_id")
	private Integer clusterId;
	@TableField(value="mail_to")
	private String mailTo;
	@TableField(value="create_date")
	private Date createDate;
	@TableField(value="owner_id")
	private Long ownerId;

	@TableField(value="disable_alerta")
	private boolean disableAlerta;

	private boolean enable;
	
	
	@TableField(exist = false)
	private UserInfo owner;
	
	private String webhook;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}


	public boolean isDisableAlerta() {
		return disableAlerta;
	}

	public void setDisableAlerta(boolean disableAlerta) {
		this.disableAlerta = disableAlerta;
	}

	public Date getCreateDate() {
		return createDate;
	}
    
	public UserInfo getOwner() {
		return owner;
	}

	public void setOwner(UserInfo owner) {
		this.owner = owner;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Integer getClusterId() {
		return clusterId;
	}

	public void setClusterId(Integer clusterId) {
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

	public String getConsummerGroup() {
		return consummerGroup;
	}

	public void setConsummerGroup(String consummerGroup) {
		this.consummerGroup = consummerGroup;
	}

	public String getConsummerApi() {
		return consummerApi;
	}

	public void setConsummerApi(String consummerApi) {
		this.consummerApi = consummerApi;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Integer getDispause() {
		return dispause;
	}

	public void setDispause(Integer dispause) {
		this.dispause = dispause;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public String getWebhook() {
		return webhook;
	}

	public void setWebhook(String webhook) {
		this.webhook = webhook;
	}

	public ClusterInfo getCluster() {
		return cluster;
	}

	public void setCluster(ClusterInfo cluster) {
		this.cluster = cluster;
	}
}
