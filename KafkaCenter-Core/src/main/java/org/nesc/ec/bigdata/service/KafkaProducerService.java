package org.nesc.ec.bigdata.service;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.nesc.ec.bigdata.common.util.KafkaProducers;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;

@Service
public class KafkaProducerService {
	
	@Autowired
	ClusterInfoMapper clusterInfoMapper;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);
	
	public void  send (String clusterId, String topicName, String key, String value ) throws Exception {
		ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		KafkaProducers<String, String> pro  = new KafkaProducers<>(props);
		try {
			pro.send(topicName, key, value);
		} catch (Exception e) {
			LOGGER.error("  Producer  topic: {} error! key:{}  value:{} ", topicName,key, value, e);
			throw e;
		} finally {
			try {
				pro.close();
			} catch (IOException e) {
				LOGGER.error("close Producer error! ");
			}
		}
	}
}
