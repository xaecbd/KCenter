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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class JmxCollector{

    private static Logger LOG = LoggerFactory.getLogger(JmxCollector.class);
    private static final String FORMAT_URL = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
    private static Map<String,JMXConnector> jmxMap = new ConcurrentHashMap<>();

    private static class SingletonHolder {
        private static final JmxCollector INSTANCE = new JmxCollector();
    }

    public static JmxCollector getInstance() {
        return JmxCollector.SingletonHolder.INSTANCE;
    }

    private JMXConnector getJmxConnector(String host, int port) throws IOException {
        JMXServiceURL jmxUrl =  new JMXServiceURL(String.format(FORMAT_URL, host, port));
        if(!jmxMap.containsKey(host)){
            JMXConnector jmxConnector = this.connect(jmxUrl);
            jmxMap.put(host,jmxConnector);
        }
        return jmxMap.get(host);
    }

    private void removeJmxConnector(String host) {
        jmxMap.remove(host);
        LOG.error("jmx client closed");
    }


    private JMXConnector connect(JMXServiceURL jmxUrl) throws IOException {
        Map<String, Object> env = new HashMap<>();
        return JMXConnectorFactory.connect(jmxUrl, env);
    }
    public Map<String,Set<MeterMetric>> metricEveryBroker(List<BrokerInfo> brokers) throws Exception {
        return  metricEveryBrokerTopic(brokers,"");
    }

    /**
     * host,metricSet
     * */
    public Map<String,Set<MeterMetric>> metricEveryBrokerTopic(List<BrokerInfo> brokers, String topic) throws Exception {
        String[] metricName = new String[] {"MessagesInPerSec", "BytesInPerSec", "BytesOutPerSec", "BytesRejectedPerSec",
                "FailedFetchRequestsPerSec", "FailedProduceRequestsPerSec"};
        Map<String,Set<MeterMetric>> result = new HashMap<>();
        for(BrokerInfo brokerInfo : brokers){
            JMXConnector jmxConnector = this.getJmxConnector(brokerInfo.getHost(),brokerInfo.getJmxPort());
            Set<MeterMetric> metricSet = new HashSet<>();
            try{
                for(String metric:metricName){
                    MeterMetric meterMetric = getMetricValue(metric,topic,jmxConnector.getMBeanServerConnection());
                    meterMetric.setMetricName(metric);
                    meterMetric.setBroker(brokerInfo.getHost());
                    meterMetric.setJmxPort(String.valueOf(brokerInfo.getJmxPort()));
                    meterMetric.setPort(String.valueOf(brokerInfo.getPort()));
                    metricSet.add(meterMetric);
                }
            }catch (IOException e){
                removeJmxConnector(brokerInfo.getHost());
            }catch (Exception e){
                throw e;
            }
            result.put(brokerInfo.getHost(),metricSet);
        }
        return result;
    }

    public Map<String,MeterMetric> mergeBrokersMetric( Map<String,Set<MeterMetric>> result) throws Exception {
        Map<String,MeterMetric> map = new HashMap<>();
        result.forEach((host,metricSet)-> metricSet.forEach((metricObj)->{
            String metricName = metricObj.getMetricName();
            if(map.containsKey(metricName)){
                MeterMetric oldVal = map.getOrDefault(metricName,new MeterMetric());
                map.put(metricName,mergeMetric(oldVal,metricObj));
            }else{
                map.put(metricName,metricObj);
            }
        }));
        return map;
    }

    private MeterMetric getMetricValue(String metricName, String topicName, MBeanServerConnection mBeanServerConnection) throws Exception {
        JSONObject jsonObject = new JSONObject();
        ObjectName objectName =  this.getObjectName(metricName,Optional.of(topicName));
        MBeanInfo mBeanInfo = mBeanServerConnection.getMBeanInfo(objectName);
        MBeanAttributeInfo[] attrInfo = mBeanInfo.getAttributes();
        for (MBeanAttributeInfo attributeInfo:attrInfo){
            String value = mBeanServerConnection.getAttribute(objectName,attributeInfo.getName()).toString();
            if(!isString(value)){
                double d = Double.parseDouble(value);
                BigDecimal bigDecimal = new BigDecimal(d);
                jsonObject.put(attributeInfo.getName(), bigDecimal.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
        }


        return jsonObject.toJavaObject(MeterMetric.class);
    }


    private MeterMetric mergeMetric(MeterMetric old, MeterMetric newVal){
        if(old==null){
            return newVal;
        }
        if(newVal==null){
            return  old;
        }
        return new MeterMetric(old.getCount()+newVal.getCount(),old.getMeanRate()+newVal.getMeanRate(),old.getOneMinuteRate()+newVal.getOneMinuteRate(),
                old.getFiveMinuteRate()+newVal.getFiveMinuteRate(),old.getFifteenMinuteRate()+newVal.getFifteenMinuteRate());

    }

    private static boolean isString(String str) {
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
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
}
