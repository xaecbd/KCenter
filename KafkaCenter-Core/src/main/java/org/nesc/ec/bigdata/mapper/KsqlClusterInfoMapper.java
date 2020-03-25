package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.KsqlClusterInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

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


}
