package org.nesc.ec.bigdata.model;

/**
 * @author Truman.P.Du
 * @date 2020/02/27
 * @description
 */
public class KsqlInfo {

    public KsqlInfo(String ksqlServerId, String clusterName, String version) {
        this.ksqlServerId = ksqlServerId;
        this.clusterName = clusterName;
        this.version = version;
    }

    public KsqlInfo(String ksqlServerId, String clusterName, String version,String ksqlAddress) {
        this.ksqlServerId = ksqlServerId;
        this.clusterName = clusterName;
        this.version = version;
        this.ksqlAddress = ksqlAddress;
    }

    public KsqlInfo() {
    }

    private String ksqlServerId;
    private String clusterName;
    private String version;
    private String serverStatus;

    private boolean ksqlHealthy;
    private boolean kafkaHealthy;
    private boolean metastoreHealthy;
    private boolean commandRunnerHealthy;

    private long id;

    private String ksqlAddress;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServerStatus() {
        return serverStatus;
    }

    public void setServerStatus(String serverStatus) {
        this.serverStatus = serverStatus;
    }

    public boolean isCommandRunnerHealthy() {
        return commandRunnerHealthy;
    }

    public void setCommandRunnerHealthy(boolean commandRunnerHealthy) {
        this.commandRunnerHealthy = commandRunnerHealthy;
    }

    public String getKsqlServerId() {
        return ksqlServerId;
    }

    public void setKsqlServerId(String ksqlServerId) {
        this.ksqlServerId = ksqlServerId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isKsqlHealthy() {
        return ksqlHealthy;
    }

    public void setKsqlHealthy(boolean ksqlHealthy) {
        this.ksqlHealthy = ksqlHealthy;
    }

    public boolean isKafkaHealthy() {
        return kafkaHealthy;
    }

    public void setKafkaHealthy(boolean kafkaHealthy) {
        this.kafkaHealthy = kafkaHealthy;
    }

    public boolean isMetastoreHealthy() {
        return metastoreHealthy;
    }

    public void setMetastoreHealthy(boolean metastoreHealthy) {
        this.metastoreHealthy = metastoreHealthy;
    }

    public String getKsqlAddress() {
        return ksqlAddress;
    }

    public void setKsqlAddress(String ksqlAddress) {
        this.ksqlAddress = ksqlAddress;
    }
}
