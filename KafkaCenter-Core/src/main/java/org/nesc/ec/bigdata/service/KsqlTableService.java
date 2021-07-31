package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.exception.KSQLException;
import org.nesc.ec.bigdata.mapper.KTableMapper;
import org.nesc.ec.bigdata.model.KTableInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.KsqlInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KsqlTableService {

    public  static final Logger LOGGER = LoggerFactory.getLogger(KsqlTableService.class);

    @Autowired
    KsqlDbService ksqlDbService;
    @Autowired
    KTableMapper kTableMapper;

    public String createTable(KsqlInfoVo ksqlInfoVo,UserInfo userInfo) throws KSQLException {
        String clusterName = ksqlInfoVo.getClusterName();
        String ksqlServerId = ksqlInfoVo.getKsqlServerId();
        KTableInfo kTableInfo = ksqlInfoVo.getkTableInfo();
        try{
            String message =  ksqlDbService.executeKsqlScript(ConnectCache.getKsqlUrl(ksqlServerId,clusterName),kTableInfo.getScript());
            if(!saveToTable(kTableInfo,userInfo)){
                LOGGER.error("save to ksql table has error,please check");
            }
            return message;
        }catch (KSQLException e){
            throw e;
        }
    }

    public void insertToDb(Set<KTableInfo> kTableInfoList){
        for (KTableInfo kTableInfo : kTableInfoList){
            if(kTableMapper.insert(kTableInfo) < 0){
                LOGGER.error("ksql table has error,ktable is:,{}",kTableInfo);
            }
        }

    }
    public void delToDb(Set<Long> ids){
        for (Long id : ids){
            if(kTableMapper.deleteById(id) < 0){
                LOGGER.error("ksql table has error,ktable is:,{}",id);
            }
        }

    }

    public List<KTableInfo> getTotalData(){
        return kTableMapper.selectList(null);
    }

    public void deleteStreamByCluster(long clusterId){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("cluster_id",clusterId);
        kTableMapper.deleteByMap(paramsMap);
    }


    public List<KTableInfo> getKsqlTableList(UserInfo userInfo,String clusterId){
        if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(userInfo.getRole().getDescription())){
            return  kTableMapper.selectTableByClusterId(Integer.parseInt(clusterId));
        }
        if(Objects.nonNull(userInfo.getTeamIDs()) && userInfo.getTeamIDs().size()>0){
            return kTableMapper.selectKTableInfoByTeam(userInfo.getTeamIDs(),Integer.parseInt(clusterId));
        }
        return new ArrayList<>();

    }

    public JSONObject describeTable(String ksqlServerId,String clusterName,String tableName) throws KSQLException {
        return ksqlDbService.describeStreamOrTable(ksqlServerId,clusterName,tableName);
    }

    public String dropTable(String ksqlServerId,String clusterName,String tableName,String id) throws KSQLException {
        String result = ksqlDbService.dropStreamOrTable(ksqlServerId,clusterName,tableName,false);
        if(kTableMapper.deleteById(Long.parseLong(id))>0){
            return  result;
        }
        return  "sql table delete kql table failed!";
    }

    public List<JSONObject> selectTable(String ksqlServerId,String clusterName,String tableName) throws KSQLException {
        return  ksqlDbService.selectStreamOrTable(ksqlServerId,clusterName,tableName);

    }

    public JSONArray showQueries(String ksqlServerId,String clusterName) throws KSQLException {
        String query = "SHOW QUERIES;";
        JSONArray array = new JSONArray();
         try{
             String result = ksqlDbService.executeQueryScript(ConnectCache.getKsqlUrl(ksqlServerId,clusterName),query,true);
             JSONArray resultArray = JSONArray.parseArray(result);
             for (int i = 0;i<resultArray.size();i++){
                 JSONObject object = resultArray.getJSONObject(i);
                 JSONArray queriesArray = object.getJSONArray("queries");
                 array.addAll(queriesArray);
             }
             return array;
         }catch (KSQLException e){
            throw e;
        }catch (Exception e1){
            LOGGER.error("show queries has error,",e1);
        }
         return array;
    }

    public String terminateQuery(String ksqlServerId,String clusterName,String queryId) throws KSQLException {
        String query = "TERMINATE "+queryId+" ;";
        return ksqlDbService.executeKsqlScript(ConnectCache.getKsqlUrl(ksqlServerId,clusterName),query);
    }


    private boolean saveToTable(KTableInfo kTableInfo, UserInfo userInfo){
        kTableInfo.setCreateTime(new Date());
        kTableInfo.setOwnerId(userInfo.getId());
        return kTableMapper.insert(kTableInfo) > 0;
    }

}
