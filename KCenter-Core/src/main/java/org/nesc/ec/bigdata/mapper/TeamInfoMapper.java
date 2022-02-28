package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.nesc.ec.bigdata.model.TeamInfo;

@Mapper
public interface TeamInfoMapper extends BaseMapper<TeamInfo> {

    @Select("select * from team_info where id=#{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "name", property = "name"),
            @Result(column = "alarm_group", property = "alarmGroup"),
            @Result(column = "own", property = "own")
    })
    TeamInfo queryById(Long id);

    @Select("select count(*) from team_info where id != #{id} and name=#{name}")
    int countTeamByName(@Param("id")Long id, @Param("name")String name);
}
