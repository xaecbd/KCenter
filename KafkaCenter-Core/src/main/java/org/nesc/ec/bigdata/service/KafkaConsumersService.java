package org.nesc.ec.bigdata.service;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.nesc.ec.bigdata.common.util.KafkaConsumers;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Service
public class KafkaConsumersService {

    @Autowired
    ClusterService clusterService;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumersService.class);

    /**
     *普通方式消费topic
     * @param clusterId
     * @param groupName
     * @param topicName
     * @param dataSize
     * @param duration
     * @param isCommit
     * @return
     */
    public ConsumerRecords<String, String>  consumer (String clusterId, String groupName,String topicName, int dataSize, Duration duration, boolean isCommit) {
        ConsumerRecords<String, String> result  = null;
        String brokers = clusterService.getBrokers(clusterId);
        Properties consumerProps = generatorConsumerProps(brokers,groupName,topicName,false);
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, dataSize);
        KafkaConsumers<String, String>  consumer  = new KafkaConsumers<>(consumerProps);;
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
     * @param groupName
     * @param topicName
     * @param dataSize
     * @param duration
     * @param isCommit
     * @param partition
     * @param offset
     * @return
     */
    public ConsumerRecords<String, String>  consumer (String clusterId, String groupName,String topicName, int dataSize, Duration duration, boolean isCommit,Integer partition, Long offset) {
        ConsumerRecords<String, String> result  = null;
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
        String brokers = clusterService.getBrokers(clusterId);
        Properties consumerProps = generatorConsumerProps(brokers,groupName,topicName,false);;
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, dataSize);

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
                LOGGER.error("close consumer error! ");
            }
        }
        return result;
    }

    public Map<TopicPartition, Long>  getLogSize (String clusterId, String groupName,String topicName) {
        KafkaConsumer<String, String> kafkaConsumer = null;
        Map<TopicPartition, Long> result = new HashMap<>();
        List<TopicPartition>topicPartitions = new ArrayList<>();
        try {
            kafkaConsumer = generatorKafkaCustomer(clusterId,groupName,topicName,false);
            List<PartitionInfo> partitions = kafkaConsumer.partitionsFor(topicName);

            partitions.forEach(partition->{
                TopicPartition topicPartition = new TopicPartition(topicName,partition.partition());
                topicPartitions.add(topicPartition);
            });
            result = kafkaConsumer.endOffsets(topicPartitions);
        } catch (Exception e) {
            LOGGER.error("consumer getLogSize error.", e);
        }finally {
            try {
                kafkaConsumer.close();
            } catch (Exception e) {
                LOGGER.error("close consumer error! ",e);
            }
        }

        return result;
    }

    public boolean commitOffsetLatest(String clusterId, String group, String topic){

        KafkaConsumer<String, String>  consumer  = generatorKafkaCustomer(clusterId,group,topic,true);
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
            LOGGER.error("consumer commitOffsetLatest error.", e);
            return false;
        }finally {
            try {
                consumer.close();
            } catch (Exception e) {
                LOGGER.error("close consumer error! ",e);
            }
        }
    }

    private KafkaConsumer<String, String> generatorKafkaCustomer(String clusterId,String group,String topic,boolean isAutoCommit){
        String brokers = clusterService.getBrokers(clusterId);
        Properties consumerProps = generatorConsumerProps(brokers,group,topic,isAutoCommit);
        KafkaConsumer<String, String>  consumer  = new KafkaConsumer<>(consumerProps);
        return consumer;
    }

    private Properties generatorConsumerProps(String brokers,String group,String topic,boolean isAutoCommit){
        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, isAutoCommit);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  StringDeserializer.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, TopicConfig.EARLIEST);
        return consumerProps;
    }

}
