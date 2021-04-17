package org.nesc.ec.bigdata.common.model;

import java.util.Objects;

public class MeterMetric {
	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
					   Double fifteenMinuteRate, String metricName, String clusterID) {
		super();
		this.count = count;
		this.meanRate = meanRate;
		this.oneMinuteRate = oneMinuteRate;
		this.fiveMinuteRate = fiveMinuteRate;
		this.fifteenMinuteRate = fifteenMinuteRate;
		this.metricName = metricName;
		this.clusterID = clusterID;
	}
	
	public MeterMetric(Long count,Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
					   Double fifteenMinuteRate,String metricName) {
		super();
		this.count = count;
		this.meanRate = meanRate;
		this.oneMinuteRate = oneMinuteRate;
		this.fiveMinuteRate = fiveMinuteRate;
		this.fifteenMinuteRate = fifteenMinuteRate;
		this.metricName = metricName;
	}
	
	
	public MeterMetric(Long count, Double meanRate, Double oneMinuteRate, Double fiveMinuteRate,
					   Double fifteenMinuteRate) {
		super();
		this.count = count;
		this.meanRate = meanRate;
		this.oneMinuteRate = oneMinuteRate;
		this.fiveMinuteRate = fiveMinuteRate;
		this.fifteenMinuteRate = fifteenMinuteRate;
	}


	private Long count;
	private Double meanRate;
	private Double oneMinuteRate;
	private Double fiveMinuteRate;
	private Double fifteenMinuteRate;
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
		return meanRate;
	}

	public void setMeanRate(Double meanRate) {
		this.meanRate = meanRate;
	}

	public Double getOneMinuteRate() {
		return oneMinuteRate;
	}

	public void setOneMinuteRate(Double oneMinuteRate) {
		this.oneMinuteRate = oneMinuteRate;
	}

	public Double getFiveMinuteRate() {
		return fiveMinuteRate;
	}

	public void setFiveMinuteRate(Double fiveMinuteRate) {
		this.fiveMinuteRate = fiveMinuteRate;
	}

	public Double getFifteenMinuteRate() {
		return fifteenMinuteRate;
	}

	public void setFifteenMinuteRate(Double fifteenMinuteRate) {
		this.fifteenMinuteRate = fifteenMinuteRate;
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
		return "MeterMetric{" +
				"count=" + count +
				", meanRate=" + meanRate +
				", oneMinuteRate=" + oneMinuteRate +
				", fiveMinuteRate=" + fiveMinuteRate +
				", fifteenMinuteRate=" + fifteenMinuteRate +
				", clusterID='" + clusterID + '\'' +
				", broker='" + broker + '\'' +
				", port='" + port + '\'' +
				", jmxPort='" + jmxPort + '\'' +
				", location='" + location + '\'' +
				", clusterName='" + clusterName + '\'' +
				", metricName='" + metricName + '\'' +
				", topic='" + topic + '\'' +
				'}';
	}
}
