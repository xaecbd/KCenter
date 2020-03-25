package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.UserInfo;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.common.RoleEnum;
import org.nesc.ec.bigdata.common.RoleHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Lola.L.Gou
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    @Select("select * from user_info where id=#{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "email", property = "email"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class)
    })
    UserInfo queryById(Long id);

    @Select("select * from user_info where name=#{name} or name=LOWER(#{name})")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "real_name", property = "realName"),
            @Result(column = "name", property = "name"),
            @Result(column = "email", property = "email"),
            @Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class)
    })
    UserInfo getByEmail(String email,String name);

    @Select("select * from user_info where name=#{name}")
    @Results({
            @Result(column = "real_name", property = "realName"),
            @Result(column = "password", property = "password"),
            @Result(column = "name", property = "name"),
            @Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class)
    })
    UserInfo getPwdByName(String name);

    @Select("select * from user_info")
    @Results({ 
    		@Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class) })
    List<UserInfo> selectUserList();

    @Insert("insert into user_info (name,real_name,email,role,create_time,password) VALUES "
            + "(#{user.name},#{user.realName},#{user.email},#{user.role},#{user.createTime},#{user.password}) ")
    Integer insertToUser(@Param("user") Map<String, Object> user);
    
    @Update("update user_info set role=#{users.role} where id=#{users.id}")
	Integer updateRole(@Param("users") Map<String, String> users);

    @Update("<script> update user_info set " +
            "<if test=\"users.realName != null and users.realName != ''\">" +
            "real_name=#{users.realName}," +
            "</if>" +
            "<if test=\"users.password != null and users.password != ''\">" +
            "password=#{users.password}," +
            "</if>" +
            "<if test=\"users.email != null and users.email != ''\">" +
            "email=#{users.email}" +
            "</if>" +
            "where id=#{users.id}</script>")
    Integer updateUser(@Param("users") UserInfo userInfo);

    @Select("<script>"+ "select * from user_info where id in "+ "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>"+ "#{item}"+ "</foreach>"+ "</script>")
    @Results({ 
		@Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class) })
    List<UserInfo> selectByIds(@Param("ids") Set<Long> setUserIds);

    @Select("select name,email from user_info where id=#{id}")
    UserInfo getEmailById(Long id);

    @Select("select * from user_info where id=#{id}")
    @Results({
            @Result(column = "real_name", property = "realName"),
            @Result(column = "password", property = "password"),
            @Result(column = "name", property = "name"),
            @Result(column = "role", property = "role", javaType= RoleEnum.class, typeHandler = RoleHandler.class) })
    UserInfo getUserInfoById(Long id);

    @Select("select email from user_info where role=#{role}")
    List<String> selectEmailByRole(Integer role);
}