package org.nesc.ec.bigdata.model.vo;

import org.nesc.ec.bigdata.model.ConnectorJob;

public class ConnectorVo {

    private String clusterName;
    private ConnectorJob connectorJob;

    public ConnectorJob getConnectorJob() {
        return connectorJob;
    }

    public void setConnectorJob(ConnectorJob connectorJob) {
        this.connectorJob = connectorJob;
    }



    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }


}
