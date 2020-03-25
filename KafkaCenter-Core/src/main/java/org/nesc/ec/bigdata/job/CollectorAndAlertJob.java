package org.nesc.ec.bigdata.job;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Truman.P.Du
 * @date 2019年4月17日 下午4:11:46
 * @version 1.0
 */
@Component
public class CollectorAndAlertJob {

	private static final Logger LOG = LoggerFactory.getLogger(CollectorAndAlertJob.class);

	@Autowired
	ClusterService clusterService;
	@Autowired
	AlertService alertService;
	@Autowired
	EmailService emailService;
	@Autowired
	InitConfig initConfig;
	@Autowired
	RestTemplate restTemplate;
	@Autowired
	HomeService homeService;
	@Autowired
	KafkaAdminService kafkaAdminService;
	@Autowired
	ZKService zkService;

	@Autowired
	AlertaJob alertaJob;

	@Autowired
	CollectTopicJob collectTopicJob;

	@Autowired
	CollectMetricsJob collectMetricsJob;

	@Autowired
	CollectConsumerJob collectConsumerJob;

	private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(7);

	static BlockingQueue<MonitorNoticeInfo> alertQueue = new ArrayBlockingQueue<>(1000);
	static Map<String, Long> cachedLastSendTime = new HashMap<>();

	@PostConstruct
	public void init() {
		if (initConfig.isMonitorCollectEnable()) {
			this.runCollectConsumerLagJob();
			this.runCollectMetricsJob();
			this.runNoticeJob();
			this.runHomePageData();
		}
		if(initConfig.isCollectTopicEnable()) {
			this.runCollectTopicData();
		}

	}

	private void runCollectConsumerLagJob() {
		scheduledExecutorService.scheduleWithFixedDelay(() -> collectConsumerJob.collectConsumer(), 1, initConfig.getMonitorCollectPeriod(), TimeUnit.MINUTES);
	}


	private void runCollectMetricsJob() {
		scheduledExecutorService.scheduleWithFixedDelay(() -> collectMetricsJob.collectMetric(),1, initConfig.getMonitorCollectPeriod(), TimeUnit.MINUTES);
	}

	private void runCollectTopicData() {
		scheduledExecutorService.scheduleWithFixedDelay(() -> collectTopicJob.collectionTopicData(), 1, initConfig.getCollectTopicPeriod(), TimeUnit.MINUTES);

	}

	private void runHomePageData(){
		scheduledExecutorService.scheduleWithFixedDelay(this::updateClusterInfo, 1, initConfig.getCollectTopicPeriod(), TimeUnit.MINUTES);
	}

	/**
	 * 通知线程，主要是用法发用监控告警邮件和webhook
	 */
	private void runNoticeJob() {
		Thread thread = new Thread(() -> {
			while (true) {
				try {
					MonitorNoticeInfo monitorNoticeInfo = alertQueue.take();
					AlertGoup alertGroup = monitorNoticeInfo.getAlertGoup();
					long dispause =Optional.ofNullable(alertGroup.getDispause()).orElse(0);
					String key = alertGroup.getCluster().getId() + Constants.Symbol.Vertical_STR + alertGroup.getTopicName() + Constants.Symbol.Vertical_STR
							+ alertGroup.getConsummerGroup();
					if (cachedLastSendTime.containsKey(key)) {
						Long lastTime = cachedLastSendTime.get(key);
						Long now = System.currentTimeMillis();
						if ((now - lastTime) >= dispause * 60 * 1000) {
							try {
								if(monitorNoticeInfo.getSendType().equalsIgnoreCase(Constants.SendType.EMAIL)) {
									sendToEmailOrWebHook(monitorNoticeInfo);
								}
							} catch (Exception e) {
								LOG.error("Alert Exception: "+key, e);
							}
						}
						alertaJob.sendToAlerta(monitorNoticeInfo,now,lastTime);
					} else {
						try {
							alertaJob.sendToAlerta(monitorNoticeInfo);
							if(monitorNoticeInfo.getSendType().equalsIgnoreCase(Constants.SendType.EMAIL)) {
								sendToEmailOrWebHook(monitorNoticeInfo);
							}
						} catch (Exception e) {
							LOG.error("Alert Exception :"+key, e);
						}
					}
				} catch (InterruptedException e) {
					LOG.error("Run Notice job has error: ", e);
				}
			}
		});
		thread.start();
	}


	private void sendToEmailOrWebHook(MonitorNoticeInfo monitorNoticeInfo) throws Exception {
		AlertGoup alertGroup = monitorNoticeInfo.getAlertGoup();
		String concont = alertGroup.getCluster().getName()+Constants.Symbol.Vertical_STR+alertGroup.getTopicName()+
				Constants.Symbol.Vertical_STR+alertGroup.getConsummerGroup()+Constants.Symbol.Vertical_STR+alertGroup.getConsummerApi();
		String key = alertGroup.getCluster().getId() + Constants.Symbol.Vertical_STR + alertGroup.getTopicName() + Constants.Symbol.Vertical_STR
				+ alertGroup.getConsummerGroup();
		cachedLastSendTime.put(key, System.currentTimeMillis());
		if(alertGroup.isEnable()) {
			if(StringUtils.isNotBlank(alertGroup.getMailTo()) ){
				generateEmailContentAndSend(monitorNoticeInfo,concont);
			}
			if (StringUtils.isNotBlank(monitorNoticeInfo.getAlertGoup().getWebhook())) {
				generateWebHookContentAndSend(monitorNoticeInfo,concont);
			}
		}
		
	}


	/**
	 * 生成邮件内容并发送
	 *
	 * @param monitorNoticeInfo  监控实体
	 * @throws Exception 抛出异常，上级捕获
	 */
	private void generateEmailContentAndSend(MonitorNoticeInfo monitorNoticeInfo,String concont) throws Exception {
		try {
			// 获取邮件内容
			Map<String, Object> emailMap = alertService.getEmailAllMessage(monitorNoticeInfo);
			emailService.randerTemplate(emailMap.get("emailEntity"),emailMap.get("emailContent"),4);

		} catch (Exception e) {
			LOG.error("generateEmailContentAndSendException: "+concont, e);
		}
	}

	/**
	 * 生成WebHook信息，并通知
	 *
	 * @param monitorNoticeInfo 监控实体
	 * @throws Exception 抛出异常，上级捕获
	 */
	private void generateWebHookContentAndSend(MonitorNoticeInfo monitorNoticeInfo,String concont) throws Exception {
		try {
			AlertGoup alertGoup = monitorNoticeInfo.getAlertGoup();

			String webHookUrl = alertGoup.getWebhook();
			List<Map<String, Object>> list = new ArrayList<>();
			for (OffsetInfo offsetInfo : monitorNoticeInfo.getOffsetInfos()) {
				Map<String, Object> map = new HashMap<>();
				map.put(BrokerConfig.GROUP, offsetInfo.getGroup());
				map.put(BrokerConfig.TOPIC, offsetInfo.getTopic());
				map.put(TopicConfig.PARTITIONS, offsetInfo.getPartition());
				map.put(TopicConfig.LAG, offsetInfo.getLag());
				map.put(Constants.KeyStr.COMSUMBER_API, offsetInfo.getConsumerMethod());
				list.add(map);
			}
			HttpHeaders headers = new HttpHeaders();
			headers.add(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.APPLICATION_JSON);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(Constants.KeyStr.MESSAGE, list);
			jsonObject.put(Constants.KeyStr.CLUSTER, alertGoup.getCluster().getName());
			HttpEntity<JSONObject> httpEntity = new HttpEntity<>(jsonObject, headers);
			restTemplate.postForEntity(webHookUrl, httpEntity, String.class);
			LOG.info("generateWebHookContentAndSend..."+concont);
		} catch (Exception e) {
			throw new RuntimeException("generateWebHookContentAndSendException: ", e);
		}
	}


	private void updateClusterInfo(){
		try {
			List<ClusterInfo> clusterList = clusterService.getTotalData();
			int groups = 0;
			int zk = 0;
			for(ClusterInfo cluser:clusterList) {
				groups += kafkaAdminService.getKafkaAdmins(cluser.getId().toString()).listConsumerGroups().size();
				zk += zkService.getZK(cluser.getId().toString()).listConsumerGroups().size();
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
			LOG.error("update clusterInfo cache faild,please check",e);
		}
	}



}
