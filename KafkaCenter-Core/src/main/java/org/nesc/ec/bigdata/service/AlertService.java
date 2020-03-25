package org.nesc.ec.bigdata.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nesc.ec.bigdata.common.model.OffsetInfo;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.ClusterGroup;
import org.nesc.ec.bigdata.model.EmailEntity;
import org.nesc.ec.bigdata.model.MonitorNoticeInfo;
import org.nesc.ec.bigdata.model.vo.AlertMailDataVo;

import org.nesc.ec.bigdata.mapper.AlertMapper;
import org.nesc.ec.bigdata.model.AlertGoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author jc1e
 *
 */
@Service
public class AlertService {

	@Autowired
	AlertMapper alertMapper;
	
	@Autowired
	DBLogService dbLogService;
	
	@Autowired
	InitConfig initConfig;
	


	public boolean delete(Long id) {
		dbLogService.dbLog("delete alert_group by id:"+id);
		return checkResult(alertMapper.deleteById(id));
	}

	public void deleteAlertByClusterGroup(ClusterGroup clusterGroup){
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.KeyStr.CLUSTER_ID, clusterGroup.getClusterID());
		map.put("consummer_group", clusterGroup.getConsummerGroup());
		List<AlertGoup> alertGoups = alertMapper.selectByMap(map);
		if (!alertGoups.isEmpty()){
			alertMapper.deleteByMap(map);
		}
	}
	
	private boolean checkResult(Integer result) {
		return result > 0;
	}
	
	public boolean exites(AlertGoup alertGoup) {
		return alertMapper.exites(alertGoup.getClusterId(), alertGoup.getTopicName(), alertGoup.getConsummerGroup()) > 0;
	}
	
	public boolean insert(AlertGoup alertGoup) {
	    alertGoup.setCreateDate(new Date());
	    alertGoup.setEnable(true);
		dbLogService.dbLog("insert alert_group-"+alertGoup.getTopicName()+":"+alertGoup.getConsummerGroup());
		return checkResult(alertMapper.insert(alertGoup));
	}
	
	
	public boolean update(AlertGoup alertGoup) {
		dbLogService.dbLog("Update alert_group by id:"+alertGoup.getId());
		return checkResult(alertMapper.updateById(alertGoup));
	}
	
	public List<AlertGoup> getAlertGoups() {
		return alertMapper.getAllGroups();
	}	
	
	boolean deleteByClusterId(Long clusterId) {
		Map<String, Object> map = new HashMap<>();
		map.put(Constants.KeyStr.CLUSTER_ID, clusterId);
		if(selectByClusterId(map)) {
			return alertMapper.deleteByMap(map) > 0;
		}
		return true;
		
	}

	public  boolean updateTaskEnable(Map<String,Object> map){
		return checkResult(alertMapper.updateEnable(map));
	}

	public  int countData(){
		return alertMapper.countData();
	}

	private boolean selectByClusterId(Map<String, Object> map) {
    	return !alertMapper.selectByMap(map).isEmpty();
    }

	public List<AlertGoup> selectAllByClusterId(String clusterId) {
		return alertMapper.getAllGroupsByCluster(clusterId);
	}
	
	public Map<String, Object> getEmailAllMessage(MonitorNoticeInfo monitorNoticeInfo) {
		// Return Data
		Map<String, Object> mailMap = new HashMap<>();
		AlertGoup alertGroup = monitorNoticeInfo.getAlertGoup();
		EmailEntity emailEntity = new EmailEntity();
		
		emailEntity.setEmailTo(alertGroup.getMailTo());
		emailEntity.setEmailFrom(initConfig.getEmailFrom());
		emailEntity.setEmailSubject(alertGroup.getCluster().getName() + " - " + alertGroup.getConsummerGroup() + "|"+ alertGroup.getTopicName());
		
		List<OffsetInfo> offsetInfos = new ArrayList<>();
		long lagCount =0L;
		for (OffsetInfo offsetInfo : monitorNoticeInfo.getOffsetInfos()) {
			lagCount = lagCount+offsetInfo.getLag();
			offsetInfos.add(offsetInfo);
		}
		AlertMailDataVo alertMailDataVo = new AlertMailDataVo();
		alertMailDataVo.setClusterId(alertGroup.getCluster().getId());
		alertMailDataVo.setGroup(alertGroup.getConsummerGroup());
		alertMailDataVo.setLagCount(lagCount);
		alertMailDataVo.setTopicName(alertGroup.getTopicName());
		alertMailDataVo.setOffsets(offsetInfos);
		alertMailDataVo.setPublicURL(initConfig.getApproveURL());
		Map<String, Object> alertMap = new HashMap<>();
		alertMap.put("alertInfo", alertMailDataVo);
		
		mailMap.put("emailEntity", emailEntity);
		mailMap.put("emailContent", alertMap);
		return mailMap;
	}

	public AlertGoup get(AlertGoup alertGoup) {
		Map<String, Object> map = new HashMap<>();
		map.put("cluster_id", alertGoup.getClusterId());
		map.put("topic_name", alertGoup.getTopicName());
		map.put("consummer_group", alertGoup.getConsummerGroup());
		List<AlertGoup> alerts = alertMapper.selectByMap(map);
		if(!alerts.isEmpty()) {
			return alerts.get(0);
		} else {
			return alertGoup;
		}
	}


}
