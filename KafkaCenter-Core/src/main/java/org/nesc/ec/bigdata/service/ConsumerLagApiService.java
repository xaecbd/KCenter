package org.nesc.ec.bigdata.service;

import org.apache.kafka.common.ConsumerGroupState;
import org.nesc.ec.bigdata.cache.ExpireMap;
import org.nesc.ec.bigdata.cache.HomeCache;
import org.nesc.ec.bigdata.common.model.GroupTopicConsumerState;
import org.nesc.ec.bigdata.common.model.PartitionAssignmentState;
import org.nesc.ec.bigdata.common.model.TopicConsumerGroupState;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import scala.collection.immutable.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author lg99
 */
@Service
public class ConsumerLagApiService {

    @Autowired
    MonitorService monitorService;

    public List<Object> describeConsumerLagStatus(String clusterId,String topic,String consumerGroup) throws Exception {
        List<GroupTopicConsumerState> brokerConsumerGroupStateList;
        List<GroupTopicConsumerState> zkConsumerGroupStateList;
        try {
            brokerConsumerGroupStateList =  monitorService.getBrokerConsumerOffsetsByGroup(clusterId,consumerGroup);
            zkConsumerGroupStateList = monitorService.getZKConsumerOffsetsByGroup(clusterId,consumerGroup);
        }catch (Exception e){
            throw  new Exception("describe consumer group has error,please check!");
        }

         GroupTopicConsumerState brokerTopicConsumerState = CollectionUtils.isEmpty(brokerConsumerGroupStateList)?null:
                brokerConsumerGroupStateList.stream().filter(brokerConsumerGroupState->topic.equalsIgnoreCase(brokerConsumerGroupState.getTopic())).findAny().get();
         GroupTopicConsumerState zkTopicConsumerState = CollectionUtils.isEmpty(zkConsumerGroupStateList)?null:
                zkConsumerGroupStateList.stream().filter(zkConsumerGroupState->topic.equalsIgnoreCase(zkConsumerGroupState.getTopic())).findAny().get();
          Object brokerValue = parseConsumerLagStatus(brokerTopicConsumerState,consumerGroup,clusterId);
          Object zkValue = parseConsumerLagStatus(zkTopicConsumerState,consumerGroup,clusterId);

          return parseResult(brokerValue,zkValue);
    }

    Object parseConsumerLagStatus(GroupTopicConsumerState groupTopicConsumerState,String consumer,String clusterId){
        if(Objects.isNull(groupTopicConsumerState)){
            return "";
        }
        List<PartitionAssignmentState> partitionAssignmentStates = groupTopicConsumerState.getPartitionAssignmentStates();
        long lag =  partitionAssignmentStates.stream().mapToLong(PartitionAssignmentState::getLag).sum();
        String key = generateKey(clusterId,consumer,groupTopicConsumerState.getTopic(),groupTopicConsumerState.getConsumerMethod());
        String status = lagStatus(groupTopicConsumerState.getConsumerGroupState(),partitionAssignmentStates,key,
                Objects.equals("zk",groupTopicConsumerState.getConsumerMethod()),groupTopicConsumerState.isSimpleConsumerGroup()).name();
        status = Objects.equals(ConsumerGroupState.STABLE.name(),status)?Constants.Status.ACTIVE:status;
        HomeCache.ConsumerLagCache consumerLagCache = new HomeCache.ConsumerLagCache(consumer,
                groupTopicConsumerState.getTopic(),status,lag, groupTopicConsumerState.getConsumerMethod());
        putToExpireCacheMap(key,consumerLagCache);
        return consumerLagCache;
    }


    public ConsumerGroupState lagStatus(ConsumerGroupState consumerGroupState,List<PartitionAssignmentState> partitionAssignmentStates,String key,boolean isZk,boolean isSimpleConsumerGroup){
        ConsumerGroupState status = ConsumerGroupState.DEAD;
        String clientId = !CollectionUtils.isEmpty(partitionAssignmentStates)?partitionAssignmentStates.get(0).getClientId():null;
        if(isZk){
            if(clientIdNonNull(clientId)){
                status = ConsumerGroupState.STABLE;
            }
        }else{
            if(isSimpleConsumerGroup){
                HomeCache.ConsumerLagCache consumerLagCache  = HomeCache.consumerLagCacheMap.getOrDefault(key,null);
                long offset =  partitionAssignmentStates.stream().mapToLong(PartitionAssignmentState::getOffset).sum();
                if(!Objects.isNull(consumerLagCache)){
                    long cacheOffset = consumerLagCache.getOffset();
                    if(offset!=cacheOffset){
                        status = ConsumerGroupState.STABLE;
                    }
                }else{
                    status = ConsumerGroupState.UNKNOWN;
                }

            }else{
                if(consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.STABLE.name())){
                    status = clientIdNonNull(clientId)?ConsumerGroupState.STABLE:ConsumerGroupState.DEAD;
                }else if(consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.DEAD.name()) ||
                        consumerGroupState.name().equalsIgnoreCase(ConsumerGroupState.EMPTY.name())){
                    status = ConsumerGroupState.DEAD;
                }else{
                    status = clientIdNonNull(clientId)? ConsumerGroupState.STABLE:ConsumerGroupState.UNKNOWN;
                }
            }
        }
        return status;
    }


    public String generateKey(String clusterId,String consumerGroup,String topic,String method){
        return clusterId+Constants.Symbol.Vertical_STR+topic+ Constants.Symbol.Vertical_STR+consumerGroup+Constants.Symbol.Vertical_STR+method;
    }

    private void putToExpireCacheMap(String key,Object value){
        ExpireMap.put(key,value);
    }

    private boolean clientIdNonNull(String str){
        return (!Objects.isNull(str) && !Objects.equals("",str));
    }

    public List<Object> parseResult(Object brokerValue,Object zkValue){
        List<Object> result = new ArrayList<>();
        if(!Objects.equals("",brokerValue) && !Objects.isNull(brokerValue)){
            result.add(brokerValue);
        }
        if(!Objects.equals("",zkValue) && !Objects.isNull(zkValue)){
            result.add(zkValue);
        }
        return result;
    }
}
