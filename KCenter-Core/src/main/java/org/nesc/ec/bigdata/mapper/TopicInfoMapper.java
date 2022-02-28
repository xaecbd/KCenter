package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.TopicInfo;

import java.util.List;

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


	@Select("SELECT a.id as id,a.cluster_id as cluster_id,a.topic_name as topic_name,a.partition," +
			"a.replication ,a.ttl as ttl,a.owner_id as owner_id,a.team_id as team_id,a.file_size as file_size,a.comments as comments,a.create_time as create_time," +
			"SUM(b.byte_in) AS byte_in,SUM(b.byte_out) AS byte_out, " +
			"SUM(b.file_size) AS disk_size  FROM topic_info a  LEFT JOIN topic_aggregate_metric b ON a.id = b.topic_id  GROUP BY a.topic_name ORDER BY a.create_time DESC;")
	@Results({
			@Result(column = "id",property = "id"),
			@Result(column = "cluster_id",property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")),
			@Result(column = "topic_name",property = "topicName"),
			@Result(column = "partition",property = "partition"),
			@Result(column = "replication",property = "replication"),
			@Result(column = "ttl",property = "ttl"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "file_size",property = "fileSize"),
			@Result(column = "comments",property = "comments"),
			@Result(column = "create_time",property = "createTime"),
			@Result(column = "byte_in",property = "byteIn"),
			@Result(column = "byte_out",property = "byteOut"),
			@Result(column = "disk_size",property = "diskSize")

	})
	List<TopicInfo> searchTopicTotalDataAndBill();


	@Select("SELECT a.id as id,a.cluster_id as cluster_id,a.topic_name as topic_name,a.partition," +
			"a.replication,a.ttl as ttl,a.owner_id as owner_id,a.team_id as team_id,a.file_size as file_size,a.comments as comments,a.create_time as create_time, " +
			"SUM(b.byte_in) AS byte_in,SUM(b.byte_out) AS byte_out, " +
			"SUM(b.file_size) AS disk_size  FROM topic_info a  LEFT JOIN topic_aggregate_metric b ON a.id = b.topic_id where a.cluster_id = #{clusterId} GROUP BY a.topic_name ORDER BY a.create_time DESC;")
	@Results({
			@Result(column = "id",property = "id"),
			@Result(column = "cluster_id",property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")),
			@Result(column = "topic_name",property = "topicName"),
			@Result(column = "partition",property = "partition"),
			@Result(column = "replication",property = "replication"),
			@Result(column = "ttl",property = "ttl"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "file_size",property = "fileSize"),
			@Result(column = "comments",property = "comments"),
			@Result(column = "create_time",property = "createTime"),
			@Result(column = "byte_in",property = "byteIn"),
			@Result(column = "byte_out",property = "byteOut"),
			@Result(column = "disk_size",property = "diskSize")

	})
	List<TopicInfo>  searchTopicTotalDataBillByClusterId(String clusterId);


	@Select("<script>" +
			"SELECT a.id as id,a.cluster_id as cluster_id,a.topic_name as topic_name,a.partition," +
			"a.replication,a.ttl as ttl,a.owner_id as owner_id,a.team_id as team_id,a.file_size as file_size,a.comments as comments,a.create_time as create_time," +
			"SUM(b.byte_in) AS byte_in,SUM(b.byte_out) AS byte_out, " +
			"SUM(b.file_size) AS disk_size  FROM topic_info a  LEFT JOIN topic_aggregate_metric b ON a.id = b.topic_id" +
			" where a.team_id in" + "<foreach item='item' index='index' collection='teamIDs'  open='(' separator=',' close=')'>" + "#{item}"
			+ "</foreach>"+
			" GROUP BY a.topic_name ORDER BY a.create_time DESC;" +
			"</script>")
	@Results({
			@Result(column = "id",property = "id"),
			@Result(column = "cluster_id",property = "cluster", one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")),
			@Result(column = "topic_name",property = "topicName"),
			@Result(column = "partition",property = "partition"),
			@Result(column = "replication",property = "replication"),
			@Result(column = "ttl",property = "ttl"),
			@Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
			@Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
			@Result(column = "file_size",property = "fileSize"),
			@Result(column = "comments",property = "comments"),
			@Result(column = "create_time",property = "createTime"),
			@Result(column = "byte_in",property = "byteIn"),
			@Result(column = "byte_out",property = "byteOut"),
			@Result(column = "disk_size",property = "diskSize")

	})
	List<TopicInfo> searchTopicBillDataByTeamsId(@Param("teamIDs") List<Long> teamIDs);



}
