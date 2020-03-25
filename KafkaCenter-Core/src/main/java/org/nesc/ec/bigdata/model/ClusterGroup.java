package org.nesc.ec.bigdata.model;

import java.util.Objects;

/**
 * 
 * @author jc1e
 *
 * 2019年5月11日
 */
public class ClusterGroup {
	private String clusterName;
	private Long clusterID;
	private String consummerGroup;
	private String consumereApi;

	public String getConsumereApi() {
		return consumereApi;
	}

	public void setConsumereApi(String consumereApi) {
		this.consumereApi = consumereApi;
	}

	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public Long getClusterID() {
		return clusterID;
	}
	public void setClusterID(Long clusterID) {
		this.clusterID = clusterID;
	}
	public String getConsummerGroup() {
		return consummerGroup;
	}
	public void setConsummerGroup(String consummerGroup) {
		this.consummerGroup = consummerGroup;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
            return true;
        }
		if (o == null || getClass() != o.getClass()) {
            return false;
        }
		ClusterGroup that = (ClusterGroup) o;
		return Objects.equals(clusterName, that.clusterName) &&
				Objects.equals(clusterID, that.clusterID) &&
				Objects.equals(consummerGroup, that.consummerGroup) &&
				Objects.equals(consumereApi, that.consumereApi);
	}

	@Override
	public int hashCode() {
		return Objects.hash(clusterName, clusterID, consummerGroup, consumereApi);
	}
}
