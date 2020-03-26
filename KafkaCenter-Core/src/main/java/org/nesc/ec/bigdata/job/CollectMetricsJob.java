package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.ElasticsearchService;
import org.nesc.ec.bigdata.service.HomeService;
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
    ElasticsearchService elasticsearchService;

    void collectMetric(){
        try {
            long now = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(now);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
            List<ClusterInfo> clusters = clusterService.getTotalData();
            List<JSONObject> result = new ArrayList<>();
            for (ClusterInfo cluster:clusters){
                if (initConfig.isMonitorCollectorIncludeEnable() && !cluster.getLocation()
                        .equalsIgnoreCase(initConfig.getMonitorCollectorIncludelocation())) {
                    return;
                }
                Set<MeterMetric> metricSet = homeService.brokerMetric(cluster);
                if (!metricSet.isEmpty()) {
                    metricSet.forEach(meterMetric -> {
                        JSONObject object = JSONObject.parseObject(JSONObject.toJSONString(meterMetric));
                        SimpleDateFormat sFormats = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        object.put("date", sFormats.format(new Date(cal.getTimeInMillis())));
                        object.put("timestamp", cal.getTimeInMillis());
                        result.add(object);
                    });
                }
            }
            String index = elasticsearchService.getMonitorElasticsearchIndexName() + "-" + sFormat.format(new Date());
            if(elasticsearchService.getESDB()!=null){
                elasticsearchService.getESDB().batchInsertES(result, index);
                LOG.debug("collect metric success!");
            }
        } catch (Exception e) {
            LOG.error("monitor metric  error.", e);
        }

    }

}
