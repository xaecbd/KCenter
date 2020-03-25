package org.nesc.ec.bigdata.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.nesc.ec.bigdata.mapper.TeamUserMapper;
import org.nesc.ec.bigdata.model.TeamUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TeamUserService {

    /**
     * USER TABLE USER_ID COLUMN
     */
    private static final String USER_TEAM_TABLE_USER_ID = "user_id";

    /**
     * USER TABLE TEAM_ID COLUMN
     */
    private static final String USER_TEAM_TABLE_TEAM_ID = "team_id";

    @Autowired
    TeamUserMapper teamUserMapper;
    @Autowired
    DBLogService dbLogService;

    public boolean insert(TeamUser teamUser) {
        Integer result =  teamUserMapper.insert(teamUser);
        return checkResult(result);
    }

    public TeamUser selectById(Long id) {
        return teamUserMapper.selectById(id);
    }

    public List<TeamUser> getTotalData(){
        return teamUserMapper.selectList(null);
    }
    
    public List<TeamUser> getTeamUsers(Long userID){
        return teamUserMapper.selectList(new EntityWrapper<TeamUser>().eq("user_id", userID));
    }

    public boolean update(TeamUser teamUser) {
        Integer result = teamUserMapper.updateById(teamUser);
        return checkResult(result);
    }

    public boolean delete(Long id) {
        Integer result = teamUserMapper.deleteById(id);
        dbLogService.dbLog("delete teamUser by id:"+id);
        return checkResult(result);
    }

    private boolean checkResult(Integer result) {
        return result > 0;
    }
    
    public TeamUser getTeamByUserId(Long userId) {
    	return teamUserMapper.getTeamIdByUserId(userId);
    }

    /**
     * get data from table user_team by teamId
     * @param teamId teamId
     * @return teamUser
     */
    public List<TeamUser> getTeamUserByTeamId(Long teamId) {
        Map<String, Object> map = new HashMap<>(3);
        map.put(USER_TEAM_TABLE_TEAM_ID, teamId);
        return getTeamUserByMap(map);
    }

    /**
     * get data from table user_team by userId
     * @param userId userId
     * @return teamUser
     */
    public List<TeamUser> getTeamUserByUserId(Long userId) {
        Map<String, Object> map = new HashMap<>(3);
        map.put(USER_TEAM_TABLE_USER_ID, userId);
        return getTeamUserByMap(map);
    }

    /**
     * query data by userId and teamId
     * @param userId userId
     * @param teamId teamId
     * @return List<TeamUser>
     */
    public List<TeamUser> getTeamUserByUserIdAndTeamId(Long userId, Long teamId) {
        Map<String, Object> map = new HashMap<>(3);
        map.put(USER_TEAM_TABLE_USER_ID, userId);
        map.put(USER_TEAM_TABLE_TEAM_ID, teamId);
        return getTeamUserByMap(map);
    }

    /**
     * get data from table user_team by map
     * @param map map
     * @return teamUser
     */
    private List<TeamUser> getTeamUserByMap(Map<String, Object> map) {
        return teamUserMapper.selectByMap(map);
    }

    /**
     * delete data from table user_team by teamId and userID
     * @param teamId teamId
     * @param userId userId
     * @return if success return true
     */
    public boolean deleteByUserIdAndTeamId(Long teamId, Long userId) {
        Map<String, Object> map = new HashMap<>(4);
        map.put(USER_TEAM_TABLE_TEAM_ID, teamId);
        map.put(USER_TEAM_TABLE_USER_ID, userId);
        dbLogService.dbLog("delete teamUser by teamId&userId:"+teamId+"&"+userId);
        return checkResult(deleteByMap(map));
    }

    /**
     * delete data from table user_team by map
     * @param map map
     * @return if success return true
     */
    private Integer deleteByMap(Map<String, Object> map) {
        return teamUserMapper.deleteByMap(map);
    }

    /**
     * delete data from table user_team by teamId
     * @param teamId teamId
     * @return true or false
     */
    public boolean deleteByTeamId(Long teamId) {
        Map<String, Object> map = new HashMap<>(4);
        map.put(USER_TEAM_TABLE_TEAM_ID, teamId);
        dbLogService.dbLog("delete teamUser by teamId:"+teamId);
        return checkResult(deleteByMap(map));
    }

    /**
     * 判断 teamUser 是否已经存在
     * @param userId userId
     * @param teamId teamId
     * @return if user exist return true, else false
     */
    public boolean teamUserExist(Long userId, Long teamId) {
        List<TeamUser> teamUserList = getTeamUserByUserIdAndTeamId(userId, teamId);
        return null != teamUserList && !teamUserList.isEmpty();
    }

}
