package org.nesc.ec.bigdata.service;

import org.apache.kafka.common.ConsumerGroupState;
import org.nesc.ec.bigdata.cache.ExpireMap;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.GroupTopicConsumerState;
import org.nesc.ec.bigdata.common.model.PartitionAssignmentState;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TopicGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author lg99
 */
@Service
public class ConsumerLagApiService {

    @Autowired
    MonitorService monitorService;
    @Autowired
    ClusterService clusterService;

    public List<Object> describeConsumerLagStatus(String clusterId, String topic, String consumerGroup) throws Exception {
        ClusterInfo info = clusterService.selectById(Long.parseLong(clusterId));
        List<GroupTopicConsumerState> brokerConsumerGroupStateList;
        List<GroupTopicConsumerState> zkConsumerGroupStateList;
        try {
            brokerConsumerGroupStateList = monitorService.getBrokerConsumerOffsetsByGroup(info, consumerGroup);
            zkConsumerGroupStateList = monitorService.getZKConsumerOffsetsByGroup(info, consumerGroup);
        } catch (Exception e) {
            throw new Exception("describe consumer group has error,please check!");
        }

        GroupTopicConsumerState brokerTopicConsumerState = CollectionUtils.isEmpty(brokerConsumerGroupStateList) ? null :
                brokerConsumerGroupStateList.stream().filter(brokerConsumerGroupState -> topic.equalsIgnoreCase(brokerConsumerGroupState.getTopic())).findAny().get();
        GroupTopicConsumerState zkTopicConsumerState = CollectionUtils.isEmpty(zkConsumerGroupStateList) ? null :
                zkConsumerGroupStateList.stream().filter(zkConsumerGroupState -> topic.equalsIgnoreCase(zkConsumerGroupState.getTopic())).findAny().get();
        Object brokerValue = parseConsumerLagStatus(brokerTopicConsumerState, consumerGroup, info);
        Object zkValue = parseConsumerLagStatus(zkTopicConsumerState, consumerGroup, info);

        return parseResult(brokerValue, zkValue);
    }

    Object parseConsumerLagStatus(GroupTopicConsumerState groupTopicConsumerState, String consumer, ClusterInfo info) {
        if (Objects.isNull(groupTopicConsumerState)) {
            return "";
        }
        List<PartitionAssignmentState> partitionAssignmentStates = groupTopicConsumerState.getPartitionAssignmentStates();
        long lag = partitionAssignmentStates.stream().mapToLong(PartitionAssignmentState::getLag).sum();


        TopicGroup topicGroup = new TopicGroup(info.getId().toString(), groupTopicConsumerState.getTopic(), consumer, groupTopicConsumerState.getConsumerMethod());
        String status = monitorService.judgeStateByGroupTopicConsumerState(groupTopicConsumerState,info).name();

        status = Objects.equals(ConsumerGroupState.STABLE.name(), status) ? Constants.Status.ACTIVE : status;
        HomeCache.ConsumerLagCache consumerLagCache = new HomeCache.ConsumerLagCache(consumer,
                groupTopicConsumerState.getTopic(), status, lag, groupTopicConsumerState.getConsumerMethod());
        putToExpireCacheMap(topicGroup.generateKey(), consumerLagCache);
        return consumerLagCache;
    }


    private void putToExpireCacheMap(String key, Object value) {
        ExpireMap.put(key, value);
    }


    public List<Object> parseResult(Object brokerValue, Object zkValue) {
        List<Object> result = new ArrayList<>();
        if (!Objects.equals("", brokerValue) && !Objects.isNull(brokerValue)) {
            result.add(brokerValue);
        }
        if (!Objects.equals("", zkValue) && !Objects.isNull(zkValue)) {
            result.add(zkValue);
        }
        return result;
    }
}
