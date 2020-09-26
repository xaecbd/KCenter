package org.nesc.ec.bigdata.job;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author Truman.P.Du
 * @date 2020/08/06
 * @description
 */
@Component
public class InitRunJob {
    private static final Logger LOG = LoggerFactory.getLogger(InitRunJob.class);

    @Autowired
    ClusterService clusterService;
    @Autowired
    AlertService alertService;
    @Autowired
    InitConfig initConfig;
    @Autowired
    HomeService homeService;
    @Autowired
    KafkaAdminService kafkaAdminService;
    @Autowired
    ZKService zkService;
    @Autowired
    CollectTopicJob collectTopicJob;
    @Autowired
    CollectMetricsJob collectMetricsJob;
    @Autowired
    CollectConsumerLagJob collectConsumerLagJob;
    @Autowired
    NoticeJob noticeJob;

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(7,
            new BasicThreadFactory.Builder().build());

    @PostConstruct
    public void init() {
        this.runCollectClusterStatistics();

        if (initConfig.isMonitorCollectEnable()) {
            this.runCollectConsumerLagJob();
            this.runCollectMetricsJob();
            noticeJob.runNoticeJob();
        }
        if(initConfig.isCollectTopicEnable()) {
            this.runCollectTopicData();
        }

    }

    private void runCollectConsumerLagJob() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> collectConsumerLagJob.collectConsumerLag(), 1, initConfig.getMonitorCollectPeriod(), TimeUnit.MINUTES);
    }


    private void runCollectMetricsJob() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> collectMetricsJob.collectMetric(),1, initConfig.getMonitorCollectPeriod(), TimeUnit.MINUTES);
    }



    private void runCollectTopicData() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> collectTopicJob.collectionTopicData(), 1, initConfig.getCollectTopicPeriod(), TimeUnit.MINUTES);

    }

    private void runCollectClusterStatistics(){
        scheduledExecutorService.scheduleWithFixedDelay(this::clusterStatistics, 1, initConfig.getCollectTopicPeriod(), TimeUnit.MINUTES);
    }

    /**
     * 更新HomePage 集群相关统计信息
     */
    private void clusterStatistics(){
        try {
            List<ClusterInfo> clusterList = clusterService.getTotalData();
            int groups = 0;
            int zk = 0;
            for(ClusterInfo cluster:clusterList) {
                groups += kafkaAdminService.getKafkaAdmins(cluster.getId().toString()).listConsumerGroups().size();
                zk += zkService.getZK(cluster.getId().toString()).listConsumerGroups().size();
            }
            HomeCache.HomePageCache pageCache = HomeCache.getConfigCache();
            int group = pageCache.getGroupSize();
            int cluster = pageCache.getClusterSize();
            int topic = pageCache.getTopicSize();
            int alert = pageCache.getAlertSize();
            pageCache.setGroupSize((zk+groups)==0?group:(zk+groups));
            pageCache.setTopicSize(topic==0?topic:homeService.getTopicList(clusterList));
            pageCache.setClusterSize(clusterList.isEmpty()?cluster:clusterList.size());
            pageCache.setAlertSize(alert==0?alert:alertService.countData());
        }catch (Exception e){
            LOG.error("cluster statistics cache fail,please check",e);
        }
    }
}
