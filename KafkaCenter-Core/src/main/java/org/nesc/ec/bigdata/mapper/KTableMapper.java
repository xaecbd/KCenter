package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.KTableInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface KTableMapper extends BaseMapper<KTableInfo> {

    @Select("select * from ktable where cluster_id = #{clusterId} order by create_time desc ")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "name", property = "name"),
            @Result(column = "script", property = "script"),
            @Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
            @Result(column = "cluster_id", property = "clusterId") })
    List<KTableInfo> selectTableByClusterId(@Param("clusterId") int clusterId);

    @Select("<script>" + "select * from ktable where team_id in "
            + "<foreach item='item' index='index' collection='teamIDs'  open='(' separator=',' close=')'>" + "#{item}"
            + "</foreach>" + " and cluster_id = #{clusterId} order by create_time desc " + "</script>")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "create_time", property = "createTime"),
            @Result(column = "name", property = "name"),
            @Result(column = "script", property = "script"),
            @Result(column = "owner_id", property = "owner", one = @One(select = "org.nesc.ec.bigdata.mapper.UserInfoMapper.queryById")),
            @Result(column = "team_id", property = "team", one = @One(select = "org.nesc.ec.bigdata.mapper.TeamInfoMapper.queryById")),
            @Result(column = "cluster_id", property = "clusterId") })
    List<KTableInfo> selectKTableInfoByTeam(@Param("teamIDs") List<Long> teamIDs,@Param("clusterId") int clusterId);
}

