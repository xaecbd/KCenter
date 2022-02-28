package org.nesc.ec.bigdata.model.vo;

import org.nesc.ec.bigdata.model.KStreamInfo;
import org.nesc.ec.bigdata.model.KTableInfo;

public class KsqlInfoVo {
    private KStreamInfo kStreamInfo;
    private String clusterName;
    private String ksqlServerId;
    private KTableInfo kTableInfo;
    public KTableInfo getkTableInfo() {
        return kTableInfo;
    }

    public void setkTableInfo(KTableInfo kTableInfo) {
        this.kTableInfo = kTableInfo;
    }

    public KStreamInfo getkStreamInfo() {
        return kStreamInfo;
    }

    public void setkStreamInfo(KStreamInfo kStreamInfo) {
        this.kStreamInfo = kStreamInfo;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getKsqlServerId() {
        return ksqlServerId;
    }

    public void setKsqlServerId(String ksqlServerId) {
        this.ksqlServerId = ksqlServerId;
    }
}
