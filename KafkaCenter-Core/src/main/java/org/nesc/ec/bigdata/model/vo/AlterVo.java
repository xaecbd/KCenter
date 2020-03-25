package org.nesc.ec.bigdata.model.vo;

import java.util.Date;

/**
 * @author：Truman.P.Du
 * @createDate: 7/12/2019
 * @version:1.0
 * @description:
 */
public class AlterVo {
    private Long id;
    private String topicName;
    private Long clusterId;
    private String clusterName;
    private String consummerGroup;
    private String consummerApi;
    private Integer threshold;
    private Integer dispause;
    private String mailTo;
    private String webhook;
    private Date createTime;
    private String owner;
    private String team;
    private Long ownerId;
    private Long teamId;
    private boolean enable;
    private boolean disableAlerta;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isDisableAlerta() {
        return disableAlerta;
    }

    public void setDisableAlerta(boolean disableAlerta) {
        this.disableAlerta = disableAlerta;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Long getId() {
        return id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Integer getDispause() {
        return dispause;
    }

    public void setDispause(Integer dispause) {
        this.dispause = dispause;
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

    public Long getClusterId() {
        return clusterId;
    }
    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
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

}
