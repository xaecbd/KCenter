package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

@TableName("ksql_info")
public class KsqlClusterInfo {

    @TableId(value="id",type= IdType.AUTO)
    private Long id;

    @TableField(value = "cluster_id")
    private Long clusterId;

    @TableField(value = "cluster_name")
    private String clusterName;

    @TableField(value = "ksql_url")
    private String ksqlUrl;

    @TableField(value = "ksql_serverId")
    private String ksqlServerId;

    @TableField(value = "team_ids")
    private String teamIds;

    private String version;

    public String getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(String teamIds) {
        this.teamIds = teamIds;
    }

    public String getKsqlServerId() {
        return ksqlServerId;
    }

    public void setKsqlServerId(String ksqlServerId) {
        this.ksqlServerId = ksqlServerId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getKsqlUrl() {
        return ksqlUrl;
    }

    public void setKsqlUrl(String ksqlUrl) {
        this.ksqlUrl = ksqlUrl;
    }
    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
