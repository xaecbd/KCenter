package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.TaskInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskInfoMapper extends BaseMapper<TaskInfo> {

	@Select("select * from task_info where id=#{id} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "cluster_ids", property = "clusterIds"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "message_rate", property = "messageRate"),
			@Result(column = "partition", property = "partition"),
			@Result(column = "replication", property = "replication"),
			@Result(column = "ttl", property = "ttl"),
			@Result(column = "comments", property = "comments"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "approve_id", property = "approveUser",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column="team_id",property="team",one=@One(select="org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById"))
	})
	TaskInfo queryById(Long id);

	@Select("select * from task_info where topic_name=#{topicName} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
		@Result(column = "cluster_ids", property = "clusterIds"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "partition", property = "partition"),
		@Result(column = "replication", property = "replication"),
		@Result(column = "ttl", property = "ttl"),
		@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "approved_id", property = "approve",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column="team_id",property="team",one=@One(select="org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
		@Result(column = "comments", property = "comments"),
		@Result(column = "create_time", property = "createTime")
		})
	List<TaskInfo> getTaskByTopicName(String topicName);
	
	@Select("select * from task_info  order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
		@Result(column = "cluster_ids", property = "clusterIds"),
		@Result(column = "message_rate", property = "messageRate"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "partition", property = "partition"),
		@Result(column = "replication", property = "replication"),
		@Result(column = "ttl", property = "ttl"),
		@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "approved_id", property = "approve",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column="team_id",property="team",one=@One(select="org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
		@Result(column = "comments", property = "comments"),
		@Result(column = "create_time", property = "createTime"),
		@Result(column = "approval_opinions", property = "approvalOpinions")
		})
	List<TaskInfo> selectTaskList();

	@Select("select * from task_info where owner_id=#{ownerId} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
		@Result(column = "cluster_ids", property = "clusterIds"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "partition", property = "partition"),
		@Result(column = "replication", property = "replication"),
		@Result(column = "ttl", property = "ttl"),
		@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "approved_id", property = "approve",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column="team_id",property="team",one=@One(select="org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
		@Result(column = "comments", property = "comments"),
		@Result(column = "message_rate", property = "messageRate"),
		@Result(column = "create_time", property = "createTime"),
		@Result(column = "approval_opinions", property = "approvalOpinions")
		})
	List<TaskInfo> selectByOwnerId(Long ownerId);
	
	@Select("select * from task_info where cluster_ids like #{sql} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
		@Result(column = "cluster_ids", property = "clusterIds"),
		@Result(column = "topic_name", property = "topicName"),
		@Result(column = "partition", property = "partition"),
		@Result(column = "replication", property = "replication"),
		@Result(column = "ttl", property = "ttl"),
		@Result(column = "owner_id", property = "owner",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "approved_id", property = "approve",one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column="team_id",property="team",one=@One(select="org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
		@Result(column = "comments", property = "comments"),
		@Result(column = "create_time", property = "createTime")
		})
	List<TaskInfo> selectByClusterId(String sql);
}
