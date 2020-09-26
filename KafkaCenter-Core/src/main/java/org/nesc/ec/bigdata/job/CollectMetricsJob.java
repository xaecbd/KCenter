package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.nesc.ec.bigdata.common.util.TimeUtil;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lg99
 */
@Component
public class CollectMetricsJob {

    private static final Logger LOG = LoggerFactory.getLogger(CollectMetricsJob.class);

    @Autowired
    ClusterService clusterService;

    @Autowired
    InitConfig initConfig;

    @Autowired
    HomeService homeService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    KafkaAdminService kafkaAdminService;

    @Autowired
    ElasticsearchService elasticsearchService;


    void collectMetric(){
        try{
            collectionTopicOrBrokerMetric(true);
            collectionTopicOrBrokerMetric(false);
        }catch (Exception e){
            LOG.error("collect  metric  error.", e);
        }
    }

    void collectionTopicOrBrokerMetric(boolean isTopic) throws Exception {
        try{
            Calendar calendar = TimeUtil.nowCalendar();
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<ClusterInfo> clusters = clusterService.getTotalData();
            List<JSONObject> result = new ArrayList<>();
            for (ClusterInfo cluster:clusters){
                if (initConfig.isMonitorCollectorIncludeEnable() && !cluster.getLocation()
                        .equalsIgnoreCase(initConfig.getMonitorCollectorIncludelocation())) {
                    continue;
                }
                List<JSONObject> resultList = isTopic?collectionTopicMetric(cluster,calendar):collectBrokerMetric(cluster,calendar);
                result.addAll(resultList);
            }
            String index = elasticsearchService.getMonitorElasticsearchIndexName() + "-" + sFormat.format(new Date());
            if(elasticsearchService.getESDB()!=null){
                elasticsearchService.getESDB().batchInsertES(result, index);
                LOG.debug("collect metric success!");
            }
        }catch (Exception e){
            String msg = isTopic?"collect topic metric  error":"collect group metric  error";
            throw new Exception(msg,e);

        }
    }


    private List<JSONObject> collectionTopicMetric(ClusterInfo cluster,Calendar calendar) throws Exception {
        List<JSONObject> result = new ArrayList<>();
        try{
            Set<String> topicList =  kafkaAdminService.getKafkaAdmins(cluster.getId().toString()).listTopics();
            topicList.forEach(topic->{
                Set<MeterMetric> metricSet = monitorService.getBrokerMetric(cluster.getId().toString(),topic);
                result.addAll(parseToResultData(metricSet,calendar,topic));
            });
        }catch (Exception e){
            throw new Exception("collect topic metric  error.",e);
        }
        return result;

    }

    private List<JSONObject> collectBrokerMetric(ClusterInfo cluster,Calendar calendar) throws Exception {
        List<JSONObject> result = new ArrayList<>();
        try{
            Set<MeterMetric> metricSet = homeService.brokerMetric(cluster);
            result = parseToResultData(metricSet,calendar,"");

        }catch (Exception e){
            throw new Exception("collect broker metric  error.",e);
        }
        return result;
    }

    private List<JSONObject> parseToResultData(Set<MeterMetric> metricSet,Calendar calendar,String topic){
        List<JSONObject> result = new ArrayList<>();
        if (!metricSet.isEmpty()) {
            metricSet.forEach(meterMetric -> {
                boolean b = !Objects.isNull(topic) && !Objects.equals("", topic);
                if(b){
                    meterMetric.setTopic(topic);
                }
                JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(meterMetric));
                SimpleDateFormat sFormats = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                object.put("date", sFormats.format(new Date(calendar.getTimeInMillis())));
                object.put("type", b ?"topic":"broker");
                object.put("timestamp", calendar.getTimeInMillis());

                result.add(object);
            });
        }
        return result;
    }




}
