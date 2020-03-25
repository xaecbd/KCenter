package org.nesc.ec.bigdata.controller;

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


	@GetMapping("/get")
	@ResponseBody
	public RestResponse getClusterById(@RequestParam Long id) {
		try {
			ClusterInfo cluster = clusterService.selectById(id);
			return SUCCESS_DATA(cluster);
		} catch (Exception e) {
			LOG.error("Find cluster by Id error.", e);
			return ERROR("GET CLUSTER DATA BY ID FAILD!");
		}
	}

	@GetMapping("")
	@ResponseBody
	public RestResponse getCluster() {
		try {
			List<ClusterInfo> clusters = clusterService.getTotalData();
			return SUCCESS_DATA(clusters);
		} catch (Exception e) {
			LOG.error("Find cluster List error.", e);
			return ERROR("GET DATA FAILD!");
		}

	}


	@PostMapping("/add")
	@ResponseBody
	public RestResponse add(@RequestBody ClusterInfo cluster) {
		try {
			if (!clusterService.clusterExits(cluster,false)) {
				if (clusterService.insert(cluster)) {
					return SUCCESS("ADD CLUSTER DATA SUCCESS");
				} else {
					return ERROR("ADD CLUSTER DATA FAILD,PLEASE MAKE SURE YOUR ZK AND BROKER ADDRESS IS VALIDATE!");
				}
			} else {
				return ERROR("THIS CLUSTER ALREADY EXITS!");
			}
		} catch (Exception e) {
			LOG.error("add cluster error.", e);
			return ERROR("ADD CLUSTER DATA FAILD!");
		}
	}

	@PutMapping("update")
	@ResponseBody
	public RestResponse update(@RequestBody ClusterInfo cluster) {
		try {
			if (!clusterService.clusterExits(cluster,true)) {
				if (clusterService.update(cluster)) {
					return SUCCESS("UPDATE CLUSTER DATA SUCCESS");
				} else {
					return ERROR("UPDATE CLUSTER DATA FAILD!");
				}
			} else {
				return ERROR("THIS CLUSTER ALREADY EXITS!");
			}
		} catch (Exception e) {
			LOG.error("update cluster error.", e);
			return ERROR("UPDATE CLUSTER DATA FAILD!");
		}
	}

	@DeleteMapping("/{id}")
	@ResponseBody
	public RestResponse delete(@PathVariable Long id) {
		try {
			if (clusterService.deleteAssociatTable(id)) {
				if(clusterService.delete(id)) {
					return SUCCESS("DELETE CLUSTER DATA SUCCESS");
				}else {
					return ERROR("DELETE CLUSTER DATA FAILD!");
				}
			} else {
				return ERROR("DELETE CLUSTER DATA FAILD,MAY BE DELETE ASSOCIAT TABLE DATA FAILD!");
			}
		} catch (Exception e) {
			LOG.error("delete cluster error.", e);
			return ERROR("DELETE CLUSTER DATA FAILD!");
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
			return ERROR("GET DATA FAILD!");
		}
	}
}
