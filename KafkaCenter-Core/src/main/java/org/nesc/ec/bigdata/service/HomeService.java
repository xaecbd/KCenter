package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.nesc.ec.bigdata.common.util.ElasticSearchQuery;
import org.nesc.ec.bigdata.common.util.JmxCollector;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class HomeService {

    private final  Logger LOG = LoggerFactory.getLogger(HomeService.class);

    @Autowired
    ClusterService clusterService;

    @Autowired
    KafkaAdminService kafkaAdminService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    AlertService alertService;

    @Autowired
    ElasticsearchService elasticsearchService;

    @Autowired
    ZKService zkService;
    public List<JSONObject> clusterStatistical() {
        List<ClusterInfo> clusters = clusterService.getTotalData();
        List<JSONObject> result = new ArrayList<>();
        for (ClusterInfo cluster:clusters){
            JSONObject json = new JSONObject();
            json.put(BrokerConfig.VERSION, cluster.getKafkaVersion());
            List<String> list = new ArrayList<>();
            json.put(Constants.JsonObject.NAME, cluster.getName());
            json.put(Constants.JsonObject.ID, cluster.getId());
            try {
                DescribeClusterResult describeClusterResult = kafkaAdminService
                        .getKafkaAdmins(String.valueOf(cluster.getId())).descCluster();
                Node controal = describeClusterResult.controller().get(5, TimeUnit.SECONDS);
                json.put(BrokerConfig.CONTROLLER, controal.host());
                describeClusterResult.nodes().get().forEach(node -> list.add(node.host()));
                json.put(BrokerConfig.NODES, list);
                int brokers = describeClusterResult.nodes().get().size();
                json.put(BrokerConfig.BROKER, brokers);
                if(brokers==0) {json.put(Constants.Status.STATUS, Constants.Status.BAD);}
                if(cluster.isEnable()) {
                    if(cluster.getBrokerSize()>brokers) {
                        json.put(Constants.Status.STATUS, Constants.Status.WARN);
                    }else if(cluster.getBrokerSize()==brokers) {
                        json.put(Constants.Status.STATUS, Constants.Status.OK);
                    }
                }else {
                    json.put(Constants.Status.STATUS, Constants.Status.OK);
                }
            } catch (Exception e) {
                continue;
            }
            result.add(json);
        }
        return result;
    }


    public HomeCache.HomePageCache clusterInfo() {
        HomeCache.HomePageCache pageCache = HomeCache.getConfigCache();
        try {
            List<ClusterInfo> clusterInfoList = clusterService.getTotalData();
            pageCache.setAlertSize(pageCache.getAlertSize()==0?alertService.countData():pageCache.getAlertSize());
            pageCache.setClusterSize(pageCache.getClusterSize()==0?clusterInfoList.size():pageCache.getClusterSize());
            pageCache.setTopicSize(pageCache.getTopicSize()==0?this.getTopicList(clusterInfoList):pageCache.getTopicSize());
            pageCache.setGroupSize(pageCache.getGroupSize()==0?calcGroup(clusterInfoList):pageCache.getGroupSize());
        } catch (Exception e2) {
            LOG.error("Get Cluster Date Faild!,{}",e2.getMessage());
        }
        return pageCache;
    }

    private int calcGroup(List<ClusterInfo> clusterInfos){
        int group = 0;
        int zk = 0;
        try{
            for(ClusterInfo cluser:clusterInfos) {
                group += kafkaAdminService.getKafkaAdmins(cluser.getId().toString()).listConsumerGroups().size();
                zk += zkService.getZK(cluser.getId().toString()).listConsumerGroups().size();
            }
        }catch (Exception e){
            LOG.error("calc all cluster group faild!,",e);
        }
        return (zk+group);
    }

    public int getTopicList(List<ClusterInfo> clusters) {
        int count = 0;
        try {
            for(ClusterInfo cluster:clusters) {
                Set<String> topicMap = kafkaAdminService.getKafkaAdmins(cluster.getId().toString()).listTopics();
                count += topicMap.size();
            }
        } catch (Exception e) {
            LOG.error("Get Topics Date Faild!,",e);
        }
        return count;

    }

    public Set<MeterMetric> brokerMetric(ClusterInfo clusterInfo) throws Exception {
        Set<MeterMetric> metricSet = new HashSet<>();
        List<BrokerInfo> brokers = zkService.getZK(clusterInfo.getId().toString()).getBrokers();
        Map<String,Set<MeterMetric>> metricBrokers = JmxCollector.getInstance().metricEveryBroker(brokers);
        metricBrokers.forEach((host,metricCol)->{
            metricCol.forEach(meterMetric -> {
                meterMetric.setClusterID(clusterInfo.getId().toString());
                meterMetric.setClusterName(clusterInfo.getName());
                meterMetric.setLocation(clusterInfo.getLocation());
                meterMetric.setBroker(host);
                metricSet.add(meterMetric);
            });
        });
        return metricSet;
    }

    public Map<String,JSONArray>  trendClusterData(long start,long end,long clientId) {
        Map<String, JSONArray> map = null;
        try {
            map = elasticsearchService.clusterTrendData(start, end, clientId);
        } catch (Exception e) {
            LOG.error("Get trend Cluster data Faild!,",e);
        }
        return map;
    }
    public Map<String, JSONArray> summatTrend(long start,long end,String interval){
        String searchQuery = ElasticSearchQuery.summaryMetricTrendQuery(interval, start, end);
        return elasticsearchService.summaryMetricTrend(searchQuery,start,end);
    }
    public JSONArray summaryData(long start,long end){
        Map<String,Long> map = elasticsearchService.summaryMetric(start, end);
        JSONArray array = new JSONArray();
        map.forEach((k,v)->{
            JSONObject obj = new JSONObject();
            obj.put(BrokerConfig.METRICNAME, k);
            obj.put(Constants.JsonObject.VALUE, v);
            array.add(obj);
        });
        return array;
    }

    static class MetricVo {
        private String broker;
        private String port;
        private String jmxPort;
        private String byteIn;
        private String byteOut;
        private String messageIn;
        private Double byteInOneMin;
        private Double byteOutOneMin;
        private String MsgInOneMin;

        public String getBroker() {
            return broker;
        }

        public void setBroker(String broker) {
            this.broker = broker;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getJmxPort() {
            return jmxPort;
        }

        void setJmxPort(String jmxPort) {
            this.jmxPort = jmxPort;
        }

        public String getByteIn() {
            return byteIn;
        }

        void setByteIn(String byteIn) {
            this.byteIn = byteIn;
        }

        public String getByteOut() {
            return byteOut;
        }

        void setByteOut(String byteOut) {
            this.byteOut = byteOut;
        }

        public String getMessageIn() {
            return messageIn;
        }

        void setMessageIn(String messageIn) {
            this.messageIn = messageIn;
        }

        public Double getByteInOneMin() {
            return byteInOneMin;
        }

        void setByteInOneMin(Double byteInOneMin) {
            this.byteInOneMin = byteInOneMin;
        }

        public Double getByteOutOneMin() {
            return byteOutOneMin;
        }

        void setByteOutOneMin(Double byteOutOneMin) {
            this.byteOutOneMin = byteOutOneMin;
        }

        public String getMsgInOneMin() {
            return MsgInOneMin;
        }

        void setMsgInOneMin(String msgInOneMin) {
            MsgInOneMin = msgInOneMin;
        }

    }


}
