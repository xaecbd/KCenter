package org.nesc.ec.bigdata.controller;


import com.alibaba.fastjson.JSONArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.constant.TopicConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.KafkaRecord;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/topic")
public class TopicController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(TopicController.class);
    @Autowired
    TopicInfoService topicInfoService;

    @Autowired
    KafkaConsumersService kafkaConsumersService;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    KafkaAdminService kafkaAdminService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    KafkaManagerService kafkaManagerService;


    @GetMapping("/query/partition")
    public RestResponse getPartition(@RequestParam String clusterId, @RequestParam String topicName) {
        try {
            List<Integer> list = kafkaManagerService.getPartitionByTopic(clusterId, topicName);
            return SUCCESS_DATA(list);
        } catch (Exception e) {
            LOG.error("Get Partition error,", e);
        }
        return ERROR("Get Data Error!Please check");
    }

    @GetMapping("get")
    @ResponseBody
    public RestResponse getTopicById(@RequestParam Long id) {
        try {
            TopicInfo topic = topicInfoService.selectById(id);
            return SUCCESS_DATA(topic);
        } catch (Exception e) {
            LOG.error("Get one topic error.", e);
            return ERROR("GET TOPIC INFORMATION FAILED!");
        }
    }

    @GetMapping("/list")
    @ResponseBody
    public RestResponse getTopics(@RequestParam("cluster") String clusterId) {
        try {
            List<TopicInfo> topics = new ArrayList<>();
            UserInfo user = this.getCurrentUser();
            if (RoleEnum.ADMIN.getDescription().equals(user.getRole().getDescription())) {
                topics = "-1".equalsIgnoreCase(clusterId) ? topicInfoService.getTotalData() : topicInfoService.selectAllByClusterId(clusterId);
            } else {
                List<Long> teamIDs = user.getTeamIDs();
                if (teamIDs != null && teamIDs.size() > 0) {
                    topics = "-1".equalsIgnoreCase(clusterId) ? topicInfoService.getTopicByTeamIDs(teamIDs) :
                            topicInfoService.getTopicByTeamIDs(teamIDs).stream().
                                    filter(topicInfo -> topicInfo.getCluster().getId() == Long.parseLong(clusterId)).collect(Collectors.toList());
                }
            }
            return SUCCESS_DATA(topics);
        } catch (Exception e) {
            LOG.error("Get all topic error.", e);
            return ERROR("GET TOPICS INFORMATION FAILED!");
        }
    }

    @DeleteMapping("delete/{id}")
    @ResponseBody
    public RestResponse delete(@PathVariable Long id) {
        try {
            TopicInfo topic = topicInfoService.selectById(id);
            ClusterInfo clusterInfo = clusterService.selectById(Long.parseLong(topic.getClusterId()));
            Long clusterID = clusterInfo.getId();
            boolean success = kafkaAdminService.getKafkaAdmins(clusterID.toString()).delete(topic.getTopicName());
            if (success && topicInfoService.delete(id)) {
                return SUCCESS("DELETE TOPIC SUCCESS");
            } else {
                return ERROR("DELETE TOPIC FAILED!");
            }
        } catch (Exception e) {
            LOG.error("Delete topic error.", e);
            return ERROR("DELETE TOPIC FAILED!");
        }
    }

    @PostMapping("/list/consumer")
    @ResponseBody
    public RestResponse consumer(@RequestBody Map<String, String> json) {
        try {
            Duration timeOut = Duration.ofMillis(Long.parseLong(json.get(Constants.KeyStr.WAIT_TIME)));
            boolean isCommit = Boolean.parseBoolean(json.get(Constants.KeyStr.IS_COMMIT));
            String clusterId = json.get(Constants.KeyStr.CLUSTERID);
            String groupID = json.get(Constants.KeyStr.GROUP_ID);
            String topicName = json.get(Constants.KeyStr.TOPICNAME);
            int dataSize = Integer.parseInt(json.get(Constants.KeyStr.RECORD_NUM));
            boolean isByPartition = Boolean.parseBoolean(json.get(Constants.KeyStr.ISBY_PARTITION));
            int partition;
            long offset;
            ConsumerRecords<String, String> records;
            if (isByPartition) {
                partition = Integer.parseInt(json.get(TopicConfig.PARTITION));
                offset = Long.parseLong(json.get(TopicConfig.OFFSET));
                records = kafkaConsumersService.consumer(clusterId, groupID, topicName, dataSize, timeOut, isCommit, partition, offset);
            } else {
                records = kafkaConsumersService.consumer(clusterId, groupID, topicName, dataSize, timeOut, isCommit);
            }
            JSONArray array = new JSONArray(dataSize + 1);
            for (ConsumerRecord<String, String> record : records) {
                KafkaRecord rec = new KafkaRecord();
                rec.setPartition(record.partition());
                rec.setOffset(record.offset());
                rec.setKey(record.key());
                rec.setValue(record.value());
                array.add(rec);
            }
            return SUCCESS_DATA(array);
        } catch (Exception e) {
            LOG.error("consumer topic error.", e);
            return ERROR("CONSUMER TOPIC FAILED!");
        }
    }

    @PostMapping("/list/producer")
    @ResponseBody
    public RestResponse producer(@RequestBody Map<String, String> json) {
        try {
            String clusterId = json.get(Constants.KeyStr.CLUSTERID);
            String topicName = json.get(Constants.KeyStr.TOPICNAME);
            String key = json.get(Constants.JsonObject.KEY);
            String value = json.get(Constants.JsonObject.VALUE);
            kafkaProducerService.send(clusterId, topicName, key, value);
            return SUCCESS("PRODUCER TOPIC SUCCESS");
        } catch (Exception e) {
            LOG.error("producer topic error.", e);
            return ERROR("PRODUCER TOPIC FAILED!");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public RestResponse update(@RequestBody TopicInfo topicInfo) {
        try {
            if (topicInfoService.update(topicInfo)) {
                return SUCCESS("Update Topic Success");
            }
        } catch (Exception e) {
            LOG.error("producer topic error.", e);
            return ERROR("Update Topic FAILED!");
        }
        return ERROR("Update Topic FAILED!");
    }

    @PostMapping("/admin_create")
    @ResponseBody
    public RestResponse adminCreate(@RequestBody TopicInfo topicInfo) {
        try {
            UserInfo user = this.getCurrentUser();
            Boolean result = topicInfoService.adminCreateTopic(topicInfo, user);
            if (result) {
                return SUCCESS("Create Topic Success");
            }
        } catch (Exception e) {
            LOG.error("Add task error.", e);
            return ERROR("Create Topic error:" + e.getMessage());
        }
        return ERROR("Create Topic Failed, Pls Check");
    }

}
