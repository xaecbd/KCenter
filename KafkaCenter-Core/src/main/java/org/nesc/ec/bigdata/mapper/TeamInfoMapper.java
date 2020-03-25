package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.nesc.ec.bigdata.model.TeamInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeamInfoMapper extends BaseMapper<TeamInfo> {

	@Select("select * from team_info where id=#{id}")
	@Results({
		 @Result(id = true, column = "id", property = "id"),
		 @Result(column = "name", property = "name"),
		 @Result(column = "own", property = "own")
	})
	TeamInfo queryById(Long id);
}
