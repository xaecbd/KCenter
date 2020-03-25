package org.nesc.ec.bigdata.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.mapper.TeamInfoMapper;
import org.nesc.ec.bigdata.mapper.TeamUserMapper;
import org.nesc.ec.bigdata.mapper.TopicInfoMapper;
import org.nesc.ec.bigdata.model.TeamInfo;
import org.nesc.ec.bigdata.model.TeamUser;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamInfoService {

	private static final String TEAM_INFO_TABLE_NAME = "name";
	private static final String TEAM_TABLE_TEAM_ID = "id";
	
	private static final String USER_TEAM_TABLE_TEAMID = "team_id";
	@Autowired
	TeamInfoMapper teamInfoMapper;
	@Autowired
	TopicInfoMapper topicMapper;
	@Autowired 
	TeamUserMapper teamUserMapper;
	
	@Autowired
	DBLogService dbLogService;

	public boolean insert(TeamInfo team) {
		Integer result =  teamInfoMapper.insert(team);
		return checkResult(result);
	}

	public TeamInfo selectById(Long id) {
		return teamInfoMapper.selectById(id);
	}

	public List<TeamInfo> getTotalData(){
		return teamInfoMapper.selectList(null);
	}

	public boolean update(TeamInfo team) {
		Integer result = teamInfoMapper.updateById(team);
		return checkResult(result);
	}

	public boolean delete(Long id) {
		Integer result = teamInfoMapper.deleteById(id);
		dbLogService.dbLog("delete team by id:"+id);
		return checkResult(result);
	}

	public boolean checkResult(Integer result) {
		return result > 0;
	}

	/**
	 * verify teamName is already exist
	 * @param name teamName
	 * @return if exist return true else false
	 */
	public boolean teamNameExist(String name) {
		List<TeamInfo> teamInfoList = getTeamInfosByTeamName(name);
		if (teamInfoList == null || teamInfoList.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * query team info by teamName
	 * @param teamName teamName
	 * @return List<TeamInfo>
	 */
	public List<TeamInfo> getTeamInfosByTeamName(String teamName) {
		Map<String, Object> queryMap = new HashMap<>(2);
		queryMap.put(TEAM_INFO_TABLE_NAME, teamName);
		return getTeamInfosByQueryMap(queryMap);
	}

	/**
	 * query team info by query Map
	 * @param queryMap queryMap
	 * @return List<TeamInfo>
	 */
	private List<TeamInfo> getTeamInfosByQueryMap(Map<String, Object> queryMap) {
		return teamInfoMapper.selectByMap(queryMap);
	}

	/**
	 * batch query team info by teamids
	 * @param teamIds teamIds
	 * @return List
	 */
	public List<TeamInfo> getTeamInfoByTeamIds(List<Long> teamIds) {
		return teamInfoMapper.selectBatchIds(teamIds);
	}

	/**
	 * 将数据库中的数据转换为页面需要的格式
	 * @param teamInfos teamInfos
	 * @return List<JSONObject>
	 */
	public List<JSONObject> transformToSelectData(List<TeamInfo> teamInfos) {
		List<JSONObject> result = new ArrayList<>();
		JSONObject object;
		for (TeamInfo teamInfo : teamInfos) {
			object = new JSONObject(3);
			object.put(Constants.JsonObject.VALUE, String.valueOf(teamInfo.getId()));
			object.put(Constants.JsonObject.LABEL, teamInfo.getName());
			result.add(object);
		}
		return result;
	}

	public List<TeamInfo> getTeamsByTeamIDs(List<Long> teamIDs) {
    	EntityWrapper<TeamInfo> queryWrapper = new EntityWrapper<>();
    	if (teamIDs == null || teamIDs.isEmpty()){
			List<TeamInfo> teamInfos= null;
    		return teamInfos;

		}else {
			queryWrapper.in(TEAM_TABLE_TEAM_ID, teamIDs);
			return teamInfoMapper.selectList(queryWrapper);
		}
	}

	public boolean isRelatedTopic(Long teamId) {
		// 先根据teamId在user_team表中查询出与之关联的users
		Map<String, Object> queryMap = new HashMap<>(2);
		queryMap.put(USER_TEAM_TABLE_TEAMID, teamId);
		List<TeamUser> users = teamUserMapper.selectByMap(queryMap);
		if(users.isEmpty()) {
			return false;
		}
		List<Long> userIds = new ArrayList<>();
		users.forEach(user-> userIds.add(user.getUserId()));
		// 用上一步查出来的users去topicInfo表中去匹配所有topic，只要topic数大于0就不能删这个team
		List<TopicInfo> topicsInfos = topicMapper.getTopicsByUserIDs(userIds);
		return topicsInfos.size() > 0;
	}
}
