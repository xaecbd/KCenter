package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONArray;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.BrokerConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.nesc.ec.bigdata.model.ClusterInfo;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.model.vo.AlterVo;
import org.nesc.ec.bigdata.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author jc1e
 *
 */
@RestController
@RequestMapping("monitor/alert")
public class AlertController extends BaseController {

	private static final Logger LOG = LoggerFactory.getLogger(AlertController.class);

	@Autowired
	AlertService alertService;

	@Autowired
	MonitorService monitorService;

	@Autowired
	ClusterService clusterService;

	@Autowired
	TeamUserService teamUserService;

	@Autowired
	InitConfig config;

	@Autowired
	RestService restService;

	@Autowired
	HttpServletRequest request;

	@Autowired
	InitConfig initConfig;
	
	/**
	 *	根据增加或修改alert表单数据查询alert
	 * @param alertGoup
	 * @return
	 */
	@PostMapping(value = "/getalert")
	public RestResponse isExsitAlert(@RequestBody AlertGoup alertGoup) {
		AlertGoup alert = alertService.get(alertGoup);
		return SUCCESS_DATA(alert);
	}
	/**
	 * 为某个topic添加消费组
	 * @param alertMap
	 * @return restresponse
	 */
	@PostMapping(value = "/add")
	public RestResponse add(@RequestBody AlertGoup alertMap) {
		try {
			UserInfo user = getCurrentUser();
			if(initConfig.getAdminname().equalsIgnoreCase(user.getName())){
				user.setId(0L);
			}
			if(alertMap.getId()!=null) {
				if(alertService.update(alertMap)) {
					return SUCCESS("Update Alter Success");
				}
			}else {
				alertMap.setOwnerId(user.getId());
				if(!alertService.exites(alertMap)) {
					if(alertService.insert(alertMap)) {
						return SUCCESS("Add Alter Success");
					}
				}else {
					return ERROR("Alter Always Exits!");
				}

			}

		} catch (Exception e) {
			LOG.error("Add Alter Errors.message is:",e);
			return ERROR("Add Alter Errors!");
		}
		return ERROR("Add Alter Errors!");

	}

	/** delete alert by id*/
	@DeleteMapping(value = "/delete/{id}")
	public RestResponse delete(@PathVariable String id) {
		try {
			if(alertService.delete(Long.parseLong(id))) {
				return SUCCESS("Delete Alter success!");
			}
		} catch (Exception e) {
			LOG.error("Delete Alter Errors.message is:",e);
			return ERROR("Delete Alter Errors!");
		}
		return ERROR("Delete Alter Errors!");

	}

	/** Update Alert is enabled */
	@PutMapping(value="/update/enable")
	public RestResponse updateEnable(@RequestBody Map<String,Object> map){
		try {
			if(alertService.updateTaskEnable(map)) {
				return SUCCESS("update task enable success!");
			}
		} catch (Exception e) {
			LOG.error("update task enable errors.message is:",e);
			return ERROR("update task enable errors!");
		}
		return ERROR("update task enable errors!");
	}

	/** Get the full alert according to ClusterID */
	@GetMapping(value = "")
	public RestResponse getALL(@RequestParam("cluster") String clusterId) {
		try {
			List<AlertGoup> results = "-1".equalsIgnoreCase(clusterId)?
					alertService.getAlertGroups():alertService.selectAllByClusterId(clusterId);
			List<AlterVo> result = new ArrayList<>();
			results.forEach(alter->{
				if(alter.getCluster()!=null || alter.getClusterId()!=null) {
					AlterVo alterVo = new AlterVo();
					alterVo.setId(alter.getId());
					alterVo.setClusterId(alter.getCluster()==null?Long.parseLong(String.valueOf(alter.getClusterId())):alter.getCluster().getId());
					alterVo.setConsummerApi(alter.getConsummerApi());
					alterVo.setConsummerGroup(alter.getConsummerGroup());
					alterVo.setTopicName(alter.getTopicName());
					alterVo.setThreshold(alter.getThreshold());
					alterVo.setMailTo(alter.getMailTo());
					alterVo.setWebhook(alter.getWebhook());
					alterVo.setDispause(alter.getDispause());
					alterVo.setClusterName(alter.getCluster()==null?"":alter.getCluster().getName());
					alterVo.setCreateTime(alter.getCreateDate());
					alterVo.setDisableAlerta(alter.isDisableAlerta());
					alterVo.setEnable(alter.isEnable());
					if(alter.getOwner()==null) {
						alterVo.setOwner(Constants.Role.ADMIN);
					}else {
						alterVo.setOwner(Optional.ofNullable(alter.getOwner().getName()).orElse(""));
						alterVo.setOwnerId(Optional.ofNullable(alter.getOwner().getId()).orElse(-1L));

					}
					result.add(alterVo);
				}
			});
			result.sort((AlterVo o1, AlterVo o2)-> {
				long o2Time = o2.getCreateTime().getTime();
				long o1Time = o1.getCreateTime().getTime();
				return o2Time>o1Time?1:-1;
			});
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOG.error("Get Alter Errors.message is:",e);
			return ERROR("Get Alter Data Errors!");
		}
	}

	@GetMapping("/topic/{clusterId}")
	public RestResponse getTopicByCluster(@PathVariable String clusterId) {
		return SUCCESS_DATA(monitorService.getTopicList(clusterId));
	}

	/** Get the consumer group according to clusterId and topic name*/
	@PostMapping("/group")
	public RestResponse getGroupByCluster(@RequestBody Map<String, String> queryMap) {
		String clusterID = queryMap.get(Constants.KeyStr.CLUSTERID);
		String topic = queryMap.get(BrokerConfig.TOPIC);
		try {
			Map<String,String> remoteHosts = config.getRemoteHostsMap();
			ClusterInfo cluster = clusterService.selectById(Long.parseLong(clusterID));
			if(config.isRemoteQueryEnable() && remoteHosts.containsKey(cluster.getLocation().toLowerCase())) {
				String url = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()
						+request.getServletPath().replace("/monitor", "/remote");
				JSONArray array = restService.queryRemoteQuery(url, queryMap);
				return SUCCESS_DATA(array);
			}else {
				return SUCCESS_DATA(monitorService.listGroups(topic, clusterID));
			}
		} catch (Exception e) {
			LOG.error("List Group Errors.message is:",e);
		}
		return ERROR("List Group Errors,Please Check");
	}

}
