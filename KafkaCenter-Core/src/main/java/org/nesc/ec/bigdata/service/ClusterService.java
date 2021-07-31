package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.nesc.ec.bigdata.common.model.BrokerInfo;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.ClusterInfoMapper;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.vo.TaskClusterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ClusterService {


	@Autowired
	ClusterInfoMapper clusterInfoMapper;
	@Autowired
	KafkaAdminService kafkaAdminService;
	
	@Autowired 
	TopicInfoService topicInfoService;
	@Autowired
	TaskInfoService taskInfoService;
	@Autowired
	AlertService alterService;
	@Autowired
	ZKService zkService;
	@Autowired
	CollectionService collectionService;
	
	@Autowired
	InitConfig initConfig;

	@Autowired
	DBLogService dbLogService;

	public boolean insert(ClusterInfo cluster) {
		Integer result = 0;
		try {
			if(zkService.checkServiceHealth(cluster.getZkAddress()) && kafkaAdminService.kafkaIsHeath(cluster.getBroker())) {
				cluster.setCreateTime(new Date());
				 result =  clusterInfoMapper.insert(cluster);
				dbLogService.dbLog("insert cluster by id:"+cluster.getId());
				return checkResult(result);
			}
		} catch (Exception e) {
			dbLogService.dbLog("insert cluster by id:"+cluster.getId()+"has error:"+e);
		}
		return false;
		
		
	}

	public ClusterInfo selectById(Long id) {
		return clusterInfoMapper.selectById(id);
	}

	public List<ClusterInfo> getTotalData(){
		return clusterInfoMapper.selectList(null);
	}

	/**return cluster info by db,and cluster status by kafka cluster*/
	public JSONObject getClusterAndStatus(ClusterInfo cluster){
		JSONObject result = new JSONObject();
		try {
			DescribeClusterResult describeClusterResult = kafkaAdminService
					.getKafkaAdmins(String.valueOf(cluster.getId())).descCluster();
			int brokers = describeClusterResult.nodes().get().size();
			if(brokers==0) {result.put(Constants.Status.STATUS, Constants.Status.BAD);}
			if(cluster.isEnable()) {
				if(cluster.getBrokerSize()>brokers) {
					result.put(Constants.Status.STATUS, Constants.Status.WARN);
				}else if(cluster.getBrokerSize()==brokers) {
					result.put(Constants.Status.STATUS, Constants.Status.OK);
				}
			}else {
				result.put(Constants.Status.STATUS, Constants.Status.OK);
			}
		} catch (Exception e) {
			return result;
		}
		return result;
	}
	public boolean update(ClusterInfo cluster) {
		int result;
		try {
			kafkaAdminService.updateKafkaAdminByClusterID(cluster.getId().toString());
			zkService.updateZKAdminByClusterID(cluster.getId().toString());
			result = clusterInfoMapper.updateById(cluster);
			dbLogService.dbLog("update cluster by id:"+cluster.getId());
		} catch (Exception e) {
			dbLogService.dbLog("update cluster by id:"+cluster.getId()+"has error:"+e);
           return false;
		}
		
		return checkResult(result);
	}

	public boolean delete(Long id) {
		Integer result = clusterInfoMapper.deleteById(id);
		dbLogService.dbLog("delete cluster by id:"+id);
		return checkResult(result);		
	}

	public boolean clusterExits(ClusterInfo cluster, boolean isUpdate) {
	try{
		List<ClusterInfo> clusterInfos = clusterInfoMapper.selectBrokers();
		// 如果是修改，先把自身移除
		if(isUpdate) {
			clusterInfos.removeIf(clusterInfo -> clusterInfo.getId().equals(cluster.getId()));
		}
		// 先校验name
		for(ClusterInfo clusterInfo:clusterInfos) {
			if(clusterInfo.getName().equals(cluster.getName())) {
				return true;
			}
		}
		// 再校验zk/broker
		Set<String> brosSet = new HashSet<>(Arrays.asList(cluster.getBroker().split(Constants.Symbol.COMMA)));
		for (ClusterInfo clusterInfo:clusterInfos){
			List<BrokerInfo> brokerInfos = zkService.getZK(clusterInfo.getId().toString()).getBrokers();
			Set<String> brokers = new HashSet<>();
			brokerInfos.forEach(brokerInfo -> {
				String broks = brokerInfo.getHost()+ Constants.Symbol.COLON+brokerInfo.getPort();
				brokers.add(broks);
			});
			Set<String> result = intersection(brokers,brosSet);
			if(!result.isEmpty()){
				return true;
			}
		}
	}catch (Exception e){
		return false;
	}
	return false;
}
		

	
	public Set<String> intersection(Set<String> s1,Set<String> s2){
		Set<String> result = new HashSet<>();
		result.clear();
		result.addAll(s1);
		result.retainAll(s2);
		return result;		
	}

	private boolean checkResult(Integer result) {
		return result > 0;
	}

	/**
	 * 判断kafka center 是否已经添加cluster
	 * @return if empty return true, else false
	 */
	public boolean clusterInfoIfEmpty() {
		return clusterInfoMapper.selectCount(null) == 0;
	}

	
	public boolean deleteAssociateTable(Long id) {
		boolean topic = topicInfoService.deleteByClusterId(id);
		boolean alter = alterService.deleteByClusterId(id);
		boolean task = taskInfoService.deleteByClusterId(id);
		boolean collect;
		Map<String,Object> map = new HashMap<>();
		map.put(Constants.KeyStr.CLUSTER_ID, id);
		if(collectionService.selectByMap(map)) {
			collect = collectionService.deleteByMap(map);
		}else {
			collect = true;
		}
		return topic && alter && task && collect;
	}

	/**
	 * 根据location查询cluster
	 * 
	 * @param location location
	 * @return all cluster
	 */
	public List<ClusterInfo> getClusterByLocation(String location) {
	//	EntityWrapper<ClusterInfo> queryWrapper = new EntityWrapper<>();
		EntityWrapper<ClusterInfo> queryWrapper = new EntityWrapper<>();
		queryWrapper.in(Constants.KeyStr.LOCATION, location);
		if("ALL".equals(location)) {
			queryWrapper = null;
		}
		return clusterInfoMapper.selectList(queryWrapper);
	}
	
	List<TaskClusterVo> getClusterMessById(String clusterIds) throws InterruptedException, ExecutionException {
        List<TaskClusterVo> clusterMess = new ArrayList<>();
        String[] clusterArr = clusterIds.split(Constants.Symbol.COMMA);
        for(String clusterID : clusterArr) {
        	TaskClusterVo taskClusterVo = new TaskClusterVo();
        	ClusterInfo cluster = selectById(Long.valueOf(clusterID));
    		List<String> brokers = new ArrayList<>();
			brokers.add(cluster.getBroker());
    		taskClusterVo.setClusterName(cluster.getName());
    		taskClusterVo.setClusterVersion(cluster.getKafkaVersion());
    		brokers.sort(String::compareTo);
    		taskClusterVo.setBrokerList(brokers);
    		taskClusterVo.setBrokerSize(brokers.size());
    		clusterMess.add(taskClusterVo);
        }
		return clusterMess;
	}

	public  String getBrokers(String clusterId){
		ClusterInfo cluster =  clusterInfoMapper.selectById(clusterId);
		return cluster.getBroker();
	}
}
