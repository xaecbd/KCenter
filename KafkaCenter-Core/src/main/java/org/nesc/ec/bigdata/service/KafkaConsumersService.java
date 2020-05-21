package org.nesc.ec.bigdata.service;

import org.nesc.ec.bigdata.common.util.KafkaConsumers;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KafkaConsumersService {

    @Autowired
    ClusterInfoMapper clusterInfoMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumersService.class);

    /**
     *普通方式消费topic
     * @param clusterId
     * @param gorupName
     * @param topicName
     * @param dataSize
     * @param duration
     * @param isCommit
     * @return
     */
    public ConsumerRecords<String, String>  consumer (String clusterId, String gorupName,String topicName, int dataSize, Duration duration, boolean isCommit) {
        ConsumerRecords<String, String> result  = null;
        ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, gorupName);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, dataSize);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, TopicConfig.EARLIEST);
        KafkaConsumers<String, String>  consumer  = new KafkaConsumers<>(consumerProps);
        consumer.subscribe(topicName);
        try {
            result =  consumer.poll(duration);
        } catch (Exception e) {
            LOGGER.error("  consumer  topic: {} error! ", topicName, e);
            throw e;
        } finally {
            try {
                if(isCommit) {
                    consumer.commit();
                }
                consumer.close();
            } catch (IOException e) {
                LOGGER.error("close consumer error! ");
            }
        }
        return result;
    }

    /**
     * 通过指定partition和offset方式消费topic
     * @param clusterId
     * @param gorupName
     * @param topicName
     * @param dataSize
     * @param duration
     * @param isCommit
     * @param partition
     * @param offset
     * @return
     */
    public ConsumerRecords<String, String>  consumer (String clusterId, String gorupName,String topicName, int dataSize, Duration duration, boolean isCommit,Integer partition, Long offset) {
        ConsumerRecords<String, String> result  = null;
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, gorupName);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, dataSize);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        KafkaConsumers<String, String>  consumer  = new KafkaConsumers<>(consumerProps);
        TopicPartition topicPartition = new TopicPartition(topicName, partition);
        consumer.assign(topicPartition);
        consumer.seek(topicPartition,offset);
        try {
            result =  consumer.poll(duration);
            List<ConsumerRecord<String, String>> records = result.records(topicPartition);
            if(!records.isEmpty()){
                long lastOffset = records.get(records.size() - 1).offset();
                offsets.put(topicPartition, new OffsetAndMetadata(lastOffset + 1));
            }
        } catch (Exception e) {
            LOGGER.error("  consumer  topic: {} error! ", topicName, e);
            throw e;
        } finally {
            try {
                if(isCommit) {
                    consumer.commitByPartition(offsets);
                }
                consumer.close();
            } catch (IOException e) {
                LOGGER.error("close consumer error! ", e);
            }
        }
        return result;
    }

    public Map<TopicPartition, Long>  getLogSize (String clusterId, String gorupName,String topicName) {
        ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, gorupName);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, TopicConfig.EARLIEST);
        KafkaConsumers<String, String>  consumer  = new KafkaConsumers<>(consumerProps);
        Map<TopicPartition, Long> result = new HashMap<>();
        try {
            KafkaConsumer<String, String> kafkaConsumer = consumer.subscribe(topicName);
            List<PartitionInfo> patitions = kafkaConsumer.partitionsFor(topicName);
            List<TopicPartition>topicPatitions = new ArrayList<>();
            patitions.forEach(patition->{
                TopicPartition topicPartition = new TopicPartition(topicName,patition.partition());
                topicPatitions.add(topicPartition);
            });
            result = kafkaConsumer.endOffsets(topicPatitions);
        } catch (Exception e) {
            LOGGER.error("consumer getLogSize error.", e);
        }finally {
            try {
                consumer.close();
            } catch (IOException e) {
                LOGGER.error("close consumer error! ");
            }
        }

        return result;
    }

    public boolean commitOffsetLastest(String clusterId,String group,String topic){
        ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, cluster.getBroker());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, TopicConfig.EARLIEST);
        KafkaConsumer<String, String>  consumer  = new KafkaConsumer<>(consumerProps);
        try {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            List<TopicPartition> topicPartitions = new ArrayList<>();
            partitionInfos.forEach(topicPartition->{
                TopicPartition partition = new TopicPartition(topic,topicPartition.partition());
                topicPartitions.add(partition);
            });
            consumer.assign(topicPartitions);
            Map<TopicPartition, Long> result =consumer.endOffsets(topicPartitions);
            result.forEach(consumer::seek);
            return true;
        } catch (Exception e) {
            LOGGER.error("consumer getLogSize error.", e);
            return false;
        }finally {
            consumer.close();
        }

    }
}
