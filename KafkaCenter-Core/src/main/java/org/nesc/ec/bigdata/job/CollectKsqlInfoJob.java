package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.exception.KSQLException;
import org.nesc.ec.bigdata.model.KStreamInfo;
import org.nesc.ec.bigdata.model.KTableInfo;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;
import org.nesc.ec.bigdata.service.KsqlDbService;
import org.nesc.ec.bigdata.service.KsqlStreamService;
import org.nesc.ec.bigdata.service.KsqlTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CollectKsqlInfoJob {

    private static final Logger LOG = LoggerFactory.getLogger(CollectKsqlInfoJob.class);
    @Autowired
    KsqlDbService ksqlDbService;
    @Autowired
    KsqlStreamService ksqlStreamService;
    @Autowired
    KsqlTableService ksqlTableService;



    void runJob(){
        Map<String,KsqlClusterInfo> clusterInfoMap = transToUrlMap();
        runStreamJob(clusterInfoMap);
        runTableJob(clusterInfoMap);
    }

    private void runStreamJob(Map<String,KsqlClusterInfo> clusterInfoMap){
        String query="SHOW STREAMS;";
        List<KsqlInfoEntity> ksqlInfoEntityList = listStreamsOrTable(clusterInfoMap,query);
        Set<KStreamInfo> kStreamInfos = generateToKstreamEntity(ksqlInfoEntityList);
        streamDesert(kStreamInfos);
    }

    private void runTableJob(Map<String,KsqlClusterInfo> clusterInfoMap){
        String query = "SHOW TABLES;";
        List<KsqlInfoEntity> ksqlInfoEntityList = listStreamsOrTable(clusterInfoMap,query);
        Set<KTableInfo> kTableInfos = generateToKtableEntity(ksqlInfoEntityList);
        tableDesert(kTableInfos);
    }

    private void streamDesert(Set<KStreamInfo> kStreamInfos){
        Set<KStreamInfo> kStreamInfoSet = new HashSet<>(ksqlStreamService.getStreamTotalData());
        Set<KStreamInfo> kStreamInfoSetFromServer = new HashSet<>(kStreamInfos);
        kStreamInfoSetFromServer.removeAll(kStreamInfoSet);
        kStreamInfoSet.removeAll(kStreamInfos);
        ksqlStreamService.insertToDb(kStreamInfoSetFromServer);
        Set<Long> needToDelete = kStreamInfoSet.stream().map(KStreamInfo::getId).collect(Collectors.toSet());
        ksqlStreamService.delToDb(needToDelete);
    }

    private void tableDesert(Set<KTableInfo> kTableInfos){
        Set<KTableInfo> kTableInfoSet = new HashSet<>(ksqlTableService.getTotalData());
        Set<KTableInfo> kTableInfoSetFromServer = new HashSet<>(kTableInfos);
        kTableInfoSetFromServer.removeAll(kTableInfoSet);
        kTableInfoSet.removeAll(kTableInfos);

        ksqlTableService.insertToDb(kTableInfoSetFromServer);
        Set<Long> needToDelete = kTableInfoSet.stream().map(KTableInfo::getId).collect(Collectors.toSet());
        ksqlStreamService.delToDb(needToDelete);
    }

    private Map<String,KsqlClusterInfo> transToUrlMap(){
        List<KsqlClusterInfo> list = getKsqlClusterInfo();
        return list.stream().collect(Collectors.toMap(KsqlClusterInfo::getKsqlServerId, k->k));
    }


    private Set<KStreamInfo> generateToKstreamEntity(List<KsqlInfoEntity> ksqlInfoEntityList){
      return  ksqlInfoEntityList.stream().map(ksqlInfoEntity ->
              new KStreamInfo(ksqlInfoEntity.getName(),ksqlInfoEntity.getClusterId(),ksqlInfoEntity.getTopic())).collect(Collectors.toSet());

    }

    private Set<KTableInfo> generateToKtableEntity(List<KsqlInfoEntity> ksqlInfoEntityList){
       return ksqlInfoEntityList.stream().map(ksqlInfoEntity -> new KTableInfo(ksqlInfoEntity.clusterId,ksqlInfoEntity.getName(),ksqlInfoEntity.getTopic()))
               .collect(Collectors.toSet());
    }


    private List<KsqlClusterInfo> getKsqlClusterInfo(){
        return ksqlStreamService.getTotalData();
    }



    private List<KsqlInfoEntity> listStreamsOrTable(Map<String,KsqlClusterInfo> ksqlUrlMap,String query){
        List<KsqlInfoEntity> ksqlInfoEntityList = new ArrayList<>();
        ksqlUrlMap.forEach((ksqlServerId,ksqlCluster)->{
            String[] urls = ksqlCluster.getKsqlUrl().split(",");
            for (String url:urls){
                try{
                    String result =  ksqlDbService.executeQueryScript(url,query,true);
                    JSONArray resultArray = JSON.parseArray(result);
                    JSONObject resultObj = resultArray.getJSONObject(0);
                    ksqlInfoEntityList.addAll(generateKsqlInfoEntity(resultObj,ksqlCluster));
                    break;
                }catch (KSQLException e){
                    LOG.error("list stream has error",e);
                }
            }
        });
        return ksqlInfoEntityList;
    }

    private List<KsqlInfoEntity> generateKsqlInfoEntity(JSONObject resultObj,KsqlClusterInfo ksqlCluster){
        List<KsqlInfoEntity> ksqlInfoEntityList = new ArrayList<>();
        JSONArray array = new JSONArray();
        if(resultObj.containsKey("streams")){
            array.addAll(resultObj.getJSONArray("streams"));
        }
        if(resultObj.containsKey("tables")){
            array.addAll(resultObj.getJSONArray("tables"));
        }
        for (int i = 0; i< array.size(); i++){
            JSONObject object = array.getJSONObject(i);
            String name = object.getString("name");
            String topic = object.getString("topic");
            long clusterId = ksqlCluster.getId();
            KsqlInfoEntity ksqlInfoEntity = new KsqlInfoEntity(clusterId,name,topic);
            ksqlInfoEntityList.add(ksqlInfoEntity);
        }
        return ksqlInfoEntityList;
    }

    class KsqlInfoEntity{
        private long clusterId;
        private String name;
        private String topic;

        public KsqlInfoEntity(long clusterId, String name, String topic) {
            this.clusterId = clusterId;
            this.name = name;
            this.topic = topic;
        }

        public long getClusterId() {
            return clusterId;
        }

        public String getName() {
            return name;
        }

        public String getTopic() {
            return topic;
        }
    }
}
