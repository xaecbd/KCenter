package org.nesc.ec.bigdata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lg99
 */
@Configuration
public class InitConfig {
	@Value("${monitor.collect.period.minutes:3}")
	private int monitorCollectPeriod;
	@Value("${monitor.enable:true}")
	private boolean  monitorCollectEnable;
	@Value("${remote.query.enable:false}")
	private boolean remoteQueryEnable;
	@Value("${monitor.collector.include.enable:false}")
	private boolean monitorCollectorIncludeEnable;
	@Value("${monitor.collector.include.location:dev}")
	private String monitorCollectorIncludelocation;
	@Value("${remote.hosts:localhost}")
	private String remoteHosts;
	@Value("${remote.locations:dev,prd}")
	private String remoteLocations;
	@Value("${connect.url:localhost:8081}")
	private String connectUrl;
	@Value("${collect.topic.period.minutes:10}")
	private int collectTopicPeriod;
	@Value("${collect.topic.enable:false}")
	private boolean collectTopicEnable;
	@Value("${system.topic.ttl.h:16}")
	private Integer ttl;
    @Value("${spring.mail.username:kafkacenter@kafka.com}")
    private String emailFrom;
    @Value("${public.url:localhost:8080}")
    private String kafkaCenterUrl;

	@Value("${spring.security.user.name}")
	private String adminname;


	@Value("${mail.enable:true}")
	private Boolean mailEnable;


	@Value("${monitor.elasticsearch.hosts:}")
	private String monitorElasticsearchHost;
	@Value("${monitor.elasticsearch.index:}")
	private String monitorElasticsearchIndexName;
	@Value("${monitor.elasticsearch.auth.user:}")
	private String monitorElasticsearchAuthUser;
	@Value("${monitor.elasticsearch.auth.password:}")
	private String monitorElasticsearchAuthPassword;

	@Value("${ark.login.verify.url:}")
	private String loginVerifyUrl;

	@Value("${collect.ksql.info.job.enable:false}")
	private boolean collectKsqlInfoJobEnable;
	@Value("${collect.ksql.info.job.period.minutes:5}")
	private int collectKsqlInfoJobPeriodMinutes;

	@Value("${collect.connector.job.enable:false}")
	private boolean collectorJobEnable;
	@Value("${collect.connector.job.period.minutes:5}")
	private int collectorJobPeriodMinutes;

	public boolean isCollectorJobEnable() {
		return collectorJobEnable;
	}

	public void setCollectorJobEnable(boolean collectorJobEnable) {
		this.collectorJobEnable = collectorJobEnable;
	}

	public int getCollectorJobPeriodMinutes() {
		return collectorJobPeriodMinutes;
	}

	public void setCollectorJobPeriodMinutes(int collectorJobPeriodMinutes) {
		this.collectorJobPeriodMinutes = collectorJobPeriodMinutes;
	}

	public boolean isCollectKsqlInfoJobEnable() {
		return collectKsqlInfoJobEnable;
	}

	public int getCollectKsqlInfoJobPeriodMinutes() {
		return collectKsqlInfoJobPeriodMinutes;
	}

	public String getAdminname() {
		return adminname;
	}

	public Integer getTtl() {
		return ttl;
	}

	public void setTtl(Integer ttl) {
		this.ttl = ttl;
	}

	public boolean isCollectTopicEnable() {
		return collectTopicEnable;
	}


	public int getCollectTopicPeriod() {
		return collectTopicPeriod;
	}


	public String getRemoteLocations() {
		return remoteLocations;
	}

	public int getMonitorCollectPeriod() {
		return monitorCollectPeriod;
	}


	public boolean isMonitorCollectEnable() {
		return monitorCollectEnable;
	}


	public boolean isRemoteQueryEnable() {
		return remoteQueryEnable;
	}


	public String getMonitorCollectorIncludelocation() {
		return monitorCollectorIncludelocation;
	}


	public Map<String,String> getRemoteHostsMap() {
		Map<String,String> map = new HashMap<>(2<<4);
		if(remoteHosts==null||remoteHosts.isEmpty()) {
			return map;
		}
		String[] hosts = remoteHosts.split(",");
		for(String host:hosts) {
			String[] array = host.split("@",-1);
			map.put(array[0].toLowerCase(), array[1]);
		}
		return map;
	}

	public boolean isMonitorCollectorIncludeEnable() {
		return monitorCollectorIncludeEnable;
	}

	public String getConnectUrl() {
		return connectUrl;
	}

	public String getEmailFrom() {
		return emailFrom;
	}


	public String getKafkaCenterUrl() {
		return kafkaCenterUrl;
	}

	public Boolean getMailEnable() {
		return mailEnable;
	}

	public String getMonitorElasticsearchHost() {
		return monitorElasticsearchHost;
	}

	public String getMonitorElasticsearchIndexName() {
		return monitorElasticsearchIndexName;
	}

	public String getMonitorElasticsearchAuthUser() {
		return monitorElasticsearchAuthUser;
	}

	public String getMonitorElasticsearchAuthPassword() {
		return monitorElasticsearchAuthPassword;
	}

	public String getLoginVerifyUrl() {
		return loginVerifyUrl;
	}

	public void setLoginVerifyUrl(String loginVerifyUrl) {
		this.loginVerifyUrl = loginVerifyUrl;
	}
}
