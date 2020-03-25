package org.nesc.ec.bigdata.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.constant.Constants;
import org.nesc.ec.bigdata.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import org.nesc.ec.bigdata.mapper.UserInfoMapper;
import org.nesc.ec.bigdata.model.TeamUser;

@Service
public class UserInfoService {

	@Autowired
	UserInfoMapper userInfoMapper;
	@Autowired
	DBLogService dbLogService;

    @Value("${spring.security.user.name}")
    private String adminname;
    
    @Value("${spring.security.user.password}")
    private String adminpwd;
    
	public boolean insert(UserInfo user) {
		user.setCreateTime(new Date());
		Integer result =  userInfoMapper.insert(user);
		return checkResult(result);
	}

	public UserInfo selectById(Long id) {
		return userInfoMapper.selectById(id);
	}
	public UserInfo getEmailById(Long id) {
		return userInfoMapper.getEmailById(id);
	}

	List<String> selectEmailByRole(RoleEnum roleEnum){
		return userInfoMapper.selectEmailByRole(roleEnum.getValue());
	}

	public UserInfo getByEmail(String email,String name) {
			return userInfoMapper.getByEmail(email,name);
	}


	public UserInfo getPwdByName(String name){
		return userInfoMapper.getPwdByName(name);
	}
	public List<UserInfo> getTotalData(){
		return userInfoMapper.selectUserList();
	}

	public boolean update(UserInfo user) {
		Integer result = userInfoMapper.updateById(user);
		return checkResult(result);
	}

	public boolean delete(Long id) {
		Integer result = userInfoMapper.deleteById(id);
		dbLogService.dbLog("delete user by id:"+id);
		return checkResult(result);		
	}

	public boolean updateInfo(UserInfo userInfo){
		return checkResult(userInfoMapper.updateUser(userInfo));
	}

	public int getRole(String RoleName){
		if(RoleEnum.ADMIN.getDescription().equalsIgnoreCase(RoleName)){
			return RoleEnum.ADMIN.getValue();
		}else{
			return RoleEnum.MEMBER.getValue();
		}
	}

	public boolean insertToUser(Map<String,Object> user){
		user.put(Constants.User.CREATETIME,new Date());
		return  checkResult(userInfoMapper.insertToUser(user));
	}

	private boolean checkResult(Integer result) {
		return result > 0;
	}

	/**
	 * get users by ids
	 * @param setUserIds userId
	 * @return users
	 */
	public List<UserInfo> getUsersByIds(Set<Long> setUserIds) {
		return userInfoMapper.selectByIds(setUserIds);
	}
	UserInfo getUserInfoById(Long id) {
		return userInfoMapper.getUserInfoById(id);
	}

	/**
	 * 将数据库中的数据转换为页面需要的格式
	 * @param userInfos userInfos
	 * @return List
	 */
	public List<JSONObject> transformToSelectData(List<UserInfo> userInfos) {
		List<JSONObject> result = new ArrayList<>();
		JSONObject object;
		for (UserInfo userInfo : userInfos) {
			object = new JSONObject(3);
			object.put(Constants.JsonObject.VALUE, userInfo.getId());
			object.put(Constants.JsonObject.LABEL, userInfo.getName());
			result.add(object);
		}
		return result;
	}

	/**
	 * 过滤掉已经在Team存在的用户
	 * @param userInfos userInfos
	 * @param teamUsers teamUsers
	 * @return 不在Team中的用户
	 */
	public List<UserInfo> filterAlreadyExistUser(List<UserInfo> userInfos, List<TeamUser> teamUsers) {
		Iterator<UserInfo> infoIterator = userInfos.iterator();
		UserInfo userInfo;
		while (infoIterator.hasNext()) {
			userInfo = infoIterator.next();
			for (TeamUser teamUser : teamUsers) {
				if (userInfo.getId().equals(teamUser.getUserId())) {
					infoIterator.remove();
				}
			}
		}
		return userInfos;
	}
	
	/**
	 * 更新用户role，前台传来的是MEMBER、ADMIN,在此转化为数字形式
	 * @param user
	 * @return
	 */
	public boolean updateRole(Map<String, String> user) {
		for(Entry<String, String> u : user.entrySet()) {
			if(Constants.Role.ROLE.equals(u.getKey()) && Constants.Role.MEMBER_UPP.equals(u.getValue())) {
				user.put(u.getKey(), Constants.Number.ONE_HUNANDER);
			}
			if(Constants.Role.ROLE.equals(u.getKey()) && Constants.Role.ADMIN_UPP.equals(u.getValue())) {
				user.put(u.getKey(), Constants.Number.ONE);
			}
    	}
		Integer result = userInfoMapper.updateRole(user);
		return checkResult(result);
	}

	public boolean isEqualsConfig(String username, String password) {
		return adminname.equals(username) && adminpwd.equals(password);
	}
}
