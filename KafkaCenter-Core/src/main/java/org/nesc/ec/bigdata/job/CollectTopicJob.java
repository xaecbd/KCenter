package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.common.errors.UnsupportedVersionException;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.common.util.JmxCollector;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.common.util.TimeUtil;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.service.*;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    InitConfig initConfig;

     /**
      * Synchronize cluster and Table topic information
      * */
     void collectionTopicData(){
        List<ClusterInfo> clusters = clusterService.getTotalData();
        clusters.forEach(clusterInfo -> {
            try{
                if (initConfig.isMonitorCollectorIncludeEnable() && !clusterInfo.getLocation()
                        .equalsIgnoreCase(initConfig.getMonitorCollectorIncludelocation())) {
                    return;
                }
                LOG.debug("collect topic data start,{}",clusterInfo.toString());
                long start  = System.currentTimeMillis();
                Set<String> clusterTopics = clusterTopic(clusterInfo);
                Map<String, TopicDescription> topicDescribe = takeTopicDescribe(clusterInfo.getId().toString(),clusterTopics);
                Map<String, Config> topicConfig = takeTopicConfig(clusterInfo.getId().toString(),clusterTopics);
                String version = clusterInfo.getKafkaVersion().split("\\.")[0];
                Map<String, Long> topicSizeMap =Integer.parseInt(version)>1? takeTopicSize(clusterInfo):unSupportVersionTopicSize(clusterInfo,clusterTopics,topicDescribe);
                List<TopicInfo> topicInfoList = dbTopic(clusterInfo);
                Set<Long> needDeleteFromDb = needToDeleteDb(clusterTopics,topicInfoList);
                Set<TopicInfo> needToInsertDb = mergeWithTopicSize(topicSizeMap,needToInsertDb(clusterTopics,topicInfoList,clusterInfo,topicDescribe,topicConfig));
                Set<TopicInfo> needToUpdateDb = mergeWithTopicSize(topicSizeMap,needToUpdateDb(clusterTopics,topicInfoList,clusterInfo,topicDescribe,topicConfig));
                if(!needDeleteFromDb.isEmpty()) {
                    topicInfoService.deleteByIds(needDeleteFromDb);
                }
                if(!needToInsertDb.isEmpty()) {
                    if(!topicInfoService.batchInsert(needToInsertDb)) {
                        LOG.error("batch insert topic table failed,please check");
                    }
                }
                if(!needToUpdateDb.isEmpty()) {
                    if(!topicInfoService.batchUpdate(needToUpdateDb)) {
                        LOG.error("batch update topic table failed,please check");
                    }
                }
                LOG.debug("collect topic data end,{},cost time is {}",clusterInfo.toString(),(System.currentTimeMillis()-start));
            }catch (Exception e){
                LOG.error("cluster:"+clusterInfo.getName()+" collection topic config has error",e);
            }

        });
    }

    /** return topic list of cluster
     *  @param clusterInfo requiredNonNull
     *  */
    private Set<String> clusterTopic(ClusterInfo clusterInfo){
       Set<String> topicList = new HashSet<>();
        String clusterId = clusterInfo.getId().toString();
        KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);

        try {
            topicList = kafkaAdmins.listTopics();
        }catch (InterruptedException | ExecutionException ignored) {
        }
        return topicList;

    }

    /**
     * take topic describe by cluster
     * */
    private  Map<String, TopicDescription> takeTopicDescribe(String clusterId, Set<String> topics){
        KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);
        return kafkaAdmins.descTopics(topics);
    }

    /**
     * get topic of cluster configs
     * */
    private Map<String, Config> takeTopicConfig(String clusterId,Set<String> topics){
        KafkaAdmins kafkaAdmins = kafkaAdminService.getKafkaAdmins(clusterId);
        Map<String, Config> topicConfig = new HashMap<>(2 << 2);
        try {
            topicConfig = kafkaAdmins.descConfigs(topics);
        }catch  (InterruptedException | ExecutionException ignored){

        }
        return topicConfig;

    }

    /**merge topic list and file size map to combination a topic list
     * @param topicSizeMap the file size map
     * @param topicInfoSet Topic List that has been processed
     * */
    private Set<TopicInfo> mergeWithTopicSize(Map<String, Long> topicSizeMap,Set<TopicInfo> topicInfoSet){
        return topicInfoSet.stream().map(topicInfo -> {
              String topicName = topicInfo.getTopicName();
              long size = topicSizeMap.getOrDefault(topicName,-1L);
              topicInfo.setFileSize(size);
              return topicInfo;
          }).collect(Collectors.toSet());
    }


    /**return the topicInfo list from db
     * @param  clusterInfo cluster requiredNonNull
     * */
    private List<TopicInfo> dbTopic(ClusterInfo clusterInfo){
        return topicInfoService.selectAllByClusterId(clusterInfo.getId().toString());
    }

    /**return the topic list need to delete form Db
     * @param  topics the kafka cluster topics
     * @param topicInfoList topic list from db <br/>
     * if the topic is not included in the topics,this topic will be deleted in db
     * */
    private Set<Long> needToDeleteDb(Set<String> topics,List<TopicInfo> topicInfoList){
        return topicInfoList.stream().filter(topicInfo -> topics!=null && !topics.contains(topicInfo.getTopicName())).map(TopicInfo::getId).collect(Collectors.toSet());
    }

    /**return the topic list need to insert into db table
     * @param topics the kafka cluster topics
     * @param  topicInfoList topic list in db
     * @param clusterInfo  clusterInfo nonNull <br/>
     * @param  topicDescribe the partition,nodes of topic
     * @param  topicConfig topic`s config information <br/>
     * if the topic is not included in the topicInfoList and the topic is included in the topics,
     * the topic will be insert to db
     * */
    private Set<TopicInfo> needToInsertDb(Set<String> topics,List<TopicInfo> topicInfoList,ClusterInfo clusterInfo,
                                          Map<String, TopicDescription> topicDescribe,Map<String, Config> topicConfig){
        Set<TopicInfo> needInsert = new HashSet<>();
        Set<String> dbTopics = topicInfoList.stream().map(TopicInfo::getTopicName).collect(Collectors.toSet());
        topics.stream().filter(topic->!dbTopics.contains(topic)).forEach(topic->{
            try {
                TopicDescription topicDescription = topicDescribe.getOrDefault(topic,null);
                List<TopicPartitionInfo>  topicPartitionInfos = Objects.isNull(topicDescription)?new ArrayList<>():topicDescription.partitions();
                int partition = !CollectionUtils.isEmpty(topicPartitionInfos)?topicPartitionInfos.size():0;
                int replicas = !CollectionUtils.isEmpty(topicPartitionInfos)?(!topicPartitionInfos.get(0).replicas().isEmpty()?topicPartitionInfos.get(0).replicas().size():0):0;
                TopicInfo topicInfo  =  this.updateTTL(topicConfig,clusterInfo.getId().toString(), new TopicInfo(), topic);
                topicInfo.setPartition(partition);
                topicInfo.setClusterId(clusterInfo.getId().toString());
                topicInfo.setReplication((short) replicas);
                topicInfo.setTopicName(topic);
                needInsert.add(topicInfo);
            } catch (Exception e) {
                LOG.error("need To insert db has error!",e);
            }
        });
        return needInsert;
    }

    /**return the topic list needed to updated in db
     * @param topics the kafka cluster topic list
     * @param topicInfoList topic list form db table
     * @param clusterInfo cluster information <br/>
     * @param  topicDescribe the partition,nodes of topic
     * @param  topicConfig topic`s config information <br/>
     * if the topic exits in topics and also exits in topicInfoList,
     *  update the topic config and update in db table
     *
     * */
    private Set<TopicInfo> needToUpdateDb(Set<String> topics,List<TopicInfo> topicInfoList,ClusterInfo clusterInfo,
                                          Map<String, TopicDescription> topicDescribe,Map<String, Config> topicConfig){
        String clusterId = clusterInfo.getId().toString();
        return topicInfoList.stream().filter(topicInfo -> topics.contains(topicInfo.getTopicName())).map(topicInfo -> {
            try{
                List<TopicPartitionInfo>  topicPartitionInfos = topicDescribe.get(topicInfo.getTopicName()).partitions();
                int partition = !CollectionUtils.isEmpty(topicPartitionInfos)?topicPartitionInfos.size():0;
                int replicas = !CollectionUtils.isEmpty(topicPartitionInfos)?(!topicPartitionInfos.get(0).replicas().isEmpty()?topicPartitionInfos.get(0).replicas().size():0):0;
                TopicInfo topicEntity = this.updateTTL(topicConfig,clusterId, topicInfo, topicInfo.getTopicName());
                topicEntity.setPartition(partition);
                topicEntity.setReplication((short)replicas);
                return topicEntity;
            }catch (Exception e){
               return new TopicInfo();
            }
        }).filter(topicInfo -> topicInfo.getTopicName()!=null).collect(Collectors.toSet());
    }

   /**set the topic ttl then return the topic
    * @param topicDescribe topic config map  in kafka cluster
    * @param clusterId clusterId requireNonNull
    * @param tInfo topicInfo entity
    * @param topicName topic name <br/>
    * if topicDescribe is null,that kafka cluster does not support viewing the topic config api,
    * then need to read the topic config from zookeeper file path
    * delete.retention.ms Time to delete the compressed data
    * retention.ms delete data according to the policy set by log.cleanup.policy
    * */
    private TopicInfo updateTTL(Map<String, Config> topicDescribe,String clusterId,TopicInfo tInfo,String topicName) throws Exception {
        try {
            if(!CollectionUtils.isEmpty(topicDescribe)) {
                Config con = topicDescribe.get(topicName);con.entries().forEach(entry->{
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
            throw new Exception("topic collect update has error: "+clusterId+Constants.Symbol.VERTICAL_STR +topicName,e);
        }
        return tInfo;
    }


    /**return the topic size map
     * @param clusterInfo requiredNonNull <br/>
     * topic log dir file size form kafka cluster
     *
     * */
    private Map<String, Long> takeTopicSize(ClusterInfo clusterInfo) throws Exception {
        try{
            String clusterId = clusterInfo.getId().toString();
            List<BrokerInfo> brokerInfos = zkService.getZK(clusterId).getBrokers();
            List<Integer> brokerIds = brokerInfos.stream().map(BrokerInfo::getBid).collect(Collectors.toList());
            Map<String, Long> mapSize =  kafkaAdminService.getKafkaAdmins(clusterId).getTopicDiskSizeForBroker(brokerIds);
            topicFileSizeWriteToEs(mapSize,clusterInfo);
            return mapSize;

        }catch (UnsupportedVersionException ignored){

        }catch (Exception e){
            throw new Exception("get topic file size has error",e);
        }
        return new HashMap<>(0);
    }

    /**
     * return the topic log dir size with topic
     * The kafka version is available below 1.x
     * take the topic of file size according to jmx
     * @param clusterInfo  cluster
     * @param  topics  topic list of kafka cluster
     * @param  topicDescribe take the topic describe according to topic
     * @return Map<String,Long> key:topicName,value:file size
     * * */
    public Map<String, Long> unSupportVersionTopicSize(ClusterInfo clusterInfo,Set<String> topics,Map<String, TopicDescription> topicDescribe) throws Exception {
        Map<String,Set<Integer>> sizeMap = new HashMap<>(2 << 2);
        topics.stream().filter(Objects::nonNull).forEach(topic->{
            TopicDescription topicDescription = topicDescribe.get(topic);
            Set<Integer> partitions = topicDescription.partitions().stream().map(TopicPartitionInfo::partition).collect(Collectors.toSet());
            sizeMap.put(topic,partitions);
        });
        try {
            String clusterId = clusterInfo.getId().toString();
            List<BrokerInfo> brokerInfos = zkService.getZK(clusterId).getBrokers();
            Map<String, Long> mapSize =  JmxCollector.getInstance().topicLogSizeByBroker(brokerInfos,sizeMap);
            topicFileSizeWriteToEs(mapSize,clusterInfo);
            return mapSize;
        }catch (Exception e){
            throw new Exception("get topic file size by jmx has error",e);
        }
    }





    /**
     * topic file size write to es
     * @param  fileSizeMap topic file size map by clusterId
     *
     * */
    private void topicFileSizeWriteToEs(Map<String, Long> fileSizeMap,ClusterInfo clusterInfo){
        SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<JSONObject> fileSizeList = new ArrayList<>();
        String index = elasticsearchService.getMonitorElasticsearchIndexName() + "-"
                + sFormat.format(new Date());
        Calendar calendar = TimeUtil.nowCalendar();
        fileSizeMap.keySet().forEach(key->{
            JSONObject object = new JSONObject();
            SimpleDateFormat sFormats = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            object.put("date", sFormats.format(new Date(calendar.getTimeInMillis())));
            object.put("timestamp", calendar.getTimeInMillis());
            object.put("topic",key);
            object.put("fileSize",fileSizeMap.get(key));
            object.put("clusterId",clusterInfo.getId());
            object.put("clusterName",clusterInfo.getName());
            object.put("type","fileSize");
            fileSizeList.add(object);
        });
        try {
            if (elasticsearchService.getESDB() != null) {
                elasticsearchService.getESDB().batchInsertES(fileSizeList, index);
            }
        }catch (Exception e){
            LOG.error("file size write to es has error,",e);
        }

    }



}
