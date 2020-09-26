package org.nesc.ec.bigdata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.nesc.ec.bigdata.common.BaseController;
import org.nesc.ec.bigdata.common.RestResponse;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author rd87
 * @version 1.0
 * @date 3/23/2019
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

    @Autowired
    RestTemplate restTemplate;

    /**
     * check the user is login
     */
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
            if (name.equalsIgnoreCase(initConfig.getAdminname())) {
                if (userInfoService.isEqualsConfig(name, passwd)) {
                    user.setId(-1L);
                    user.setName(name);
                    user.setRole(RoleEnum.ADMIN);
                    session.setAttribute(SessionAttr.USER.getValue(), user);
                    return user;
                } else {
                    return ERROR("Login fail, Check Username Or Password!");
                }
            } else {
                UserInfo userInfo = userInfoService.getPwdByName(name);
                if (userInfo != null) {
                    List<TeamUser> teamUsers = teamUserService.getTeamUsers(userInfo.getId());
                    List<Long> teamIDs = new ArrayList<>();
                    teamUsers.forEach(teamUser -> teamIDs.add(teamUser.getTeamId()));
                    userInfo.setTeamIDs(teamIDs);
                    if (MD5Util.passwordIsTrue(passwd, userInfo.getPassword())) {
                        session.setAttribute(SessionAttr.USER.getValue(), userInfo);
                        return userInfo;
                    } else {
                        return ERROR("Login fail, Check Username Or Password!");
                    }
                } else {
                    return ERROR("Login fail, UserName is not exits!");
                }
            }

        } catch (Exception e) {
            return ERROR("Login fail, request body error!");
        }
    }

    /**
     * user logins in using oauth2
     */
    @GetMapping("/oauth2")
    public void oauth2Login(HttpServletResponse response) {
        try {
            response.sendRedirect(genericOauthService.createAuthorURL());
        } catch (IOException e) {
            logger.error("oauth2 request url failed", e);
        }
    }

    /**
     * user logout from system and forward to login page
     */
    @GetMapping("/logout")
    public void logout(HttpSession session, HttpServletResponse response) {
        try {
            session.removeAttribute(SessionAttr.USER.getValue());
            response.sendRedirect(url);
        } catch (IOException e) {
            logger.error("redirect logout fail", e);
        }
    }

    /**
     * return information about the logged-in user
     */
    @GetMapping("/user")
    public UserInfo getUserInfo(HttpSession session, @RequestParam("code") String code) {
        UserInfo userDO;
        UserInfo user = genericOauthService.getUser(code);
        userDO = userInfoService.getByEmail((user.getEmail()), user.getName());
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

    @PostMapping("/verify")
    public RestResponse verify(HttpSession session, @RequestBody Map<String, String> body) {

        String sessionId = body.get(Constants.Verify.SESSIONID);
        String email = body.get(Constants.Verify.EMAIL);
        String name = body.get(Constants.Verify.NAME);

        if(StringUtils.isBlank(sessionId)||StringUtils.isBlank(email)||StringUtils.isBlank(name)){
            return ERROR("verify fail.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(Constants.KeyStr.CONTENT_TYPE, Constants.KeyStr.APPLICATION_JSON);
        JSONObject obj = new JSONObject();
        body.forEach(obj::put);
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(obj, headers);
        JSONObject responseBody = restTemplate.postForEntity(initConfig.getLoginVerifyUrl(), httpEntity, JSONObject.class).getBody();
        if (!responseBody.getBoolean(Constants.KeyStr.DATA)) {
            return ERROR("verify fail.");
        }

        UserInfo userDO;
        UserInfo user = new UserInfo();
        user.setName(name);
        user.setEmail(email);
        userDO = userInfoService.getByEmail(email, name);
        if (userDO == null) {
            userInfoService.insert(user);
            user.setRole(RoleEnum.MEMBER);
            session.setAttribute(SessionAttr.USER.getValue(), user);
        } else {
            List<TeamUser> teamUsers = teamUserService.getTeamUsers(userDO.getId());
            List<Long> teamIDs = new ArrayList<>();
            teamUsers.forEach(teamUser -> teamIDs.add(teamUser.getTeamId()));
            userDO.setTeamIDs(teamIDs);
            //userDO.setPicture(user.getPicture());
            session.setAttribute(SessionAttr.USER.getValue(), userDO);
        }
        return SUCCESS_DATA((UserInfo) session.getAttribute(SessionAttr.USER.getValue()));
    }
}
