package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.exception.ConnectorException;
import org.nesc.ec.bigdata.model.ConnectorInfo;
import org.nesc.ec.bigdata.model.ConnectorJob;
import org.nesc.ec.bigdata.service.ConnectClusterService;
import org.nesc.ec.bigdata.service.ConnectInfoService;
import org.nesc.ec.bigdata.service.ConnectorJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CollectConnectorJob {

    public static final Logger LOGGER = LoggerFactory.getLogger(CollectConnectorJob.class);

    @Autowired
    ConnectInfoService connectInfoService;
    @Autowired
    ConnectorJobService connectorJobService;
    @Autowired
    ConnectClusterService connectClusterService;


    void runJob(){
        sqlConnectorJobOperator();
    }


    private void sqlConnectorJobOperator(){
        Set<ConnectorJob> clusterConnectorJobs = selectClusterConnectors();
        List<ConnectorJob> connectorJobList = connectorJobService.selectConnectorJob();
        Set<ConnectorJob> connectorJobs = new HashSet<>(connectorJobList);
        needToDelete(connectorJobs,clusterConnectorJobs);
        needToSave(connectorJobs,clusterConnectorJobs);
    }

    private Set<ConnectorJob> selectClusterConnectors(){
        Set<ConnectorJob> connectorJobs = new HashSet<>();
        List<ConnectorInfo> connectorInfoList =   connectInfoService.selectConnectList();
        for (ConnectorInfo connectorInfo:connectorInfoList){
            try{
                JSONArray connectorArray =  connectClusterService.selectConnectors(connectorInfo.getUrl());
                for (int i = 0;i<connectorArray.size();i++){
                    String connector = connectorArray.getString(i);
                    JSONObject statusObj = connectClusterService.status(connectorInfo.getUrl(),connector);
                    String state = statusObj.getJSONObject("connector").getString("state");
                    String type = statusObj.getString("type");
                    JSONObject scriptObject = connectClusterService.connectorConfig(connectorInfo.getUrl(),connector);
                    String className = scriptObject.getString("connector.class");
                    String script  = transToObject(connector,scriptObject);
                    ConnectorJob connectorJob = new ConnectorJob(connector,type,className,Integer.parseInt(String.valueOf(connectorInfo.getId())),state,script);
                    connectorJobs.add(connectorJob);
                }
            }catch (ConnectorException connectorException){
                LOGGER.error("cluster fetch connectors has error,clusterName:{}",connectorInfo.getName(),connectorException);
            }
        }
        return connectorJobs;
    }

    private String transToObject(String connectorName,JSONObject object){
        JSONObject resultObject = new JSONObject();
        resultObject.put("name",connectorName);
        resultObject.put("config",object);
        return resultObject.toString();
    }


    private void needToDelete(Set<ConnectorJob> sqlConnectJob,Set<ConnectorJob> clusterConnectJob){
        Set<ConnectorJob> connectorJobs = new HashSet<>(sqlConnectJob);
        connectorJobs.removeAll(clusterConnectJob);
        connectorJobService.deleteConnector(connectorJobs);
    }

    public void needToSave(Set<ConnectorJob> sqlConnectJob,Set<ConnectorJob> clusterConnectJob){
        Set<ConnectorJob> connectorJobs = new HashSet<>(clusterConnectJob);
        connectorJobs.removeAll(sqlConnectJob);
        connectorJobService.saveConnector(connectorJobs);
    }


}
