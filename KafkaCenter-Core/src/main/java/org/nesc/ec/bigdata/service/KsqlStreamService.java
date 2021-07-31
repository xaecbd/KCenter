package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.ConnectCache;
import org.nesc.ec.bigdata.exception.KSQLException;
import org.nesc.ec.bigdata.mapper.KStreamInfoMapper;
import org.nesc.ec.bigdata.mapper.KsqlClusterInfoMapper;
import org.nesc.ec.bigdata.model.KStreamInfo;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;
import org.nesc.ec.bigdata.model.KsqlInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.KsqlInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class KsqlStreamService {

    public   static final Logger LOGGER = LoggerFactory.getLogger(KsqlStreamService.class);

    @Autowired
    KsqlClusterInfoMapper ksqlClusterInfoMapper;
    @Autowired
    KsqlDbService ksqlDbService;
    @Autowired
    KStreamInfoMapper kStreamInfoMapper;
    @Autowired
    RestService restService;

    public List<KsqlClusterInfo> getTotalData() {
        return ksqlClusterInfoMapper.selectList(null);
    }

    public void insertToDb(Set<KStreamInfo> kStreamInfos){
        for (KStreamInfo kStreamInfo : kStreamInfos){
            if(kStreamInfoMapper.insert(kStreamInfo)<0){
                LOGGER.error("insert to kstream table has error,{}",kStreamInfo);
            }
        }
    }

    public void deleteStreamByCluster(long clusterId){
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("cluster_id",clusterId);
        kStreamInfoMapper.deleteByMap(paramsMap);
    }

    public void delToDb(Set<Long> ids){
        for (long id : ids){
            if(kStreamInfoMapper.deleteById(id)<0){
                LOGGER.error("insert to kstream table has error,{}",id);
            }
        }
    }

    public List<KStreamInfo> getClusterKStreamData(int clusterId){
        return  kStreamInfoMapper.selectKStreamByCluster(clusterId);
    }

    public List<KStreamInfo> getStreamTotalData(){
        return  kStreamInfoMapper.selectList(null);
    }

    public List<KStreamInfo> getClusterKStreamByTeam(List<Long> teamIds,int clusterId){
        return kStreamInfoMapper.getKStreamInfoByTeam(teamIds,clusterId);

    }

    public List<KsqlInfo> getKsqlList(List<KsqlClusterInfo> ksqlClusters) {
        List<KsqlInfo> list = new ArrayList<>();
        ksqlClusters.forEach(ksqlClusterInfo -> {
            String ksqlServiceId = ksqlClusterInfo.getKsqlServerId();
            String clusterName = ksqlClusterInfo.getClusterName();
            String version = ksqlClusterInfo.getVersion();
            String ksqlUrl = ksqlClusterInfo.getKsqlUrl();
            String[] urls = ksqlUrl.split(",");
            KsqlInfo ksqlInfo = new KsqlInfo(ksqlServiceId, clusterName, version, ksqlUrl);
            ksqlInfo.setId(ksqlClusterInfo.getId());
            KsqlInfo ksql = null;
            for (String url : urls) {
                ksql = setHealthyInfo(url, ksqlInfo);
                String key = clusterName + "|" + ksqlServiceId;
                ConnectCache.KSQL_URL_MAP.put(key, url);
                if (ksql != null) {
                    ksqlInfo = ksql;
                }
            }
            list.add(ksqlInfo);

        });
        return list;
    }

    private KsqlInfo setHealthyInfo(String url,KsqlInfo ksqlInfo){
        url = restService.generatorUrl(url,"/healthcheck");
        ResponseEntity<String> responseEntity = ksqlDbService.sendRequest(url, HttpMethod.GET,null);
        if(responseEntity.getStatusCode().is2xxSuccessful()){
            JSONObject object = JSON.parseObject(responseEntity.getBody());
            boolean ksqlHealthy = object.getBooleanValue("isHealthy");
            JSONObject detailObject = object.getJSONObject("details");
            boolean metastoreHealthy = detailObject.containsKey("metastore") && detailObject.getJSONObject("metastore").getBooleanValue("isHealthy");
            boolean kafkaHealthy = detailObject.containsKey("kafka") && detailObject.getJSONObject("kafka").getBooleanValue("isHealthy");
            boolean commandHealthy = detailObject.containsKey("commandRunner") && detailObject.getJSONObject("commandRunner").getBooleanValue("isHealthy");
            ksqlInfo.setKsqlHealthy(ksqlHealthy);
            ksqlInfo.setKafkaHealthy(kafkaHealthy);
            ksqlInfo.setMetastoreHealthy(metastoreHealthy);
            ksqlInfo.setCommandRunnerHealthy(commandHealthy);
        }else {
            LOGGER.error("get ksql cluster healthy has error,response:",responseEntity.getBody());
        }
        return ksqlInfo;
    }

    public JSONObject describeStream(String serverId,String clusterName,String streamName) throws KSQLException {
        return ksqlDbService.describeStreamOrTable(serverId,clusterName,streamName);
    }

    public String dropStream(String serverId,String clusterName,String streamName,String id) throws KSQLException {
        String message = ksqlDbService.dropStreamOrTable(serverId,clusterName,streamName,true);
        if(kStreamInfoMapper.deleteById(Long.parseLong(id))>0){
            return message;
        }else {
            return "sql table delete stream failed!";
        }
    }

    public List<JSONObject> selectStream(String serverId, String clusterName, String streamName) throws KSQLException {
         return ksqlDbService.selectStreamOrTable(serverId,clusterName,streamName);
    }



    public String createStream(KsqlInfoVo ksqlInfoVo, UserInfo user) throws KSQLException {
        KStreamInfo kStreamInfo = ksqlInfoVo.getkStreamInfo();
        StringBuilder stringBuilder = new StringBuilder();
        String config = "";
        if(Objects.nonNull(kStreamInfo.getConfig()) && !Objects.equals("",kStreamInfo.getConfig())){
            config = generateConfig(kStreamInfo.getConfig(),kStreamInfo);
            if(!Objects.equals("",config)){
                stringBuilder.append(config);
            }
        }
        if(kStreamInfo.getStreamType()==1 && Objects.isNull(kStreamInfo.getTopic())){
            kStreamInfo.setTopic(kStreamInfo.getName().toUpperCase());
        }
        stringBuilder.append(kStreamInfo.getScript());
        kStreamInfo.setConfig(config);
        try {
            String message =  ksqlDbService.executeKsqlScript(ConnectCache.KSQL_URL_MAP.get(ksqlInfoVo.getClusterName()+"|"+ ksqlInfoVo.getKsqlServerId()),stringBuilder.toString());
            if(!saveStreamToSql(user,kStreamInfo)){
                LOGGER.error("save to db has error,please check");
            }
            return message;
        } catch (KSQLException e) {
            throw e;
        }
    }

    private boolean saveStreamToSql(UserInfo userInfo,KStreamInfo kStreamInfo){
        long userId = userInfo.getId();
        kStreamInfo.setOwnerId(userId);
        kStreamInfo.setCreateTime(new Date());
        return kStreamInfoMapper.insert(kStreamInfo)>0;
    }

    private String generateConfig(String config,KStreamInfo kStreamInfo){
        JSONObject object = JSON.parseObject(config);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key:object.keySet()){
            if("KAFKA_TOPIC".equalsIgnoreCase(key)){
                kStreamInfo.setTopic(object.getString(key));
            }
            stringBuilder.append(key).append("=").append("'").append(object.getString(key)).append("',");
        }
        String result = stringBuilder.toString();
        return result.substring(0,result.length()-1);
    }
}
