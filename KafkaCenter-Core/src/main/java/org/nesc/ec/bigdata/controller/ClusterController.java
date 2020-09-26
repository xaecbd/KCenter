package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.KafkaAdminService;
import org.nesc.ec.bigdata.service.ZKService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author lg99
 */
@RestController
@RequestMapping("/cluster")
public class ClusterController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(ClusterController.class);

	@Autowired
	ClusterService clusterService;
	@Autowired
	InitConfig config;
	@Autowired
	ZKService zkService;
	@Autowired
	KafkaAdminService kafkaAdminService;

	/** check the zookeeper cluster connection is health*/
	@GetMapping("/validateZKAddress")
	@ResponseBody
	public RestResponse validateZKAddress(@RequestParam("zkAddress") String zkAddress){
		try {
			return SUCCESS_DATA(zkService.checkZKAddressHealth(zkAddress));
		}catch (Exception e){
			LOG.error("validateZKAddress has error",e);
			return ERROR("validateZKAddress failed");
		}
	}

	/** check the kafka cluster connection is health*/
	@GetMapping("/validateKafkaAddress")
	@ResponseBody
	public RestResponse validateKafkaAddress(@RequestParam("kafkaAddress") String kafkaAddress){
		try {
			return SUCCESS_DATA(kafkaAdminService.validateKafkaAddress(kafkaAddress));
		}catch (Exception e){
			LOG.error("validateKafkaAddress has error",e);
			return ERROR("validateKafkaAddress failed");
		}
	}


	/** according the clusterId get the clusterInfo*/
	@GetMapping("/get")
	@ResponseBody
	public RestResponse getClusterById(@RequestParam Long id) {
		try {
			ClusterInfo cluster = clusterService.selectById(id);
			return SUCCESS_DATA(cluster);
		} catch (Exception e) {
			LOG.error("Find cluster by Id error.", e);
			return ERROR("GET CLUSTER DATA BY ID FAILED!");
		}
	}

	/**select the all data from the cluster table*/
	@GetMapping("")
	@ResponseBody
	public RestResponse getCluster() {
		try {
			List<ClusterInfo> clusters = clusterService.getTotalData();
			return SUCCESS_DATA(clusters);
		} catch (Exception e) {
			LOG.error("Find cluster List error.", e);
			return ERROR("GET DATA FAILED!");
		}

	}

	/**return cluster total data and cluster status*/
	@PostMapping("/status")
	@ResponseBody
	public RestResponse clusterStatus(@RequestBody  ClusterInfo clusterInfo){
		try {
			JSONObject clusters = clusterService.getClusterAndStatus(clusterInfo);
			return SUCCESS_DATA(clusters);
		} catch (Exception e) {
			LOG.error("Find cluster status List error.", e);
			return ERROR("GET DATA FAILED!");
		}
	}

	/**add the cluster info to cluster table
	 * 1. check the cluster is exits,if exits,return error
	 * 2. else insert to cluster table
	 * */
	@PostMapping("/add")
	@ResponseBody
	public RestResponse add(@RequestBody ClusterInfo cluster) {
		try {
			if (!clusterService.clusterExits(cluster,false)) {
				if (clusterService.insert(cluster)) {
					return SUCCESS("ADD CLUSTER DATA SUCCESS");
				} else {
					return ERROR("ADD CLUSTER DATA FAILED,PLEASE MAKE SURE YOUR ZK AND BROKER ADDRESS IS VALIDATE!");
				}
			} else {
				return ERROR("THIS CLUSTER ALREADY EXITS!");
			}
		} catch (Exception e) {
			LOG.error("add cluster error.", e);
			return ERROR("ADD CLUSTER DATA FAILED!");
		}
	}

	/** update the cluster info to cluster table
	 * 1.check the cluster is exits,if exits,return error
	 * 2.else update the cluster table
	 * */
	@PutMapping("update")
	@ResponseBody
	public RestResponse update(@RequestBody ClusterInfo cluster) {
		try {
			if (!clusterService.clusterExits(cluster,true)) {
				if (clusterService.update(cluster)) {
					return SUCCESS("UPDATE CLUSTER DATA SUCCESS");
				} else {
					return ERROR("UPDATE CLUSTER DATA FAILED!");
				}
			} else {
				return ERROR("THIS CLUSTER ALREADY EXITS!");
			}
		} catch (Exception e) {
			LOG.error("update cluster error.", e);
			return ERROR("UPDATE CLUSTER DATA FAILED!");
		}
	}

	/**
	 * delete the clusterInfo by clusterId
	 * 1.delete all tables associated with Cluster,such as topicInfo,alertInfo,taskInfo tables,
	 * if success,delete the clusterInfo from cluster table
	 * 2.if failed,return error
	 * */
	@DeleteMapping("/{id}")
	@ResponseBody
	public RestResponse delete(@PathVariable Long id) {
		try {
			if (clusterService.deleteAssociateTable(id)) {
				if(clusterService.delete(id)) {
					return SUCCESS("DELETE CLUSTER DATA SUCCESS");
				}else {
					return ERROR("DELETE CLUSTER DATA FAILED!");
				}
			} else {
				return ERROR("DELETE CLUSTER DATA FAILED,MAY BE DELETE ASSOCIATED TABLE DATA FAILED!");
			}
		} catch (Exception e) {
			LOG.error("delete cluster error.", e);
			return ERROR("DELETE CLUSTER DATA FAILED!");
		}

	}


	/**
	 * 根据location查询集群信息
	 *
	 * @param location
	 * @return
	 */
	@GetMapping("by_location/{location}")
	@ResponseBody
	public RestResponse getClusterByLocation(@PathVariable String location) {
		try {
			List<ClusterInfo> clusters = clusterService.getClusterByLocation(location);
			return SUCCESS_DATA(clusters);
		} catch (Exception e) {
			LOG.error("Find cluster List error.", e);
			return ERROR("GET DATA FAILED!");
		}
	}
}
