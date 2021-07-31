package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.ConnectorJob;

import java.util.List;

@Mapper
public interface ConnectJobMapper extends BaseMapper<ConnectorJob> {

    @Select("<script>" + "select * from connect_job where cluster_id = #{clusterId} and is_history = 0 and team_id in "
            + "<foreach item='item' index='index' collection='teamIDs'  open='(' separator=',' close=')'>" + "#{item}"
            + "</foreach>" + " order by create_time desc " + "</script>")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "type", property = "type"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "state", property = "state"),
            @Result(column = "class_name", property = "className"),
            @Result(column = "script", property = "script"),
            @Result(column = "owner_id", property = "owner",one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team",one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")) })
    List<ConnectorJob> getConnectorJobByTeams(@Param("teamIDs") List<Long> teamIDs,@Param("clusterId") int clusterId);

    @Select("select * from connect_job where cluster_id = #{clusterId} and is_history = 0 order by create_time desc; ")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "type", property = "type"),
            @Result(column = "class_name", property = "className"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "state", property = "state"),
            @Result(column = "script", property = "script"),
            @Result(column = "owner_id", property = "owner",one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team",one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")) })
    List<ConnectorJob> getConnectorJobByClusterId(@Param("clusterId") int clusterId);


    @Select("select * from connect_job where id = #{id} order by create_time desc; ")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "type", property = "type"),
            @Result(column = "class_name", property = "className"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "state", property = "state"),
            @Result(column = "script", property = "script"),
            @Result(column = "owner_id", property = "owner",one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team",one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")) })
    ConnectorJob getConnectorJobById(@Param("id") long id);

    @Update("update connect_job set is_history = 1 where id = #{id}")
    int upToHistory(long id);
}
