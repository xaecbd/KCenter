package org.nesc.ec.bigdata.controller;
/** 
* @author：Truman.P.Du  
* @createDate: 2019年3月20日 下午2:03:50 
* @version:1.0
* @description:
*/

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
import org.nesc.ec.bigdata.common.util.MD5Util;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.TeamUser;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.service.TeamUserService;
import org.nesc.ec.bigdata.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController{

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	@Autowired
    private UserInfoService userInfoService;

	@Autowired
	private TeamUserService teamUserService;
	
	@PostMapping(value="register")
	@ResponseBody
	public RestResponse add(@RequestBody Map<String, Object> user ) {
		try {
			UserInfo userInfo = userInfoService.getPwdByName((String) user.get(Constants.User.NAME));
			if(userInfo==null){
				int role = userInfoService.getRole((String) user.get(Constants.Role.ROLE));
				user.put(Constants.Role.ROLE,String.valueOf(role));
				String pwd = MD5Util.string2MD5("123456");
				user.put(Constants.User.PASSWORD,pwd);
				if(userInfoService.insertToUser(user)) {
					return SUCCESS("add success!");
				}else {
					return ERROR("add failed!");
				}
			}else{
				return ERROR("The user already exits!");
			}

		} catch (Exception e) {
			return ERROR("add failed!");
		}
			
	}
	
	@PutMapping(value="modifty")
	@ResponseBody
	public RestResponse update(@RequestBody UserInfo user) {
		try {
			if(!"".equalsIgnoreCase(user.getPassword()) && null!=user.getPassword()){
				String password = MD5Util.string2MD5(user.getPassword());
				user.setPassword(password);
			}
			if(userInfoService.updateInfo(user)) {
				return SUCCESS("update success!");
			}else {

				return ERROR("update failed!");
			}	
		} catch (Exception e) {
			return ERROR("update failed!");
		}
	}
	  
	@PutMapping(value="role")
	@ResponseBody
	public RestResponse updateRole(@RequestBody Map<String, String> user) {
		try {
			if(userInfoService.updateRole(user)) {
				return SUCCESS("update success!");
			}else {
				return ERROR("update failed!");
			}	
		} catch (Exception e) {			
			return ERROR("update failed!");
		}
	}
	
	@DeleteMapping("/del/{userId}")
	@ResponseBody
    public RestResponse del(@PathVariable Long userId) {
        if(userInfoService.delete(userId)){
            return SUCCESS("Delete user success.");
        } else {
            return ERROR("Delete user fail.");
        }
    }
    
	@GetMapping("")
	@ResponseBody
    public RestResponse list() {
        try {
            List<UserInfo> userInfoList = userInfoService.getTotalData();
            return SUCCESS_DATA(userInfoList);
        } catch (Exception e) {
            return ERROR("GET User list error!");
        }
    }
	
	@GetMapping("/get")
	@ResponseBody
	public RestResponse getUserById(@RequestParam Long id) {
		try {
			UserInfo user = userInfoService.selectById(id);
			return SUCCESS_DATA(user);
		} catch (Exception e) {	
			return ERROR("select User failed");
		}		
	}

	@GetMapping("/all/{teamId}")
	public RestResponse getAllUser(@PathVariable Long teamId) {
		try {
			List<UserInfo> userInfos = userInfoService.getTotalData();
			List<TeamUser> teamUserList = teamUserService.getTeamUserByTeamId(teamId);
			List<UserInfo> userInfoList = userInfoService.filterAlreadyExistUser(userInfos, teamUserList);
			List<JSONObject> result = userInfoService.transformToSelectData(userInfoList);
			return SUCCESS_DATA(result);
		} catch (Exception e) {
			LOGGER.error("Get all user failed", e);
			return ERROR("Get all user failed");
		}
	}
}