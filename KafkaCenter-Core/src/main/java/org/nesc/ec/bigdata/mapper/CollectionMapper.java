package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.Collections;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface CollectionMapper extends BaseMapper<Collections> {
	
	@Select("select * from topic_collection where user_id=#{userId} and type=#{type}")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "name", property = "name"),
		@Result(column = "type", property = "type"), 
		@Result(column = "user_id",property="user", one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
    List<Collections> listByUser(Long userId, String type);
	
	@Select("select * from topic_collection where user_id=#{userId} and type=#{type} and cluster_id=#{clusterId} and name=#{name}")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "name", property = "name"),
		@Result(column = "type", property = "type"), 
		@Result(column = "user_id",property="user", one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
    Collections getInfo(Long userId, String type, String name, String clusterId);
	
	@Select("select * from topic_collection where user_id=#{userId} and type=#{type}")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "name", property = "name"),
		@Result(column = "type", property = "type"), 
		@Result(column = "user_id",property="user", one=@One(select="org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
		@Result(column = "cluster_id",property="cluster", one=@One(select="org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById"))
    })
	List<Collections> listData(Long userId,String type);
	
	

}
