package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.common.SessionAttr;
import org.nesc.ec.bigdata.common.util.MD5Util;
import org.nesc.ec.bigdata.config.InitConfig;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.TeamUser;
import org.nesc.ec.bigdata.model.UserInfo;
import org.nesc.ec.bigdata.security.auth.GenericOauthService;
import org.nesc.ec.bigdata.service.TeamUserService;
import org.nesc.ec.bigdata.service.UserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rd87
 * @date 3/23/2019
 * @version 1.0
 */
@RestController
@RequestMapping("/login")
public class LoginController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private GenericOauthService genericOauthService;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	TeamUserService teamUserService;

	@Autowired
	InitConfig initConfig;

	@Value("${public.url:/}")
	String url;

	@SuppressWarnings("unchecked")
	@GetMapping("/check")
	public UserInfo loginCheck(HttpSession session) {
		return (UserInfo) session.getAttribute(SessionAttr.USER.getValue());
	}

	/**
	 * 普通用户登录
	 *
	 * @param session
	 * @param body
	 * @return
	 */
	@PostMapping("/system")
	@ResponseBody
	public Object systemLogin(HttpSession session, @RequestBody String body) {
		UserInfo user = new UserInfo();
		try {
			JSONObject userJson = JSON.parseObject(body);
			String name = userJson.getString(Constants.KeyStr.USER_NAME);
			String passwd = userJson.getString(Constants.KeyStr.PASSWORD);
			if(name.equalsIgnoreCase(initConfig.getAdminname())){
				if (userInfoService.isEqualsConfig(name, passwd)) {
					user.setName(name);
					user.setRole(RoleEnum.ADMIN);
					session.setAttribute(SessionAttr.USER.getValue(), user);
					return user;
				} else {
					return ERROR("Login fail, Check Username Or Password!");
				}
			}else{
				UserInfo userInfo = userInfoService.getPwdByName(name);
				if(userInfo!=null){
					List<TeamUser> teamUsers = teamUserService.getTeamUsers(userInfo.getId());
					List<Long> teamIDs = new ArrayList<>();
					teamUsers.forEach(teamUser -> teamIDs.add(teamUser.getTeamId()));
					userInfo.setTeamIDs(teamIDs);
					if(MD5Util.passwordIsTrue(passwd,userInfo.getPassword())){
						session.setAttribute(SessionAttr.USER.getValue(), userInfo);
						return userInfo;
					}else{
						return ERROR("Login fail, Check Username Or Password!");
					}
				}else{
					return ERROR("Login fail, UserName is not exits!");
				}
			}

		} catch (Exception e) {
			return ERROR("Login fail, request body error!");
		}
	}

	@GetMapping("/oauth2")
	public void oauth2Login(HttpServletResponse response){
		try {
			response.sendRedirect(genericOauthService.createAuthorURL());
		} catch (IOException e) {
			logger.error("oauth2 request url faild", e);
		}
	}

	@GetMapping("/logout")
	public void logout(HttpSession session, HttpServletResponse response) {
		try {
			response.sendRedirect(url);
			session.removeAttribute(SessionAttr.USER.getValue());
		} catch (IOException e) {
			logger.error("redirect logout fail", e);
		}
	}

	@GetMapping("/user")
	public UserInfo getUserInfo(HttpSession session,  @RequestParam("code") String code) {
		UserInfo userDO;
		UserInfo user = genericOauthService.getUser(code);
		userDO = userInfoService.getByEmail((user.getEmail()),user.getName());
		if (userDO == null) {
			userInfoService.insert(user);
			user.setRole(RoleEnum.MEMBER);
			session.setAttribute(SessionAttr.USER.getValue(), user);
		} else {
			List<TeamUser> teamUsers = teamUserService.getTeamUsers(userDO.getId());
			List<Long> teamIDs = new ArrayList<>();
			teamUsers.forEach(teamUser -> teamIDs.add(teamUser.getTeamId()));
			userDO.setTeamIDs(teamIDs);
			userDO.setPicture(user.getPicture());
			session.setAttribute(SessionAttr.USER.getValue(), userDO);
		}
		return (UserInfo) session.getAttribute(SessionAttr.USER.getValue());
	}
}
