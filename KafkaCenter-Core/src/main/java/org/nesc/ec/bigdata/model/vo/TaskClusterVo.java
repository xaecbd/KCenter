package org.nesc.ec.bigdata.model.vo;

import java.util.List;
/**
 * 
 * @author jc1e
 *
 */
public class TaskClusterVo {
	private String clusterName;
	private String clusterVersion;
	private Integer brokerSize;
    private List<String> brokerList;
    
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getClusterVersion() {
		return clusterVersion;
	}
	public void setClusterVersion(String clusterVersion) {
		this.clusterVersion = clusterVersion;
	}
	public Integer getBrokerSize() {
		return brokerSize;
	}
	public void setBrokerSize(Integer brokerSize) {
		this.brokerSize = brokerSize;
	}
	public List<String> getBrokerList() {
		return brokerList;
	}
	public void setBrokerList(List<String> brokerList) {
		this.brokerList = brokerList;
	}
}
