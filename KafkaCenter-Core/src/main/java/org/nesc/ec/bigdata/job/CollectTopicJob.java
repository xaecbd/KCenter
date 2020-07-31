package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.KafkaAdminService;
import org.nesc.ec.bigdata.service.TopicInfoService;
import org.nesc.ec.bigdata.service.ZKService;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
@Component
public class CollectTopicJob {

    private static final Logger LOG = LoggerFactory.getLogger(CollectTopicJob.class);

    @Autowired
    ClusterService clusterService;

    @Autowired
    KafkaAdminService kafkaAdminService;

    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    ZKService zkService;

    private  Map<String, TopicInfo> dbTopicMap = new HashMap<>();
    private  Map<String,Map<String, TopicDescription>> descTopicMap = new HashMap<>();
    private  Map<String,Map<String, Config>> descConfigsMap = new HashMap<>();


    /*
     * 1.获取集群的topic信息，和topic表信息进行对比，集群中被删除的topic在topic表中删除
     * 2.获取第一部分的数据，将topic表中没有的数据集群中有的数据写入表中
     * **/
    void collectionTopicData() {
        Map<String,Set<String>> clusterTopicMap = topicDescribe();
        Set<Long> delete = this.dbTopic(clusterTopicMap);
        Map<String,Set<TopicInfo>> map = topicOperation();
        Set<TopicInfo> needInsert = map.getOrDefault(Constants.Operation.INSERT,new HashSet<>());
        Set<TopicInfo> needUpdate = map.getOrDefault(Constants.Operation.UPDATE,new HashSet<>());
        if(!delete.isEmpty()) {
            topicInfoService.deleteByIds(delete);
        }
        if(!needInsert.isEmpty()) {
            if(!topicInfoService.batchInsert(needInsert)) {
                LOG.error("batch insert topic table failed,please check");
            }
        }
        if(!needUpdate.isEmpty()) {
            if(!topicInfoService.batchUpdate(needUpdate)) {
                LOG.error("batch update topic table failed,please check");
            }
        }
    }

    private Map<String,Set<String>> topicDescribe(){
        Map<String,Set<String>> clusterTopicMap = new HashMap<>();
        List<ClusterInfo> clusters = clusterService.getTotalData();
        clusters.forEach(cluster->{
            KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(cluster.getId().toString());
            Map<String, TopicDescription> descTopics = null;
            Map<String, Config> descConfigs = null;
            try {
                Set<String> topics = kafkaAdmins.listTopics();
                clusterTopicMap.put(cluster.getId().toString(),topics);
                descTopics = kafkaAdmins.descTopics(topics);
                descConfigs = kafkaAdmins.descConfigs(topics);
            }catch (InterruptedException | ExecutionException ignored) {
            }finally {
                descTopicMap.put(cluster.getId().toString(), descTopics);
                descConfigsMap.put(cluster.getId().toString(), descConfigs);
            }

        });
        return clusterTopicMap;

    }

    private Map<String, Set<TopicInfo>> topicOperation(){
        Map<String, Set<TopicInfo>> map = new HashMap<>();
        Set<TopicInfo> needInsert = new HashSet<>();
        Set<TopicInfo> needUpdate = new HashSet<>();
        descTopicMap.forEach((clusterId,topicInfo)-> topicInfo.forEach((key, value)->{
            List<TopicPartitionInfo>  topicPartitionInfos = value.partitions();
            int partition = !topicPartitionInfos.isEmpty()?topicPartitionInfos.size():0;
            int nodeSize = !topicPartitionInfos.isEmpty()?(!topicPartitionInfos.get(0).replicas().isEmpty()?topicPartitionInfos.get(0).replicas().size():0):0;
            String newKey = generateKey(clusterId,key);
            Map<String, Config> topicDescribe = descConfigsMap.get(clusterId);
            if(!dbTopicMap.containsKey(newKey)) {
                TopicInfo tInfo = this.updateTTL(topicDescribe,  clusterId, new TopicInfo(), key);
                tInfo.setPartition(partition);
                tInfo.setReplication((short)nodeSize);
                tInfo.setTopicName(key);
                tInfo.setClusterId(clusterId);
                needInsert.add(tInfo);
            }else {
                //需要更新的topic
                TopicInfo tInfo = this.updateTTL(topicDescribe,clusterId, dbTopicMap.get(newKey), key);
                tInfo.setPartition(partition);
                tInfo.setReplication((short)nodeSize);
                needUpdate.add(tInfo);
            }
        }));
        map.put(Constants.Operation.INSERT,needInsert);
        map.put(Constants.Operation.UPDATE,needUpdate);
        return map;
    }

    private Set<Long> dbTopic( Map<String,Set<String>> clusterTopicMap){
        List<TopicInfo> topicInfos = topicInfoService.getTotalData();
        Set<Long> result = new HashSet<>();
        topicInfos.forEach(topic->{
            String key = generateKey(topic.getCluster().getId().toString(),topic.getTopicName());
            dbTopicMap.put(key, topic);
            Set<String> topics = clusterTopicMap.get(topic.getCluster().getId().toString());
            if( topics!=null && !topics.contains(topic.getTopicName())) {
                result.add(topic.getId());
            }
        });
        return result;

    }

    private TopicInfo updateTTL(Map<String, Config> topicDescribe,String clusterId,TopicInfo tInfo,String topicName) {
        try {
            if(topicDescribe!=null) {
                Config con = topicDescribe.get(topicName);
                con.entries().forEach(entry->{
                    if(TopicConfig.DELETE_RETENTION_MS.equalsIgnoreCase(entry.name())) {
                        long ttl = Long.parseLong(entry.value());
                        tInfo.setTtl(ttl>0?ttl:0);
                    }
                });
            }else {
                JSONObject res = zkService.getZK(clusterId).descConfig(topicName);
                if(res.containsKey(TopicConfig.DELETE_RETENTION_MS)) {
                    String ttlStr= res.getString(TopicConfig.DELETE_RETENTION_MS).trim();
                    long ttl = Long.parseLong(ttlStr);
                    tInfo.setTtl(ttl>0?ttl:0);
                }else if(res.containsKey(TopicConfig.RETENTION_MS)){
                    String ttlStr= res.getString(TopicConfig.RETENTION_MS).trim();
                    long ttl = Long.parseLong(ttlStr);
                    tInfo.setTtl(ttl>0?ttl:0);
                }else {
                    tInfo.setTtl(0L);
                }

            }
        } catch (Exception e) {
            LOG.error("topic collect update has error: "+clusterId+Constants.Symbol.Vertical_STR+topicName,e);
        }

        return tInfo;
    }



    private String generateKey(String clusterId,String topicName) {
        return clusterId+"|"+topicName;
    }
}
