package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.exception.ConnectorException;
import org.nesc.ec.bigdata.mapper.ConnectJobMapper;
import org.nesc.ec.bigdata.model.ConnectorJob;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.ConnectorVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ConnectorJobService {

    @Autowired
    ConnectJobMapper connectJobMapper;

    @Autowired
    ConnectClusterService connectClusterService;


    public static final Logger LOGGER = LoggerFactory.getLogger(ConnectorJobService.class);

    public static final ExecutorService executors = Executors.newFixedThreadPool(20);

    public boolean createConnectJob(ConnectorJob connectorJob){
        if(checkJobExit(connectorJob.getName(),String.valueOf(connectorJob.getClusterId()))){
            return connectJobMapper.insert(connectorJob) >0;
        }
        return false;
    }


    public boolean saveConnector(ConnectorVo connectorVo,UserInfo userInfo) throws ConnectorException {
        ConnectorJob connectorJob = connectorVo.getConnectorJob();
        connectorJob.setOwnerId(userInfo.getId());
        connectClusterService.createConnector(connectorVo);
        return createConnectJob(connectorJob);
    }

    public void saveConnector(Set<ConnectorJob> connectorJobSet)  {
        for (ConnectorJob connectorJob:connectorJobSet){
            if( connectJobMapper.insert(connectorJob) < 0) {
                LOGGER.info("save to db has error,{}",connectorJob.toString());
            }
        }
    }

    public void deleteConnector(Set<ConnectorJob> connectorJobSet)  {
        for (ConnectorJob connectorJob:connectorJobSet){
            if( connectJobMapper.deleteById(connectorJob.getId()) < 0) {
                LOGGER.info("delete from db has error,{}",connectorJob);
            }
        }
    }

    public boolean updateConnector(ConnectorVo connectorVo) throws ConnectorException {
        ConnectorJob connectorJob = connectorVo.getConnectorJob();
        connectClusterService.updateConnector(connectorVo);
        connectorJob.setUpdateTime(new Date());
        return updateConnectJob(connectorJob);
    }

    public boolean deleteConnector(ConnectorJob connectorJob,String clusterName) throws ConnectorException {
        connectClusterService.deleteConnector(String.valueOf(connectorJob.getClusterId()),clusterName,connectorJob.getName());
        return deleteConnectJob(connectorJob.getId());
    }

    public boolean updateConnectJob(ConnectorJob connectorJob){

        // 将原始数据修改为历史数据
        connectJobMapper.upToHistory(connectorJob.getId());
        JSONObject object = new JSONObject();
        object.put("name",connectorJob.getName());
        object.put("config", JSON.parseObject(connectorJob.getScript()));
        connectorJob.setScript(object.toString());

        return connectJobMapper.insert(connectorJob) > 0;
    }

    public boolean deleteConnectJob(long id){
        // 添加历史记录
        return connectJobMapper.upToHistory(id) > 0;
    }

    public List<ConnectorJob> selectConnectJobList(UserInfo userInfo,int clusterId){
        if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(userInfo.getRole().getDescription())){
            return  connectJobMapper.getConnectorJobByClusterId(clusterId);
        }
        return connectJobMapper.getConnectorJobByTeams(userInfo.getTeamIDs(),clusterId);
    }

    public List<ConnectorJob> selectConnectAndState(UserInfo userInfo,int clusterId,String clusterName){
        return selectConnectJobList(userInfo,clusterId);
//        return connectorJobList.parallelStream().map(connectorJob -> {
//            try {
//                String status =  connectClusterService.status(String.valueOf(clusterId),clusterName,connectorJob.getName());
//                connectorJob.setState(status);
//            } catch (ConnectorException e) {
//                LOGGER.error("fetch connector status has error",e);
//            }
//            return connectorJob;
//        }).collect(Collectors.toList());
    }




    public List<ConnectorJob> selectConnectorJob(){
        return  connectJobMapper.selectList(null);
    }
    public JSONArray  connectorJobPlugins(String clusterId,String clusterName) throws ConnectorException {
        return connectClusterService.connectorPlugins(clusterId,clusterName);
    }

    public JSONArray connectorJobTaskInfo(String clusterId,String clusterName,String connectorName) throws ConnectorException {
        return connectClusterService.connectorTask(clusterId,clusterName,connectorName);
    }

    public ConnectorJob searchConnectorJob(String id) throws ConnectorException {
        return connectJobMapper.getConnectorJobById(Long.parseLong(id));
    }

    public JSONArray validateConnector(ConnectorVo connectorVo) throws ConnectorException {
        return  connectClusterService.validateConnector(connectorVo);
    }

    private boolean checkJobExit(String name,String clusterId){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("name",name);
        paramMap.put("cluster_id",clusterId);
        List<ConnectorJob> connectorJobList = connectJobMapper.selectByMap(paramMap);
        return CollectionUtils.isEmpty(connectorJobList);
    }

    public String connectorTaskRestart(String connectorName,String taskId,String clusterName,String clusterId) throws ConnectorException {
        return connectClusterService.restartConnectorTask(connectorName,taskId,clusterName,clusterId);
    }

    public String connectorJobRestart(ConnectorVo connectorVo) throws ConnectorException {
        ConnectorJob connectorJob =  connectorVo.getConnectorJob();
        return connectClusterService.restartConnectorJob(connectorJob.getName(),connectorVo.getClusterName(),String.valueOf(connectorJob.getClusterId()));
    }

    public String connectorJobPause(ConnectorVo connectorVo) throws ConnectorException {
        ConnectorJob connectorJob =  connectorVo.getConnectorJob();
        return connectClusterService.pauseConnectorJob(connectorJob.getName(),connectorVo.getClusterName(),String.valueOf(connectorJob.getClusterId()));
    }

    public String connectorJobResume(ConnectorVo connectorVo) throws ConnectorException {
        ConnectorJob connectorJob =  connectorVo.getConnectorJob();
        return connectClusterService.resumeConnectorTask(connectorJob.getName(),connectorVo.getClusterName(),String.valueOf(connectorJob.getClusterId()));
    }


}
