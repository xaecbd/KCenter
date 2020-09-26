package org.nesc.ec.bigdata.common.model;

import java.util.Objects;

public class MeterMetric {
	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
			Double fifteenMinuteRate,String metricName,String clusterID) {
		super();
		this.count = count;
		MeanRate = meanRate;
		OneMinuteRate = oneMinuteRate;
		FiveMinuteRate = fiveMinuteRate;
		FifteenMinuteRate = fifteenMinuteRate;
		this.metricName = metricName;
		this.clusterID = clusterID;
	}
	
	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
			Double fifteenMinuteRate,String metricName) {
		super();
		this.count = count;
		MeanRate = meanRate;
		OneMinuteRate = oneMinuteRate;
		FiveMinuteRate = fiveMinuteRate;
		FifteenMinuteRate = fifteenMinuteRate;
		this.metricName = metricName;
	}
	
	
	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
			Double fifteenMinuteRate) {
		super();
		this.count = count;
		MeanRate = meanRate;
		OneMinuteRate = oneMinuteRate;
		FiveMinuteRate = fiveMinuteRate;
		FifteenMinuteRate = fifteenMinuteRate;
	}


	private Long count;
	private Double MeanRate;
	private Double OneMinuteRate;
	private Double FiveMinuteRate;
	private Double FifteenMinuteRate;
	private String clusterID;
	private String broker;
	private String port;
	private String jmxPort;
	private String location;
    private String clusterName;
	private String metricName;
	private String topic;

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getLocation() {
		return location;
	}
  
	public void setLocation(String location) {
		this.location = location;
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

	public void setJmxPort(String jmxPort) {
		this.jmxPort = jmxPort;
	}

	public String getBroker() {
		return broker;
	}

	public void setBroker(String broker) {
		this.broker = broker;
	}

	public String getClusterID() {
		return clusterID;
	}

	public void setClusterID(String clusterID) {
		this.clusterID = clusterID;
	}

	public MeterMetric() {
		super();
		// TODO Auto-generated constructor stub
	}
	public String getMetricName() {
		return metricName;
	}
	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public Double getMeanRate() {
		return MeanRate;
	}
	public void setMeanRate(Double meanRate) {
		MeanRate = meanRate;
	}
	public Double getOneMinuteRate() {
		return OneMinuteRate;
	}
	public void setOneMinuteRate(Double oneMinuteRate) {
		OneMinuteRate = oneMinuteRate;
	}
	public Double getFiveMinuteRate() {
		return FiveMinuteRate;
	}
	public void setFiveMinuteRate(Double fiveMinuteRate) {
		FiveMinuteRate = fiveMinuteRate;
	}
	public Double getFifteenMinuteRate() {
		return FifteenMinuteRate;
	}
	public void setFifteenMinuteRate(Double fifteenMinuteRate) {
		FifteenMinuteRate = fifteenMinuteRate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(broker,metricName,clusterID,port);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MeterMetric meterMetric = (MeterMetric) o;
		return (broker == meterMetric.getBroker()) || (broker != null && broker.equalsIgnoreCase(meterMetric.getBroker())) &&
				(metricName == meterMetric.getMetricName()) || (metricName != null && metricName.equalsIgnoreCase(meterMetric.getMetricName())) &&
				(clusterID == meterMetric.getClusterID()) || (clusterID != null && clusterID.equalsIgnoreCase(meterMetric.getClusterID())) &&
				(port == meterMetric.getPort()) || (port != null && port.equalsIgnoreCase(meterMetric.getPort()));
	}
	@Override
	public String toString() {
		return "MeterMetric [count=" + count + ", MeanRate=" + MeanRate + ", OneMinuteRate=" + OneMinuteRate
				+ ", FiveMinuteRate=" + FiveMinuteRate + ", FifteenMinuteRate=" + FifteenMinuteRate + ", clusterID="
				+ clusterID + ", broker=" + broker + ", metricName=" + metricName + "]";
	}

	


	

	
}
