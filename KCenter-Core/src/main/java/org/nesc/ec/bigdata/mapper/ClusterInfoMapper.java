package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.ClusterInfo;

import java.util.List;

@Mapper
public interface ClusterInfoMapper extends BaseMapper<ClusterInfo> {
	
    @Select("select * from cluster_info where broker like #{broker} order by create_time desc")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "name", property = "name"),
		@Result(column = "zk_address", property = "zkAddress"), 
		@Result(column = "broker", property = "broker"),
		@Result(column = "broker_size", property = "brokerSize"),
		@Result(column = "location", property = "location"),
		@Result(column = "kafka_version", property = "kafkaVersion"),
		@Result(column = "graf_addr", property = "grafAddr"),
		@Result(column = "enable", property = "enable")
    })
	ClusterInfo getClusterByZkAndBroker(@Param("broker") String broker);
    
    @Select("select * from cluster_info where id=#{id} order by create_time desc")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "name", property = "name"),
		@Result(column = "zk_address", property = "zkAddress"), 
		@Result(column = "broker", property = "broker"),
		@Result(column = "location", property = "location"),
		@Result(column = "broker_size", property = "brokerSize"),
		@Result(column = "kafka_version", property = "kafkaVersion"),
		@Result(column = "graf_addr", property = "grafAddr"),
		@Result(column = "enable", property = "enable")
    })
    ClusterInfo queryById(Long id);


	@Select("select * from cluster_info")
	@Results({
			@Result(id = true, column = "id", property = "id"),
			@Result(column = "zk_address", property = "zkAddress"),
			@Result(column = "broker", property = "broker")
	})
	List<ClusterInfo> selectBrokers();

    @Select("select broker_size from cluster_info")
	List<Integer> getAllClusterSize();

	@Select("select * from cluster_info where name=#{clusterName}")
	@Results({
			@Result(id = true, column = "id", property = "id"),
			@Result(column = "zk_address", property = "zkAddress"),
			@Result(column = "broker", property = "broker"),
			@Result(column = "ksql_url",property = "ksqlUrl")
	})
	ClusterInfo selectByClusterName(String clusterName);
}
