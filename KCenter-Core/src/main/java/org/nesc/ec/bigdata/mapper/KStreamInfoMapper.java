package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.KStreamInfo;

import java.util.List;


@Mapper
public interface KStreamInfoMapper extends BaseMapper<KStreamInfo> {

    @Select("<script>" + "select * from kstream where team_id in "
            + "<foreach item='item' index='index' collection='teamIDs'  open='(' separator=',' close=')'>" + "#{item}"
            + "</foreach>" + " and cluster_id = #{clusterId} order by create_time desc " + "</script>")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "stream_type", property = "streamType"),
            @Result(column = "config", property = "config"),
            @Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
            @Result(column = "cluster_id", property = "clusterId") })
    List<KStreamInfo> getKStreamInfoByTeam(@Param("teamIDs") List<Long> teamIDs,@Param("clusterId") int clusterId);

    @Select("select * from kstream where cluster_id = #{clusterId} order by create_time desc ")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "stream_type", property = "streamType"),
            @Result(column = "config", property = "config"),
            @Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
            @Result(column = "cluster_id", property = "clusterId") })
    List<KStreamInfo> selectKStreamByCluster(@Param("clusterId") int clusterId);
}
