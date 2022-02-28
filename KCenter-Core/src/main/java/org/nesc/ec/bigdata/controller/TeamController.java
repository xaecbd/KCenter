package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.model.TeamInfo;
import org.nesc.ec.bigdata.model.TeamUser;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.ClusterService;
import org.nesc.ec.bigdata.service.TeamInfoService;
import org.nesc.ec.bigdata.service.TeamUserService;
import org.nesc.ec.bigdata.service.UserInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author rd87
 * @date 3/23/2019
 * @version 1.0
 */
@RestController
@RequestMapping("/team")
public class TeamController  extends BaseController {

    private static Logger LOG = LoggerFactory.getLogger(TeamController.class);
    @Autowired
    TeamInfoService teamInfoService;

    @Autowired
    TeamUserService teamUserService;

    @Autowired
    UserInfoService userInfoService;

    @Autowired
    ClusterService clusterService;

    /**return team list according to role*/
    @RequestMapping(  value = "/", method = RequestMethod.GET )
    public RestResponse list() {
        try {
			List<TeamInfo> teamInfoList = listTeams();
            return SUCCESS_DATA(teamInfoList);
        } catch (Exception e) {
        	LOG.error("Get Team List Errors,msg:",e);
            return ERROR("GET Team list error!");
        }

    }

    @RequestMapping(  value = "", method = RequestMethod.GET )
    public RestResponse listTeam() {
        try {
            List<TeamInfo> teamInfoList = listTeams();
            return SUCCESS_DATA(teamInfoService.transformToSelectData(teamInfoList));
        } catch (Exception e) {
            LOG.error("Get Team List Errors,msg:",e);
            return ERROR("GET Team list error!");
        }

    }

    private List<TeamInfo> listTeams() {
        List<TeamInfo> teamInfoList = null;
        try{
            UserInfo user = this.getCurrentUser();
            if(RoleEnum.ADMIN.getDescription().equals(user.getRole().getDescription())) {
                teamInfoList = teamInfoService.getTotalData();
            } else {
                List<Long> teamIDs = user.getTeamIDs();
                teamInfoList = teamInfoService.getTeamsByTeamIDs(teamIDs);
            }
            return teamInfoList;
        }catch (RuntimeException e){
            throw e;
        }

    }

    /**return team list*/
    @RequestMapping(  value = "/get", method = RequestMethod.GET )
    public TeamInfo get(Long id) {
        return teamInfoService.selectById(id);
    }

    /**save team,
     * if team exits,return error
     * else add team to team table success,return success
     * */
    @RequestMapping(  value = "/save", method = RequestMethod.POST )
    public RestResponse save(@RequestBody TeamInfo team) {
        try {
            if(teamInfoService.teamNameExist(team.getName())) {
                return ERROR("team " + team.getName() + " already exist, please modify!");
            }
            if (teamInfoService.insert(team)){
                return SUCCESS("Save team success.");
            } else {
                return ERROR("Save team fail.");
            }
        } catch (Exception e) {
        	LOG.error("Save Team Errors,msg:",e);
            return ERROR("Save team error.");
        }
    }

    /**save or update team,if team exits,return error,
     * else update team to team table,return success
     * */
    @RequestMapping(  value = "/upsert", method = RequestMethod.POST )
    public RestResponse update(@RequestBody TeamInfo team) {
        try {

            if (null != team.getId()) {
                if(!teamInfoService.isAllowUpdate(team)) {
                    return ERROR("team " + team.getName() + " already exist, please modify!");
                }
                if (teamInfoService.update(team)) {
                    return SUCCESS("update team success.");
                } else {
                    return ERROR("update team fail.");
                }
            } else {
                if(teamInfoService.teamNameExist(team.getName())) {
                    return ERROR("team " + team.getName() + " already exist, please modify!");
                }

                if (teamInfoService.insert(team)){
                    return SUCCESS("Save team success.");
                } else {
                    return ERROR("Save team fail.");
                }
            }
        } catch (Exception e) {
        	LOG.error("update or save team error.msg:",e);
            return ERROR("update or save team error.");
        }
    }

    /**delete team,
     * if team  is associated with Topic,return error,
     * else delete team from team table
     * */
    @RequestMapping(  value = "/del/{teamId}", method = RequestMethod.DELETE )
    public RestResponse del(@PathVariable Long teamId) {
        try {
        	// 先判断能不能删（有没有topic与之关联）
        	if(!teamInfoService.isRelatedTopic(teamId)) {
                if(teamInfoService.delete(teamId)){
                	teamUserService.deleteByTeamId(teamId);
                    return SUCCESS("Delete team success.");
                }
        	} else {
        		return ERROR("This team is associated with some topics and cannot be deleted.");
        	}
        } catch (Exception e) {
            LOG.error("Delete team fail.", e);
        }
        return ERROR("Delete team fail.");
    }

    /**add user to team*/
    @RequestMapping(  value = "/adduser", method = RequestMethod.POST )
    public RestResponse addUser(@RequestBody TeamUser teamUser) {

        try {
            if (teamUserService.teamUserExist(teamUser.getUserId(), teamUser.getTeamId())) {
                return ERROR("this user already exist!");
            }
            if (teamUserService.insert(teamUser)){
                return SUCCESS("Add user to team success.");
            }
        } catch (Exception e) {
            LOG.error("Add user to team fail.",e);
        }
        return ERROR("Add user to team fail.");
    }

    /**return the user of the specified team */
    @GetMapping("/userinfos/{teamId}")
    public RestResponse userInfos(@PathVariable Long teamId) {
        try {
            List<UserInfo> users = new ArrayList<>();
            List<TeamUser> teamUserList = teamUserService.getTeamUserByTeamId(teamId);
            Set<Long> setUserIds = teamUserList.stream().map(TeamUser::getUserId).collect(Collectors.toSet());
            if(setUserIds.size() > 0){
                users = userInfoService.getUsersByIds(setUserIds);
            }
            return SUCCESS_DATA(users);
        } catch (Exception e) {
            LOG.error("Get users by team id error!", e);
            return ERROR("Get users by team id error!");
        }
    }

    /**delete user of team */
    @DeleteMapping("/del/user")
    public RestResponse removeUser(@RequestParam Long userId, @RequestParam Long teamId) {
        try{
            if(teamUserService.deleteByUserIdAndTeamId(teamId, userId)) {
                return SUCCESS("Remove user success!");
            }
        }
        catch (Exception e) {
            LOG.error("Remove user failed!", e);
        }
        return ERROR("Remove user failed!");
    }

    /**return the user`s team*/
	@GetMapping("/userteam")
	public RestResponse getUserTeamBySession() {
		try {
			UserInfo user = this.getCurrentUser();
			if ("admin".equals(user.getName())) {
				return ERROR("The administrator creates the topic directly in [Kafka Manager -> Topic].");
			} else if (null != user.getTeamIDs() && !user.getTeamIDs().isEmpty()) {
				if (clusterService.clusterInfoIfEmpty()) {
					return ERROR("Please add cluster first!");
				}
				List<TeamUser> teamUserList = teamUserService.getTeamUserByUserId(user.getId());
				List<Long> teamIds = teamUserList.stream().map(TeamUser::getTeamId).collect(Collectors.toList());
				List<TeamInfo> teamInfoList = teamInfoService.getTeamInfoByTeamIds(teamIds);
				List<JSONObject> result = teamInfoService.transformToSelectData(teamInfoList);
				if (!result.isEmpty()) {
					return SUCCESS_DATA(result);
				}
				return ERROR("Please join a team first.");
			} else {
				return ERROR("Please join a team first.");
			}
		} catch (Exception e) {
			LOG.error("Get user team failed!", e);
			return ERROR("Get user team failed!");
		}
	}


}
