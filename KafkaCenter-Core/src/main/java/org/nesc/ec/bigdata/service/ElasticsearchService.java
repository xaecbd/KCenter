package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.common.model.OffsetStat;
import org.nesc.ec.bigdata.common.util.ElasticSearchQuery;
import org.nesc.ec.bigdata.common.util.ElasticsearchUtil;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.TopicMetricVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Truman.P.Du
 * @version 1.0
 * @date 2019年4月17日 下午3:58:28
 */
@Service
public class ElasticsearchService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchService.class);

    @Autowired
    InitConfig initConfig;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TopicInfoService topicInfoService;

    private ElasticsearchUtil elasticsearchUtil;


    @PostConstruct
    public void init() {
        elasticsearchUtil = new ElasticsearchUtil(initConfig.getMonitorElasticsearchHost(), initConfig.getMonitorElasticsearchAuthUser(), initConfig.getMonitorElasticsearchAuthPassword());
    }

    public ElasticsearchUtil getESDB() {
        return elasticsearchUtil;
    }

    @PreDestroy
    public void close() {
        try {
            elasticsearchUtil.close();
        } catch (IOException e) {
            LOG.error("elasticsearchUtil close faild!", e);
        }
    }

    public String getMonitorElasticsearchIndexName() {
        return initConfig.getMonitorElasticsearchIndexName();
    }


    /**
     * 查询生成消费情况历史信息
     */
    public List<OffsetStat> queryDateIntervalOffset(String clusterId, String topic, String group, String type, String start, String end, String interval) {

        List<OffsetStat> list = new ArrayList<>();
        if (elasticsearchUtil == null) {
            return list;
        }
        try {
            if (StringUtils.isBlank(start)) {
                start = String.valueOf(System.currentTimeMillis() - (60 * 60 * 1000));
                end = String.valueOf(System.currentTimeMillis());
            }
            if (StringUtils.isBlank(interval)) {
                interval = Constants.Interval.FIVE_MINUTES;
            }
            long starts = Long.parseLong(start);
            long ends = Long.parseLong(end);
            if ((ends - starts) / (60 * 1000) > 24 * 60) {
                start = String.valueOf(Long.parseLong(start) - (24 * 60 * 60 * 1000));
            }
            String requestBody = ElasticSearchQuery.getDateIntervalQueryBody(clusterId, topic, group, type, start, end, interval);
            JSONObject responseObj = elasticsearchUtil.searchES(requestBody, getMonitorElasticsearchIndexName() + "*");
            if (responseObj != null) {
                list = parseDateIntervalResponse(responseObj);
                for (int i = list.size() - 1; i > 0; i--) {
                    list.get(i).setOffset(list.get(i).getOffset() - list.get(i - 1).getOffset());
                }
                if (!list.isEmpty()) {
                    list.remove(0);
                }
            }
        } catch (Exception e) {
            LOG.error("queryDateIntervalOffset faild!", e);
        }
        return list;
    }

    private List<OffsetStat> parseDateIntervalResponse(JSONObject responseObj) {
        List<OffsetStat> list = new ArrayList<>();
        responseObj.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.EleaticSearch.AGGS).
                getJSONArray(Constants.EleaticSearch.BUCKETS).forEach(obj -> {
                    OffsetStat offset = new OffsetStat();
                    JSONObject offsetJson = (JSONObject) obj;
                    offset.setTimestamp(offsetJson.getLong(Constants.JsonObject.KEY));
                    Long offsetValue = offsetJson.getJSONObject(TopicConfig.OFFSET).getLong(Constants.JsonObject.VALUE);
                    offset.setOffset(offsetValue);
                    if (offsetValue == null) {
                        offset.setOffset(list.get(list.size() - 1).getOffset());
                    }
                    Long lagValue = offsetJson.getJSONObject(TopicConfig.LAG).getLong(Constants.JsonObject.VALUE);
                    offset.setLag(lagValue);
                    if (lagValue == null) {
                        offset.setLag(list.get(list.size() - 1).getLag());
                    }
                    list.add(offset);
                });
        return list;
    }


    /**
     * 查询生成消费情况历史信息
     */
    public List<OffsetStat> queryOffset(String clusterId, String topic, String group, String type, String start, String end) {
        List<OffsetStat> list = new ArrayList<>();
        if (elasticsearchUtil == null) {
            return list;
        }
        try {
            if (StringUtils.isBlank(start)) {
                start = String.valueOf(System.currentTimeMillis() - (60 * 60 * 1000));
                end = String.valueOf(System.currentTimeMillis());
            }

            String requestBody = ElasticSearchQuery.getRequestBody(clusterId, topic, group, type, start, end);
            JSONObject responseObj = elasticsearchUtil.searchES(requestBody, getMonitorElasticsearchIndexName() + "*");
            if (responseObj != null) {
                list = parseResponse(responseObj);
            }
        } catch (IOException e) {
            LOG.error("queryOffset failed!", e);
        }
        return list;
    }

    private List<OffsetStat> parseResponse(JSONObject responseObj) {
        List<OffsetStat> list = new ArrayList<>();
        JSONArray array = responseObj.getJSONObject(Constants.EleaticSearch.HITS).getJSONArray(Constants.EleaticSearch.HITS);
        array.forEach(obj -> {
            JSONObject objs = (JSONObject) obj;
            OffsetStat offsetStat = objs.getObject(Constants.EleaticSearch.SOURCE_, OffsetStat.class);
            list.add(offsetStat);
        });
        return list;
    }


    List<OffsetStat> getRequestBody(String clientId) {
        List<OffsetStat> list = new ArrayList<>();
        if (elasticsearchUtil == null) {
            return list;
        }
        String searchQuery = ElasticSearchQuery.getLagQueryString(clientId);
        JSONObject responseObj;
        try {
            responseObj = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (responseObj != null) {
                JSONObject aggs = responseObj.getJSONObject(Constants.EleaticSearch.AGGREGATIONS);
                if (aggs == null) {
                    return list;
                }
                JSONArray json = aggs.getJSONObject(Constants.Number.TWO).getJSONArray(Constants.EleaticSearch.BUCKETS);
                json.forEach(obj -> {
                    JSONObject objs = (JSONObject) obj;
                    Long timeStamp = objs.getJSONObject("3").getLong(Constants.JsonObject.VALUE);
                    Long clusterId = objs.getLong(Constants.JsonObject.KEY);
                    int size = objs.getIntValue(Constants.EleaticSearch.DOC_COUNT);
                    int finalSize = Math.min(size, 10000);
                    List<OffsetStat> offsetStats = getCluster(finalSize, clusterId, timeStamp);
                    list.addAll(offsetStats);
                });
            }
        } catch (IOException e) {
            LOG.error("getRequestBody failed!", e);
        }
        return list;
    }

    public List<OffsetStat> getCluster(int size, Long clusterId, Long timeStamp) {
        List<OffsetStat> offsetStats = new ArrayList<>();
        if (elasticsearchUtil == null) {
            return offsetStats;
        }
        String searchQuery = ElasticSearchQuery.getClusterQueryString(size, clusterId, timeStamp);
        JSONObject responseObj;
        try {
            responseObj = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (responseObj != null) {
                offsetStats = parseResponse(responseObj);
                if (!offsetStats.isEmpty()) {
                    offsetStats.removeIf(offsetStat -> offsetStat.getTimestamp().longValue() != timeStamp);
                }
            }
        } catch (IOException e) {
            LOG.error("getCluster failed!", e);
        }
        return offsetStats;
    }


    private Map<String, JSONArray> clusterTrendAggData(long start, long end, long clientId, String interval) {
        Map<String, JSONArray> map = new HashMap<>();
        if (elasticsearchUtil == null) {
            return map;
        }
        String searchQuery = ElasticSearchQuery.clusterTrendAggres(start, end, clientId, interval);
        try {
            JSONObject temp = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (!temp.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return map;
            }
            JSONArray dataArr = temp.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATAGRAME).getJSONArray(Constants.EleaticSearch.BUCKETS);
            JSONArray res = new JSONArray();
            dataArr.forEach(data -> {
                JSONObject obj = (JSONObject) data;
                long time = obj.getLongValue(Constants.JsonObject.KEY);
                JSONArray metricArr = obj.getJSONObject(Constants.KeyStr.METRIC).getJSONArray(Constants.EleaticSearch.BUCKETS);
                metricArr.forEach(metric -> {
                    JSONObject metricObj = (JSONObject) metric;
                    String metricName = metricObj.getString(Constants.JsonObject.KEY);
                    JSONArray brokerArr = metricObj.getJSONObject(BrokerConfig.BROKER).getJSONArray(Constants.EleaticSearch.BUCKETS);
                    brokerArr.forEach(broker -> {
                        JSONObject brokerObj = (JSONObject) broker;
                        String brok = brokerObj.getString(Constants.JsonObject.KEY);
                        long value = brokerObj.getJSONObject(Constants.KeyStr.AVG_RATE).getLongValue(Constants.JsonObject.VALUE);
                        JSONObject json = new JSONObject();
                        json.put(Constants.KeyStr.TIMESTAMP, time);
                        json.put(BrokerConfig.METRICNAME, metricName);
                        json.put(BrokerConfig.ONE_MINUTE_RATE, value);
                        json.put(BrokerConfig.BROKER, brok);
                        res.add(json);
                    });
                });
            });
            String[] metricNames = BrokerConfig.METRIC_NAME_ARRAY;
            for (String metric : metricNames) {
                JSONArray arr = new JSONArray();
                res.forEach(obj -> {
                    JSONObject json = (JSONObject) obj;
                    if (metric.equalsIgnoreCase(json.getString(BrokerConfig.METRICNAME))) {
                        arr.add(json);
                    }
                });
                map.put(metric, arr);
            }
        } catch (IOException e) {
            LOG.error("get cluster trend chart error", e);
        }
        return map;
    }

    private Map<String, JSONArray> clusterTrendNoAgg(long start, long end, long clientId) throws Exception {
        Map<String, JSONArray> map = new HashMap<>();
        if (elasticsearchUtil == null) {
            return map;
        }
        String searchQuery = ElasticSearchQuery.clusterQuery(start, end, clientId);
        try {
            JSONObject temp = elasticsearchUtil.scrollSearch(searchQuery, getMonitorElasticsearchIndexName() + Constants.Symbol.STARSTR);
            if (!temp.containsKey(Constants.EleaticSearch.HITS)) {
                return map;
            }
            if (!temp.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return map;
            }
            JSONArray hits = temp.getJSONArray(Constants.EleaticSearch.HITS);
            hits.forEach(obj -> {
                JSONArray array = (JSONArray) obj;
                array.forEach(arr -> {
                    JSONObject hit = (JSONObject) arr;
                    JSONObject source = hit.getJSONObject(Constants.EleaticSearch.SOURCE_);
                    if (source.containsKey(Constants.ConsumerType.BROKER)) {
                        String metrcName = source.getString(BrokerConfig.METRICNAME);
                        JSONArray arrRes;
                        if (map.containsKey(metrcName)) {
                            arrRes = map.get(metrcName);
                        } else {
                            arrRes = new JSONArray();
                        }
                        arrRes.add(source);
                        map.put(metrcName, arrRes);
                    }

                });

            });

        } catch (IOException e) {
            LOG.error("get cluster data error", e);
        }


        return map;
    }

    Map<String, JSONArray> clusterTrendData(long start, long end, long clientId) throws Exception {
        long time = (end - start) / (1000 * 60);
        if (time > 24 * 60) {
            String interval = this.getInterval((int) time);
            return this.clusterTrendAggData(start, end, clientId, interval);
        } else {
            return this.clusterTrendNoAgg(start, end, clientId);
        }
    }


    Map<String, JSONArray> summaryMetricTrend(String searchQuery, long start, long end) {
        Set<JSONObject> objs = new java.util.HashSet<>();
        Map<String, JSONArray> res = new HashMap<>();
        if (elasticsearchUtil == null) {
            return res;
        }
        Map<String, ClusterInfo> clusterMap = new HashMap<>();
        List<ClusterInfo> clusters = clusterService.getTotalData();
        clusters.forEach(cluster -> clusterMap.put(cluster.getId().toString(), cluster));
        String[] metricNames = BrokerConfig.METRIC_NAME_ARRAY;
        try {
            JSONObject temp = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + Constants.Symbol.STARSTR);
            if (!temp.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return res;
            }
            JSONObject tests = temp.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATAGRAME);
            JSONArray metrics = tests.getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (Object obj : metrics) {
                JSONObject item = (JSONObject) obj;
                long time = item.getLongValue(Constants.JsonObject.KEY);
                JSONArray clusterArr = item.getJSONObject(Constants.KeyStr.CLUSTER_ID).getJSONArray(Constants.EleaticSearch.BUCKETS);
                for (Object metricObj : clusterArr) {
                    JSONObject objes = (JSONObject) metricObj;
                    String clusterId = objes.getString(Constants.JsonObject.KEY);
                    JSONArray broker = objes.getJSONObject(BrokerConfig.METRIC_NAME).getJSONArray(Constants.EleaticSearch.BUCKETS);
                    broker.forEach(bus -> {
                        JSONObject bucket = (JSONObject) bus;
                        String metricName = bucket.getString(Constants.JsonObject.KEY);
                        JSONArray buckets = bucket.getJSONObject(BrokerConfig.BROKER).getJSONArray(Constants.EleaticSearch.BUCKETS);
                        long sumValue = 0L;
                        for (Object xxx : buckets) {
                            JSONObject xxxObj = (JSONObject) xxx;
                            long minData = xxxObj.getJSONObject(Constants.KeyStr.MIN_DATA).getLongValue(Constants.JsonObject.VALUE);
                            long maxData = xxxObj.getJSONObject(Constants.KeyStr.MAX_DATA).getLongValue(Constants.JsonObject.VALUE);
                            sumValue = sumValue + (maxData - minData);
                        }
                        if (clusterMap.containsKey(clusterId)) {
                            JSONObject json = new JSONObject();
                            json.put(Constants.KeyStr.TIME, time);
                            json.put(Constants.JsonObject.NAME, clusterMap.get(clusterId).getName());
                            json.put(Constants.JsonObject.VALUE, sumValue);
                            json.put(BrokerConfig.METRICNAME, metricName);
                            objs.add(json);
                        }
                    });
                }

            }
            for (String name : metricNames) {
                JSONArray array = new JSONArray();
                for (JSONObject obj : objs) {
                    String metricName = obj.getString(BrokerConfig.METRICNAME);
                    if (name.equalsIgnoreCase(metricName)) {
                        array.add(obj);
                    }
                }
                res.put(name, array);
            }
        } catch (Exception e) {
            LOG.error("get summary trend chart error", e);
        }
        return res;
    }


    Map<String, Long> summaryMetric(long start, long end) {

        Map<String, Long> map = new HashMap<>();
        if (elasticsearchUtil == null) {
            return map;
        }
        String searchQuery = ElasticSearchQuery.summaryMetricQuery(start, end);
        try {
            JSONObject temp = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (!temp.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return map;
            }
            JSONArray buckets = temp.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(BrokerConfig.METRIC_NAME).getJSONArray(Constants.EleaticSearch.BUCKETS);
            buckets.forEach(obj -> {
                JSONObject clusters = (JSONObject) obj;
                String metricName = clusters.getString(Constants.JsonObject.KEY);
                JSONArray bucketArr = clusters.getJSONObject(BrokerConfig.BROKER).getJSONArray(Constants.EleaticSearch.BUCKETS);
                long value = 0L;
                for (Object object : bucketArr) {
                    JSONObject jsonObject = (JSONObject) object;
                    value = value + jsonObject.getJSONObject(Constants.KeyStr.DIFF_DATA).getLongValue(Constants.JsonObject.VALUE);
                }
                if (map.containsKey(metricName)) {
                    long val = map.get(metricName);
                    map.put(metricName, val + value);
                } else {
                    map.put(metricName, value);
                }
            });
        } catch (IOException e) {
            LOG.error("get summary data error", e);
        }
        return map;
    }

    public List<JSONObject> getTopicMetric(long start, long end, String clusterId, String topic, String metric) {
        List<JSONObject> resultList = new ArrayList<>();
        String searchQuery = ElasticSearchQuery.searchTopicMetricQuery(start, end, clusterId, topic, metric);
        try {
            JSONObject result = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (!result.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return resultList;
            }
            JSONArray bucketArray = result.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATE).getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (int i = 0; i < bucketArray.size() - 1; i++) {
                JSONObject obj = bucketArray.getJSONObject(i);
                JSONObject nextObj = bucketArray.getJSONObject(i + 1);
                String key = obj.getString(Constants.EleaticSearch.KEY_AS_STRING);
                String nextKey = nextObj.getString(Constants.EleaticSearch.KEY_AS_STRING);
                JSONObject metricObj = obj.getJSONObject(Constants.KeyStr.METRIC);
                JSONObject nextMetricObj = nextObj.getJSONObject(Constants.KeyStr.METRIC);
                long metricValue = metricObj.getLongValue(Constants.JsonObject.VALUE);
                long nextMetricValue = nextMetricObj.getLongValue(Constants.JsonObject.VALUE);
                JSONObject resultObj = new JSONObject();
                resultObj.put(Constants.JsonObject.TIME, nextKey);
                resultObj.put(Constants.JsonObject.VALUE, (nextMetricValue - metricValue));
                resultList.add(resultObj);
            }
        } catch (Exception e) {
            LOG.error("get topic metric data error", e);
        }
        return resultList;
    }


    /***
     *
     * search the topic max log size group by topic,
     * Admin user queries all topics,
     * while ordinary user queries all topics belonging to the team
     * @param  userInfo the current login user information
     *
     */
    public List<JSONObject> top10TopicLogSizeRang7Days(UserInfo userInfo, long start, long end) {
        List<JSONObject> objectList = new ArrayList<>();
        String searchQuery = ElasticSearchQuery.top10TopicLogSizeRang7Days(searchQuery(userInfo), start, end);
        try {
            JSONObject result = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (!result.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return objectList;
            }
            Map<String, Long> valueMap = new HashMap<>();
            JSONArray bucketArray = result.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATE).getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (int i = 0; i < bucketArray.size(); i++) {
                JSONObject bucketObj = bucketArray.getJSONObject(i);
                long key = bucketObj.getLongValue(Constants.EleaticSearch.KEY);
                JSONArray clusterBuckets = bucketObj.getJSONObject("cluster").getJSONArray(Constants.EleaticSearch.BUCKETS);

                for (int k = 0; k < clusterBuckets.size(); k++) {
                    JSONObject clusterObj = clusterBuckets.getJSONObject(k);
                    String clusterName = clusterObj.getString(Constants.EleaticSearch.KEY);
                    JSONArray topicBucket = clusterObj.getJSONObject("topic").getJSONArray(Constants.EleaticSearch.BUCKETS);
                    for (int j = 0; j < topicBucket.size(); j++) {
                        JSONObject topicObj = topicBucket.getJSONObject(j);
                        String topic = topicObj.getString(Constants.EleaticSearch.KEY);
                        long value = topicObj.getJSONObject("logSize").getLongValue(Constants.EleaticSearch.VALUE);
                        String mapKey = clusterName + Constants.Symbol.SPACE_STR + topic;
                        valueMap.put(mapKey, value);
                    }
                }
                List<JSONObject> mergeList = generateMapToArray(valueMap, key);
                if (!CollectionUtils.isEmpty(mergeList)) {
                    objectList.addAll(mergeList);
                }

            }
        } catch (ConnectException ce) {
            LOG.warn("search top 10 topic file Size Rang 7 Days has Error,for timeout connecting to es");
        } catch (Exception e) {
            LOG.error("search top 10 topic Log Size Rang 7 Days has Error", e);
        }
        return objectList;
    }

    /**
     * search the topic max file size group by topic,
     * Admin user queries all topics,
     * while ordinary user queries all topics belonging to the team
     *
     * @param userInfo the current login user information
     */
    public List<JSONObject> top10TopicFileSizeRang7Days(UserInfo userInfo, long start, long end) {
        List<JSONObject> objectList = new ArrayList<>();
        String query = searchQuery(userInfo);
        if (Objects.equals("", query)) {
            return objectList;
        }
        String searchQuery = ElasticSearchQuery.top10TopicFileSizeRang7Days(query, start, end);
        try {
            JSONObject result = elasticsearchUtil.searchES(searchQuery, getMonitorElasticsearchIndexName() + "*");
            if (!result.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return objectList;
            }
            JSONArray bucketArray = result.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATE).getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (int i = 0; i < bucketArray.size(); i++) {
                JSONObject bucketObj = bucketArray.getJSONObject(i);
                long time = bucketObj.getLongValue(Constants.EleaticSearch.KEY);
                JSONArray clusterBuckets = bucketObj.getJSONObject(Constants.KeyStr.CLUSTER).getJSONArray(Constants.EleaticSearch.BUCKETS);
                Map<String, Long> valueMap = new HashMap<>();
                for (int k = 0; k < clusterBuckets.size(); k++) {
                    JSONObject clusterObj = clusterBuckets.getJSONObject(k);
                    String clusterName = clusterObj.getString(Constants.EleaticSearch.KEY);
                    JSONArray topicBucket = clusterObj.getJSONObject(Constants.KeyStr.TOPIC).getJSONArray(Constants.EleaticSearch.BUCKETS);
                    for (int j = 0; j < topicBucket.size(); j++) {
                        JSONObject topicObj = topicBucket.getJSONObject(j);
                        String topic = topicObj.getString(Constants.EleaticSearch.KEY);
                        long value = topicObj.getJSONObject(Constants.KeyStr.MAX_FILE).getLongValue(Constants.EleaticSearch.VALUE);
                        String key = clusterName + Constants.Symbol.SPACE_STR + topic;
                        valueMap.put(key, value);
                    }
                }
                List<JSONObject> mergeList = generateMapToArray(valueMap, time);
                if (!CollectionUtils.isEmpty(mergeList)) {
                    objectList.addAll(mergeList);
                }
            }
        } catch (ConnectException ce) {
            LOG.warn("search top 10 topic file Size Rang 7 Days has Error,for timeout connecting to es");
        } catch (Exception e) {
            LOG.error("search top 10 topic file Size Rang 7 Days has Error", e);
        }
        return objectList;

    }

    /**
     * search topic ByteInPer AND ByteOutPer metric value
     * because metric count value is increasing,we will calculate the
     * difference value between the max and min count value.then the value that we really want
     */
    public Set<TopicMetricVo> searchTopicIO(String time) {
        Set<TopicMetricVo> metricVoSet = new HashSet<>();
        String query = ElasticSearchQuery.searchTopicInOrOutQuery(time, time);
        try {
            JSONObject response = elasticsearchUtil.searchES(query, initConfig.getMonitorElasticsearchIndexName() + "*");
            if (!response.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return metricVoSet;
            }
            JSONArray dateBucketsArray = response.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATE).getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (int i = 0; i < dateBucketsArray.size(); i++) {
                JSONObject dateObj = dateBucketsArray.getJSONObject(i);
                String date = dateObj.getString(Constants.EleaticSearch.KEY_AS_STRING);
                JSONArray clusterBucketsArray = dateObj.getJSONObject(Constants.ConsumerType.BROKER).getJSONArray(Constants.EleaticSearch.BUCKETS);
                for (int j = 0; j < clusterBucketsArray.size(); j++) {
                    JSONObject clusterObj = clusterBucketsArray.getJSONObject(j);
                    String clusterId = clusterObj.getString(Constants.EleaticSearch.KEY);
                    JSONArray topicBucketsArray = clusterObj.getJSONObject(Constants.KeyStr.TOPIC).getJSONArray(Constants.EleaticSearch.BUCKETS);
                    for (int k = 0; k < topicBucketsArray.size(); k++) {
                        JSONObject topicObj = topicBucketsArray.getJSONObject(k);
                        String topic = topicObj.getString(Constants.EleaticSearch.KEY);
                        JSONArray metricBucketsArray = topicObj.getJSONObject(Constants.KeyStr.METRIC).getJSONArray(Constants.EleaticSearch.BUCKETS);
                        long byteInMetric = 0L, byteOutMetric = 0L;
                        for (int g = 0; g < metricBucketsArray.size(); g++) {
                            JSONObject countObj = metricBucketsArray.getJSONObject(g);
                            String metric = countObj.getString(Constants.EleaticSearch.KEY);
                            long value = countObj.getJSONObject(Constants.KeyStr.DIFF_DATA).getLongValue(Constants.EleaticSearch.VALUE);
                            if (Objects.equals(metric, BrokerConfig.BYTES_IN_PER_SEC)) {
                                byteInMetric = value;
                            } else {
                                byteOutMetric = value;
                            }
                        }
                        TopicMetricVo topicMetricVo = new TopicMetricVo(Long.parseLong(clusterId), date, topic, byteInMetric, byteOutMetric);
                        metricVoSet.add(topicMetricVo);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("search Topic IO has error", e);
        }
        return metricVoSet;

    }

    /**
     * search topic fileSize for cluster.
     * Take the maximum and minimum fileSize values, and the difference between them is disk,
     * which is used today
     * the value including replication
     */
    public Set<TopicMetricVo> searchTopicFileSize(String time) {
        String query = ElasticSearchQuery.searchFileSizeGroupByTopic(time, time);
        Set<TopicMetricVo> topicMetricVoSet = new HashSet<>();
        try {
            JSONObject response = elasticsearchUtil.searchES(query, initConfig.getMonitorElasticsearchIndexName() + "*");
            if (!response.containsKey(Constants.EleaticSearch.AGGREGATIONS)) {
                return topicMetricVoSet;
            }
            JSONArray dateBuckets = response.getJSONObject(Constants.EleaticSearch.AGGREGATIONS).getJSONObject(Constants.KeyStr.DATE).getJSONArray(Constants.EleaticSearch.BUCKETS);
            for (int i = 0; i < dateBuckets.size(); i++) {
                JSONObject clusterObj = dateBuckets.getJSONObject(i);
                String date = clusterObj.getString(Constants.EleaticSearch.KEY_AS_STRING);
                JSONArray clusterBucketsArray = clusterObj.getJSONObject(Constants.KeyStr.LOWER_CLUSTER_ID).getJSONArray(Constants.EleaticSearch.BUCKETS);
                for (int j = 0; j < clusterBucketsArray.size(); j++) {
                    JSONObject topicObj = clusterBucketsArray.getJSONObject(j);
                    String clusterId = topicObj.getString(Constants.EleaticSearch.KEY);
                    JSONArray topicBucketArray = topicObj.getJSONObject(Constants.KeyStr.TOPIC).getJSONArray(Constants.EleaticSearch.BUCKETS);
                    for (int k = 0; k < topicBucketArray.size(); k++) {
                        JSONObject topicBucketObj = topicBucketArray.getJSONObject(k);
                        String topic = topicBucketObj.getString(Constants.EleaticSearch.KEY);
                        long value = topicBucketObj.getJSONObject(Constants.KeyStr.DIFF_DATA).getLongValue(Constants.EleaticSearch.VALUE);
                        TopicMetricVo topicMetricVo = new TopicMetricVo(Long.parseLong(clusterId), date, topic, value);
                        topicMetricVoSet.add(topicMetricVo);
                    }

                }
            }
        } catch (Exception e) {
            LOG.error("search topic fileSize has error", e);
        }
        return topicMetricVoSet;

    }

    private List<JSONObject> generateMapToArray(Map<String, Long> valueMap, long time) {
        List<JSONObject> list = new ArrayList<>();
        Set<String> keySet = valueMap.keySet();
        LinkedHashSet<String> keyLinkedSet = keySet.stream().sorted((o1, o2) -> {
            if (valueMap.get(o2) - valueMap.get(o1) > 0) {
                return 1;
            } else if (valueMap.get(o2) - valueMap.get(o1) == 0) {
                return 0;
            } else {
                return -1;
            }
        }).collect(Collectors.toCollection(LinkedHashSet::new));
        boolean flag = keyLinkedSet.size() > 10;
        if (flag) {
            keyLinkedSet = keyLinkedSet.stream().limit(10).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        keyLinkedSet.forEach((key) -> {
            JSONObject object = new JSONObject();
            object.put(Constants.KeyStr.DATE, time);
            object.put("topic", key);
            object.put(Constants.EleaticSearch.VALUE, valueMap.get(key));
            list.add(object);

        });
        list.sort((o1, o2) -> (int) (o2.getLongValue(Constants.EleaticSearch.VALUE) - o1.getLongValue(Constants.EleaticSearch.VALUE)));
        return list;
    }

    /**
     * Return topic by role
     * if user is admin,return null
     * f the user is a regular user,returns the topic owned by the user's team
     */
    private String searchQuery(UserInfo userInfo) {
        if (Objects.equals(userInfo.getRole().name(), RoleEnum.ADMIN.name())) {
            return null;
        } else {
            List<TopicInfo> topicInfoList = topicInfoService.getTopicByTeamIDs(userInfo.getTeamIDs());
            StringBuilder sb = new StringBuilder();
            if (!CollectionUtils.isEmpty(topicInfoList)) {
                topicInfoList.forEach(topicInfo -> {
                    sb.append("\"").append(topicInfo.getTopicName()).append("\"").append(",");
                });
                String str = sb.toString();
                return str.substring(0, str.length() - 1);
            }
            return sb.toString();

        }
    }

    private String getInterval(int diff) {
        if (diff / Constants.Time.FIVE < Constants.Time.HUNDRED) {
            return Constants.Interval.FIVE_MINUTES;
        }
        if (diff / Constants.Time.TEN < Constants.Time.HUNDRED) {
            return Constants.Interval.TEN_MINUTES;
        }
        if (diff / Constants.Time.THIRTY < Constants.Time.HUNDRED) {
            return Constants.Interval.THREETY_MINUTES;
        }
        if (diff / Constants.Time.SIXTY < Constants.Time.HUNDRED) {
            return Constants.Interval.ONE_HOURS;
        }
        if (diff / (Constants.Time.FOUR * Constants.Time.SIXTY) < Constants.Time.HUNDRED) {
            return Constants.Interval.FOUR_HOURS;
        }
        if (diff / (Constants.Time.EIGHT * Constants.Time.SIXTY) < Constants.Time.HUNDRED) {
            return Constants.Interval.EIGHT_HOURS;
        }
        if (diff / (Constants.Time.SIXTEEN * Constants.Time.SIXTY) < Constants.Time.HUNDRED) {
            return Constants.Interval.FORTHY_HOURS;
        }
        return Constants.Interval.ONE_DAY;
    }

}
