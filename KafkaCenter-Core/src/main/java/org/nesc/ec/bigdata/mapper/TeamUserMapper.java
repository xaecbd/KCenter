package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.TeamUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeamUserMapper extends BaseMapper<TeamUser> {
	@Select("select * from user_team where user_id=#{userId}")
    @Results({
    	@Result(id = true, column = "id", property = "id"),
		@Result(column = "owner_id", property = "userId"),
		@Result(column="team_id",property="teamId")
    })
    TeamUser getTeamIdByUserId(Long userId);
}
