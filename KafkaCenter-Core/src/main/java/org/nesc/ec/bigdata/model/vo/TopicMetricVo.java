package org.nesc.ec.bigdata.model.vo;

import java.util.Objects;

/**
 * @author lg99
 */
public class TopicMetricVo {
    private long clusterId;
    private String date;
    private String topic;
    private long byteInMetric;
    private long byteOutMetric;
    private long fileSize;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TopicMetricVo that = (TopicMetricVo) o;
        return clusterId == that.clusterId &&
                date.equals(that.date) &&
                topic.equals(that.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clusterId, date, topic);
    }

    public TopicMetricVo(long clusterId, String date, String topic, long byteInMetric, long byteOutMetric) {
        this.clusterId = clusterId;
        this.date = date;
        this.topic = topic;
       this.byteInMetric = byteInMetric;
       this.byteOutMetric = byteOutMetric;
    }

    public TopicMetricVo(long clusterId, String date, String topic, long fileSize) {
        this.clusterId = clusterId;
        this.date = date;
        this.topic = topic;
        this.fileSize = fileSize;
    }

    public long getClusterId() {
        return clusterId;
    }

    public void setClusterId(long clusterId) {
        this.clusterId = clusterId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public long getByteInMetric() {
        return byteInMetric;
    }

    public void setByteInMetric(long byteInMetric) {
        this.byteInMetric = byteInMetric;
    }

    public long getByteOutMetric() {
        return byteOutMetric;
    }

    public void setByteOutMetric(long byteOutMetric) {
        this.byteOutMetric = byteOutMetric;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
