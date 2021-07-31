package org.nesc.ec.bigdata.mapper;


import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.AlertGoup;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author jc1e
 *
 */
@Mapper
public interface AlertMapper extends BaseMapper<AlertGoup> {
	
    @Select("select * from alert_group where topic_name=#{topicName}")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "consummer_group", property = "consummerGroup"), 
		@Result(column = "consummer_api", property = "consummerApi"),
		@Result(column = "threshold", property = "threshold"),
		@Result(column = "dispause", property = "dispause"),
		@Result(column = "mail_to", property = "mailTo"), @Result(column = "enable", property = "enable"),
		@Result(column = "webhook", property = "webhook"), @Result(column = "disable_alerta", property = "disableAlerta"),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
    List<AlertGoup> getGroups(@Param("topicName") String topicName);

	@Select("select * from alert_group where cluster_id=#{clusterId}")
	@Results({
			@Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "consummer_group", property = "consummerGroup"),
			@Result(column = "consummer_api", property = "consummerApi"),
			@Result(column = "threshold", property = "threshold"),
			@Result(column = "dispause", property = "dispause"),
			@Result(column = "mail_to", property = "mailTo"),
			@Result(column = "create_date", property = "createDate"),
			@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "webhook", property = "webhook"),
			@Result(column = "disable_alerta", property = "disableAlerta"),
			@Result(column = "enable", property = "enable"),
			@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
	})
	List<AlertGoup> getAllGroupsByCluster(String clusterId);

    @Select("select * from alert_group")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "consummer_group", property = "consummerGroup"), 
		@Result(column = "consummer_api", property = "consummerApi"),
		@Result(column = "threshold", property = "threshold"),
		@Result(column = "dispause", property = "dispause"),
		@Result(column = "mail_to", property = "mailTo"), @Result(column = "disable_alerta", property = "disableAlerta"),
		@Result(column = "create_date", property = "createDate"), @Result(column = "enable", property = "enable"),
		@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "webhook", property = "webhook"),@Result(column = "disable_alerta", property = "disableAlerta"),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
    List<AlertGoup> getAllGroups();

	@Select("select * from alert_group t where t.enable=1")
	@Results({
			@Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "consummer_group", property = "consummerGroup"),
			@Result(column = "consummer_api", property = "consummerApi"),
			@Result(column = "threshold", property = "threshold"),
			@Result(column = "dispause", property = "dispause"),
			@Result(column = "disable_alerta", property = "disableAlerta"),
			@Result(column = "mail_to", property = "mailTo"),
			@Result(column = "create_date", property = "createDate"), @Result(column = "enable", property = "enable"),
			@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "webhook", property = "webhook"),@Result(column = "disable_alerta", property = "disableAlerta"),
			@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
	})
	List<AlertGoup> getEnableAlertGroups();

    @Insert("insert into alert_group (cluster_id,topic_name,consummer_group,consummer_api,threshold,dispause,mail_to,webhook,disable_alerta,enable) VALUES "
    		+ "(#{alertMap.cluster_id},#{alertMap.topicName},#{alertMap.consummerGroup},#{alertMap.consummerApi},#{alertMap.threshold},#{alertMap.dispause},#{alertMap.mailTo},#{alertMap.webhook},#{alertMap.disableAlerta},#{alertMap.enable}) ")
	void insertAlert(@Param("alertMap") Map<String, String> alertMap);
    
    @Select("select count(*)  from alert_group where topic_name=#{dataMap.topicName} and consummer_group=#{dataMap.consummerGroup}")
	int getDataCount(@Param("dataMap") Map<String, String> dataMap);
    
    @Select("select count(*)  from alert_group where cluster_id=#{clusterId} and topic_name=#{topicName} and consummer_group=#{group}")
	int exites(Integer clusterId,String topicName,String group);
   
    
    @Select("select * from alert_group where topic_name=#{dataMap.topic_name} and consummer_group=#{dataMap.consummer_group} ")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "consummer_group", property = "consummerGroup"), 
		@Result(column = "consummer_api", property = "consummerApi"),
		@Result(column = "threshold", property = "threshold"),
		@Result(column = "dispause", property = "dispause"),@Result(column = "enable", property = "enable"),
		@Result(column = "mail_to", property = "mailTo"), @Result(column = "enable", property = "enable"),
		@Result(column = "webhook", property = "webhook"),@Result(column = "disable_alerta", property = "disableAlerta"),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
	AlertGoup getTopicOneGroup(@Param("dataMap") Map<String, Object> dataMap);

    @Update("update alert_group set consummer_group = #{alertMap.consummerGroup}, consummer_api = #{alertMap.consummerApi}, threshold = #{alertMap.threshold},dispause=#{alertMap.dispause},mail_to=#{alertMap.mailTo},webhook=#{alertMap.webhook},disable_alerta=#{alertMap.disableAlerta} where id = #{alertMap.id}")
	void updateAlertById(@Param("alertMap") Map<String, String> alertMap);

    @Select("select count(1) from  alert_group")
    int countData();

    @Update("update alert_group set enable=#{alertMap.enable},disable_alerta=#{alertMap.disableAlerta} where id=#{alertMap.id}")
    int updateEnable(@Param("alertMap") Map<String, Object> alertMap);

	@Select("select * from alert_group where owner_id=#{id}")
	@Results({
			@Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "consummer_group", property = "consummerGroup"),
			@Result(column = "consummer_api", property = "consummerApi"),
			@Result(column = "threshold", property = "threshold"),
			@Result(column = "dispause", property = "dispause"),@Result(column = "enable", property = "enable"),
			@Result(column = "mail_to", property = "mailTo"),@Result(column = "disable_alerta", property = "disableAlerta"),
			@Result(column = "create_date", property = "createDate"), @Result(column = "enable", property = "enable"),
			@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "webhook", property = "webhook"),@Result(column = "disable_alerta", property = "disableAlerta"),
			@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
	})
	List<AlertGoup> selectAlertGroupByOwnId(@Param("id") long id);


	@Select("select a.id,t.alarm_group from alert_group a,user_team u,team_info t where a.owner_id=u.user_id and t.id=u.team_id")
	List<Map<String,Object>> getAlertAlarmGroupMap();
}