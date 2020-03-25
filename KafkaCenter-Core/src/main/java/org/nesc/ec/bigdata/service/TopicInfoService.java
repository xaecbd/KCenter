package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.util.KafkaAdmins;
import org.nesc.ec.bigdata.common.util.TimeUtil;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;
import org.nesc.ec.bigdata.mapper.TopicInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.TaskInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * @author Lola.L.Gou
 */
@Service
public class TopicInfoService {

	@Autowired
	TopicInfoMapper topicInfoMapper;
	@Autowired
	ClusterInfoMapper clusterInfoMapper;
	@Autowired
	KafkaAdminService kafkaAdminService;
	@Autowired
	DBLogService dbLogService;
	private static final Logger LOG = LoggerFactory.getLogger(TopicInfoService.class);

	public boolean insert(TopicInfo topic) {
		topic.setCreateTime(new Date());
		Integer result =  topicInfoMapper.insert(topic);
		return checkResult(result);
	}

	public TopicInfo selectById(Long id) {
		return topicInfoMapper.getTopicById(id);
	}

	public List<TopicInfo> getTotalData(){
		return topicInfoMapper.getTopics();
	}

	public boolean update(TopicInfo topic) {
		Integer result = topicInfoMapper.updateById(topic);
		return checkResult(result);
	}

	public boolean delete(Long id) {
		Integer result = topicInfoMapper.deleteById(id);
		dbLogService.dbLog("delete topic by id:"+id);
		return checkResult(result);
	}


	boolean isDelete(String topicName, String clusterId) {
		Map<String,Object> map = new HashMap<>();
		map.put(Constants.KeyStr.TOPIC_NAME, topicName);
		map.put(Constants.KeyStr.CLUSTER_ID, clusterId);
		return checkResult(topicInfoMapper.deleteByMap(map));
	}
	private boolean checkResult(Integer result) {
		return result > 0;
	}

	public Boolean adminCreateTopic(TopicInfo topicInfo, UserInfo userInfo) throws ExecutionException, InterruptedException {
		boolean flag = true;
		try {
			if(KafkaAdminCreateTopic(topicInfo)){
				TopicInfo topicInfo1 = topicInfo;
				topicInfo1.setTtl(TimeUtil.hoursTransToMiss(topicInfo.getTtl().intValue()));
				if (!"admin".equalsIgnoreCase(userInfo.getName())){
					topicInfo1.setOwnerId(userInfo.getId());
					if (userInfo.getTeamIDs() !=null){
						topicInfo1.setTeamId(userInfo.getTeamIDs().get(0));
					}
				}
				if(!this.insert(topicInfo1)) {
					flag=false;
				}
			}
			return flag;
		}catch (Exception e){
			flag = false;
			Map<String,Object> paramMap = new HashMap<>();
			KafkaAdmins kafkaAdmis = kafkaAdminService.getKafkaAdmins(topicInfo.getClusterId());
			if(kafkaAdmis.checkExists(topicInfo.getTopicName())) {
				kafkaAdmis.delete(topicInfo.getTopicName());
			}
			paramMap.put(Constants.KeyStr.TOPIC_NAME, topicInfo.getTopicName());
			paramMap.put(Constants.KeyStr.CLUSTER_ID, topicInfo.getClusterId());
			topicInfoMapper.deleteByMap(paramMap);
		}
		return flag;
	}

	public JSONObject encapsulatedObject(TaskInfo task) throws Exception {
		JSONObject obj = new JSONObject();
		String[] clusterIds = task.getClusterIds().split(Constants.Symbol.COMMA);
		if(clusterIds.length>0){
			boolean flag = true;
			for(String clusterId : clusterIds) {
				if(!"".equals(clusterId)) {
					try {
						if(createTopic(task, clusterId)) {
							TopicInfo topic = new TopicInfo();
							topic.setClusterId(clusterId);
							topic.setComments(task.getComments());
							if(task.getOwner() != null){
								topic.setOwnerId(task.getOwner().getId());
							}
							topic.setPartition(task.getPartition());
							topic.setReplication(task.getReplication());
							if(task.getTeam() != null){
								topic.setTeamId(task.getTeam().getId());
							}
							topic.setTopicName(task.getTopicName());
							topic.setTtl(TimeUtil.hoursTransToMiss(task.getTtl()));
							if(!this.insert(topic)) {
								flag=false;
							}
						}
					} catch (Exception e) {
						ClusterInfo cluster = clusterInfoMapper.queryById(Long.parseLong(clusterId));
						int index = e.getMessage().indexOf(Constants.Symbol.COLON);
						backCreateTopic(task);
						obj.put(Constants.TRUE, false);
						obj.put(Constants.KeyStr.MESSAGE, "cluster: "+cluster.getName()+"  create topic has error:"+e.getMessage().substring(index+1, e.getMessage().length()));
						return obj;
					}
				}
			}
			obj.put(Constants.TRUE, flag);
			obj.put(Constants.KeyStr.MESSAGE, Constants.SUCCESS);
		}
		return obj;

	}

	private void backCreateTopic(TaskInfo task) {
		try {
			String[] clusterIds = task.getClusterIds().split(Constants.Symbol.COMMA);
			for(String clusterId:clusterIds) {
				KafkaAdmins kafkaAdmis = kafkaAdminService.getKafkaAdmins(clusterId);
				if(kafkaAdmis.checkExists(task.getTopicName())) {
					kafkaAdmis.delete(task.getTopicName());
				}
			}
			Map<String,Object> paramMap = new HashMap<String, Object>();
			paramMap.put(Constants.KeyStr.TOPIC_NAME, task.getTopicName());
			topicInfoMapper.deleteByMap(paramMap);
		} catch (Exception e) {	}

	}



	private Boolean createTopic(TaskInfo task, String clusterId) throws Exception {
		if(!kafkaAdminService.getKafkaAdmins(clusterId).createTopicIfNotExists(task.getTopicName(), task.getPartition(),task.getReplication(), (task.getTtl() * 3600 * 1000))) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;

	}
	public Boolean KafkaAdminCreateTopic(TopicInfo topicInfo) throws Exception {
		if(!kafkaAdminService.getKafkaAdmins(topicInfo.getClusterId()).createTopicIfNotExists(topicInfo.getTopicName(), topicInfo.getPartition(),topicInfo.getReplication(), (topicInfo.getTtl().intValue() * 3600 * 1000))) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public TopicInfo getTopicsByName(String topic,Long clientId) {
		return topicInfoMapper.getTopicsByTopicName(topic, clientId);
	}
	public List<TopicInfo> getTopicByTeamIDs(List<Long> teamIDs) {
		return topicInfoMapper.getTopicsByTeamIDs(teamIDs);
	}

	public boolean exitsTopic(String topic,Long clientId) {
		Map<String,Object> columnMap = new HashMap<>();
		columnMap.put(Constants.KeyStr.TOPIC_NAME, topic);
		columnMap.put(Constants.KeyStr.CLUSTER_ID, clientId);
		List<TopicInfo> topicList = topicInfoMapper.selectByMap(columnMap);
		return !topicList.isEmpty();
	}
	boolean deleteByClusterId(Long id) {
		Map<String,Object> map = new HashMap<>();
		map.put(Constants.KeyStr.CLUSTER_ID, id);
		if(selectByClusterId(map)) {
			return topicInfoMapper.deleteByMap(map) > 0;
		}
		return true;
	}

	private boolean selectByClusterId(Map<String, Object> map) {
		return !topicInfoMapper.selectByMap(map).isEmpty();
	}

	public List<TopicInfo> selectAllByClusterId(String clusterId) {
		return topicInfoMapper.getTopicsByCluster(clusterId);
	}

	public List<TopicInfo> selectTopicsByClusterId(String clusterId){
		return topicInfoMapper.selectTopicsByCluster(clusterId);
	}


	public boolean batchUpdate(Set<TopicInfo> topics) {
		int flagCount = 0;
		for(TopicInfo info:topics) {
			int update = topicInfoMapper.updateById(info);
			if(checkResult(update)){
				flagCount = flagCount+1;
			}
		}
		return flagCount == topics.size();
	}

	public boolean batchInsert(Set<TopicInfo> topics) {
		int flagCount = 0;
		for(TopicInfo info:topics) {
			int update = topicInfoMapper.insert(info);
			if(checkResult(update)){
				flagCount = flagCount+1;
			}
		}
		return flagCount == topics.size();
	}


	public boolean deleteByIds(Set<Long> ids) {
		int flagCount = 0;
		for (Long id:ids){
			int i = topicInfoMapper.deleteById(id);
			if(checkResult(i)){
				flagCount = flagCount+1;
			}
		}
		return flagCount == ids.size();	}
}
