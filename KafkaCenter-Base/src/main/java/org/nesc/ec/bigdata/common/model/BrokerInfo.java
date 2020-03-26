package org.nesc.ec.bigdata.common.model;

import java.util.Objects;

public class BrokerInfo {
	public BrokerInfo() {
		super();
	}
	private int bid;
	private String host;
	private int port;
	private int jmxPort;
	private long timestamp;
	private int version;
	
	public BrokerInfo(int bid, String host, int port) {
		super();
		this.bid = bid;
		this.host = host;
		this.port = port;
	}
	public int getBid() {
		return bid;
	}
	public void setBid(int bid) {
		this.bid = bid;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getJmxPort() {
		return jmxPort;
	}
	public void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bid,host,jmxPort,port);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; };
		if (o == null || getClass() != o.getClass()) { return false; };
		BrokerInfo brokerInfo = (BrokerInfo) o;
		return 	Objects.equals(brokerInfo.getHost().toLowerCase(), brokerInfo.getHost().toLowerCase()) &&
				Objects.equals(brokerInfo.getBid(), brokerInfo.getBid()) &&
				Objects.equals(brokerInfo.getPort(), brokerInfo.getHost());
	}
}
