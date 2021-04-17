package org.nesc.ec.bigdata.common.util;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.common.model.MeterMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class JmxCollector {

    private static Logger LOG = LoggerFactory.getLogger(JmxCollector.class);
    private static final String FORMAT_URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
    private static Map<String, JMXConnector> jmxMap = new ConcurrentHashMap<>();
    public static final String REGEX = "[a-zA-Z]+";

    private static class SingletonHolder {
        private static final JmxCollector INSTANCE = new JmxCollector();
    }

    public static JmxCollector getInstance() {
        return JmxCollector.SingletonHolder.INSTANCE;
    }

    private JMXConnector getJmxConnector(String host, int port) throws IOException {
        JMXServiceURL jmxUrl = new JMXServiceURL(String.format(FORMAT_URL, host, port));
        if (!jmxMap.containsKey(host)) {
            JMXConnector jmxConnector = this.connect(jmxUrl);
            jmxMap.put(host, jmxConnector);
        }
        return jmxMap.get(host);
    }

    private void removeJmxConnector(String host) {
        jmxMap.remove(host);
        LOG.error("jmx client closed");
    }


    private JMXConnector connect(JMXServiceURL jmxUrl) throws IOException {
        Map<String, Object> env = new HashMap<>(2<<2);
        return JMXConnectorFactory.connect(jmxUrl, env);
    }
    public Map<String, Set<MeterMetric>> metricEveryBroker(List<BrokerInfo> brokers) throws Exception {
        brokers = brokers.stream().filter(brokerInfo -> brokerInfo.getJmxPort() != -1).collect(Collectors.toList());
        if (brokers.isEmpty()) {
            return new HashMap<>();
        }
        return metricEveryBrokerTopic(brokers, "");
    }

    /**
     * host,metricSet
     */
    public Map<String, Set<MeterMetric>> metricEveryBrokerTopic(List<BrokerInfo> brokers, String topic) throws Exception {
        String[] metricName = new String[]{"MessagesInPerSec", "BytesInPerSec", "BytesOutPerSec", "BytesRejectedPerSec",
                "FailedFetchRequestsPerSec", "FailedProduceRequestsPerSec"};
        if ("".equalsIgnoreCase(topic)) {
            metricName = new String[]{"MessagesInPerSec", "BytesInPerSec", "BytesOutPerSec"};
        }
        Map<String, Set<MeterMetric>> result = new HashMap<>(2 << 2);
        for (BrokerInfo brokerInfo : brokers) {
            Set<MeterMetric> metricSet = new HashSet<>();
            try {
                JMXConnector jmxConnector = this.getJmxConnector(brokerInfo.getHost(), brokerInfo.getJmxPort());
                for (String metric : metricName) {
                    MeterMetric meterMetric = getMetricValue(metric, topic, jmxConnector.getMBeanServerConnection());
                    meterMetric.setMetricName(metric);
                    meterMetric.setBroker(brokerInfo.getHost());
                    meterMetric.setJmxPort(String.valueOf(brokerInfo.getJmxPort()));
                    meterMetric.setPort(String.valueOf(brokerInfo.getPort()));
                    metricSet.add(meterMetric);
                }
            } catch (IOException e) {
                removeJmxConnector(brokerInfo.getHost());
                LOG.error("connect closed:", e);
            } catch (Exception e) {
                throw e;
            }
            result.put(brokerInfo.getHost(), metricSet);
        }
        return result;
    }

    public Map<String, MeterMetric> mergeBrokersMetric(Map<String, Set<MeterMetric>> result) throws Exception {
        Map<String, MeterMetric> map = new HashMap<>(2 << 2);
        result.forEach((host, metricSet) -> metricSet.forEach((metricObj) -> {
                String metricName = metricObj.getMetricName();
                if (map.containsKey(metricName)) {
                    MeterMetric oldVal = map.getOrDefault(metricName, new MeterMetric());
                    map.put(metricName, mergeMetric(oldVal, metricObj));
                } else {
                    map.put(metricName, metricObj);
                }
        }));
        return map;
    }

    private MeterMetric getMetricValue(String metricName, String topicName, MBeanServerConnection mBeanServerConnection) {
        JSONObject jsonObject = new JSONObject();
        try {
            ObjectName objectName = this.getObjectName(metricName, Optional.of(topicName));
            MBeanInfo mBeanInfo = mBeanServerConnection.getMBeanInfo(objectName);
            MBeanAttributeInfo[] attrInfo = mBeanInfo.getAttributes();
            for (MBeanAttributeInfo attributeInfo : attrInfo) {
                String value = mBeanServerConnection.getAttribute(objectName, attributeInfo.getName()).toString();
                if (!isString(value)) {
                    double d = Double.parseDouble(value);
                    BigDecimal bigDecimal = new BigDecimal(d);
                    jsonObject.put(attributeInfo.getName(), bigDecimal.setScale(4, RoundingMode.HALF_UP).doubleValue());
                }
            }
            return jsonObject.toJavaObject(MeterMetric.class);
        } catch (InstanceNotFoundException e) {
            return new MeterMetric();
        } catch (Exception e) {
            LOG.error("collect this metric info fail:", e);
            return new MeterMetric();
        }


    }


    private MeterMetric mergeMetric(MeterMetric old, MeterMetric newVal) {
        if (old == null || old.getCount() == null) {
            return newVal;
        }
        if (newVal == null || newVal.getCount() == null) {
            return old;
        }
        return new MeterMetric(old.getCount() + newVal.getCount(), old.getMeanRate()+ newVal.getMeanRate(), old.getOneMinuteRate() + newVal.getOneMinuteRate(),
                old.getFiveMinuteRate() + newVal.getFiveMinuteRate(), old.getFifteenMinuteRate() + newVal.getFifteenMinuteRate());

    }


    private static boolean isString(String str) {
        Pattern pattern = Pattern.compile(REGEX);
        return pattern.matcher(str).matches();
    }

    private ObjectName getObjectName(String metricName, Optional<String> topicName) {
        ObjectName objectName = null;
        try {
            if (topicName.isPresent() && !"".equalsIgnoreCase(topicName.get())) {
                objectName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=" + metricName + ",topic=" + topicName.get());
            } else {
                objectName = new ObjectName("kafka.server:type=BrokerTopicMetrics,name=" + metricName);
            }
        } catch (MalformedObjectNameException e) {
            LOG.debug("Get ObjectName error! " + e.getMessage());
        }
        return objectName;
    }

    /**
     * return the topic size according to cluster`s brokers
     * @param brokerInfoList cluster`s broker information
     * @param  topicInfoMap key:topic name value:topic partition`s list
     * */
    public Map<String, Long> topicLogSizeByBroker(List<BrokerInfo> brokerInfoList,Map<String,Set<Integer>> topicInfoMap){
        Map<Integer, Map<String,Long>> brokerMap = new HashMap<>(2 << 2);
        for (BrokerInfo brokerInfo : brokerInfoList){
            try{
                JMXConnector jmxConnector = this.getJmxConnector(brokerInfo.getHost(), brokerInfo.getJmxPort());
                MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
                Map<String,Long> topicSizeMap = getTopicLogSize(topicInfoMap,mBeanServerConnection);
                brokerMap.put(brokerInfo.getBid(),topicSizeMap);
            }catch (IOException e) {
                removeJmxConnector(brokerInfo.getHost());
                LOG.error("connect closed:", e);
            } catch (Exception e) {
                throw e;
            }
        }
        return mergeBrokerLogSize(brokerMap);
    }


    /**
     * merge every broker according to topicName
     * @param  topicLogSizeMap key: brokerId value:  key:topicName value:file size
     * */
    private  Map<String, Long> mergeBrokerLogSize(Map<Integer, Map<String, Long>> topicLogSizeMap){
        Map<String, Long> sizeMap = new HashMap<>(2 << 2);
        topicLogSizeMap.keySet().forEach(key->{
            Map<String,Long> map = topicLogSizeMap.getOrDefault(key,new HashMap<>(1));
            map.keySet().forEach(topic->{
                long oldSize = sizeMap.getOrDefault(topic,0L);
                sizeMap.put(topic,oldSize+map.getOrDefault(topic,0L));
            });
        });
        return sizeMap;
    }

    /**
     * return the topic file size by topic information and merge every partitions value
     * @param topicInfoMap key:topic name value:topic partition`s list
     * @param  mBeanServerConnection broker jmx connection
     * */
    private Map<String,Long>  getTopicLogSize(Map<String,Set<Integer>> topicInfoMap,MBeanServerConnection mBeanServerConnection){
        Map<String,Long> topicLogSize = new HashMap<>(2 << 2);
        topicInfoMap.keySet().forEach(key->{
            try{
                Set<Integer> partitions = topicInfoMap.get(key);
                long size = getLogSizeMetricValue(key,partitions,mBeanServerConnection);
                topicLogSize.put(key,size);
            }catch (Exception e){
                LOG.error("get topic log size has error",e);
            }

        });
        return topicLogSize;
    }

    /**
     * return the topic file size by partitions
     * @param  topicName topic name
     * @param  partitions topic`s partition list
     * @param mBeanServerConnection   broker jmx connection
     * */
    private long getLogSizeMetricValue(String topicName,Set<Integer> partitions,MBeanServerConnection mBeanServerConnection){
        return partitions.stream().mapToLong(partition->{
            String value = null;
            try {
                value = mBeanServerConnection.getAttribute(getLogSizeObjectName(topicName,partition),"Value").toString();
            } catch (InstanceNotFoundException ignored) {

            }catch (Exception e){
                LOG.warn("get topic log size has error",e);
            }
            return value!=null?Long.parseLong(value):0L;
        }).sum();


    }

    /**
     * return the kafka log size Object Name
     * */
    private ObjectName getLogSizeObjectName(String topicName,int partition){
        ObjectName objectName = null;
        try{
            objectName = new ObjectName("kafka.log:type=Log,name=Size,topic="+topicName +",partition="+partition);
        } catch (MalformedObjectNameException e) {
            LOG.debug("Get topic Log Size ObjectName error! " + e.getMessage());
        }
        return objectName;
    }

}
