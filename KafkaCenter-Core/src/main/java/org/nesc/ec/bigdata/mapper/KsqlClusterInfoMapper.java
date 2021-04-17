package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;

import java.util.List;

@Mapper
public interface KsqlClusterInfoMapper extends BaseMapper<KsqlClusterInfo> {


    @Select("select * from ksql_info where cluster_id=#{clusterId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "cluster_name", property = "clusterName"),
            @Result(column = "ksql_url", property = "ksqlUrl"),
            @Result(column = "version", property = "version"),
            @Result(column = "ksql_serverId", property = "ksqlServerId")
    })
    KsqlClusterInfo selectByClusterId(Long clusterId);

    @Select("select * from ksql_info where cluster_id=#{clusterId} and ksql_serverId = #{ksqlServerId}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "cluster_name", property = "clusterName"),
            @Result(column = "ksql_url", property = "ksqlUrl"),
            @Result(column = "version", property = "version"),
            @Result(column = "ksql_serverId", property = "ksqlServerId")
    })
    KsqlClusterInfo selectKsqlInfo(Long clusterId,String ksqlServerId);


    @Select("<script>" + "select * from ksql_info where  "
            + "<foreach item='item' index='index' collection='teamIDs'   separator='OR' >" + " FIND_IN_SET(#{item},team_ids) "
            + "</foreach></script>")
    @Results({ @Result(id = true, column = "id", property = "id"),
            @Result(column = "cluster_id", property = "clusterId"),
            @Result(column = "cluster_name", property = "clusterName"),
            @Result(column = "ksql_url", property = "ksqlUrl"),
            @Result(column = "ksql_serverId", property = "ksqlServerId"),
            @Result(column = "version", property = "version"),
            @Result(column = "team_ids", property = "teamIds") })
    List<KsqlClusterInfo> getKsqlByTeamIDs(@Param("teamIDs") List<Long> teamIDs);

}
