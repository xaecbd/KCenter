package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import kafka.security.auth.Topic;
import net.sf.jsqlparser.statement.select.Top;
import org.nesc.ec.bigdata.model.TeamInfo;
import org.nesc.ec.bigdata.model.TopicInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface TopicInfoMapper extends BaseMapper<TopicInfo> {

	@Select("select * from topic_info where id=#{id} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "cluster_id", property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")) })
	TopicInfo getTopicById(Long id);

	@Select("select * from topic_info order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "cluster_id", property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")) })
	List<TopicInfo> getTopics();


	@Select("select * from topic_info where cluster_id=#{clusterId} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "cluster_id", property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")) })
	List<TopicInfo> getTopicsByCluster(String clusterId);

	@Select("<script>" + "select * from topic_info where team_id in "
			+ "<foreach item='item' index='index' collection='teamIDs'  open='(' separator=',' close=')'>" + "#{item}"
			+ "</foreach>" + " order by create_time desc " + "</script>")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "cluster_id", property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")) })
	List<TopicInfo> getTopicsByTeamIDs(@Param("teamIDs") List<Long> teamIDs);
	
	@Select("select * from topic_info  where topic_name=#{topic} and cluster_id=#{clientId} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "create_time", property = "createTime"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "cluster_id", property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")) })
	TopicInfo getTopicsByTopicName(String topic,Long clientId);

	@Select("select file_size from topic_info  where topic_name=#{topic} and cluster_id=#{clientId} order by create_time desc")
	String selectFileSizeByTopic(String topic,Long clientId);

	@Select("select * from topic_info where cluster_id=#{clusterId} order by create_time desc")
	@Results({ @Result(id = true, column = "id", property = "id"),
			@Result(column = "topic_name", property = "topicName"),
			@Result(column = "file_size", property = "fileSize"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById"))})
	List<TopicInfo> selectTopicsByCluster(String clusterId);

	
	@Select("<script>" + "select * from topic_info where owner_id in "
			+ "<foreach item='item' index='index' collection='userIds'  open='(' separator=',' close=')'>" + "#{item}"
			+ "</foreach>" +"</script>")
	List<TopicInfo> getTopicsByUserIDs(@Param("userIds") List<Long> userIds);


	@Select("SELECT team_id,SUM(file_size) as size FROM topic_info where TEAM_ID IS NOT NULL GROUP BY team_id;")
	@Results({
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "size", property = "fileSize"),
	})
	List<TopicInfo> topicFileSizeByTeam();

}
