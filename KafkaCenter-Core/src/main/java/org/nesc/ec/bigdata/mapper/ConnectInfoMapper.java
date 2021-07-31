package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.ConnectorInfo;

import java.util.List;

@Mapper
public interface ConnectInfoMapper extends BaseMapper<ConnectorInfo> {

    @Select("<script>" + "select * from connect_info where  "
            + "<foreach item='item' index='index' collection='teamIDs'   separator='OR' >" + " FIND_IN_SET(#{item},team_ids) "
            + "</foreach></script>")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cluster_id", property = "cluster",one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")),
            @Result(column = "version", property = "version"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "url", property = "url"),
            @Result(column = "team_ids", property = "teamIds") })
    List<ConnectorInfo> getConnectByTeams(@Param("teamIDs") List<Long> teamIDs);

    @Select("select * from connect_info order by create_time desc;")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cluster_id", property = "cluster",one = @One(select = "org.nesc.ec.bigdata.mapper.ClusterInfoMapper.queryById")),
            @Result(column = "version", property = "version"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "url", property = "url"),
            @Result(column = "team_ids", property = "teamIds") })
    List<ConnectorInfo> selectConnectList();

    @Select("<script>" + "select * from connect_info where  "
            + "<foreach item='item' index='index' collection='urls'   separator='OR' >" + " FIND_IN_SET(#{item},url) "
            + "</foreach></script>")
    List<ConnectorInfo>  getConnectByUrl(@Param("urls") List<String> urls);

    @Select("<script>" + "select * from connect_info where id != #{id} and "
            + "<foreach item='item' index='index' collection='urls'   separator='OR' >" + " FIND_IN_SET(#{item},url) "
            + "</foreach> </script>")
    List<ConnectorInfo>  getConnectByUrlAndId(@Param("urls") List<String> urls,@Param("id") long id );
}
